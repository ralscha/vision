package ch.rasc.vision.controller;

import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_MODIFY;
import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_READ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.FindIterable;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreReadRequest;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResult;
import ch.rasc.vision.Application;
import ch.rasc.vision.config.MongoDb;
import ch.rasc.vision.dto.VisionResult;
import ch.rasc.vision.entity.CImage;
import ch.rasc.vision.entity.Image;
import ch.rasc.vision.eventbus.EventBusEvent;
import ch.rasc.vision.util.QueryUtil;
import ch.rasc.vision.util.ValidationMessages;
import ch.rasc.vision.util.ValidationMessagesResult;
import ch.rasc.vision.util.ValidationUtil;
import net.coobird.thumbnailator.Thumbnails;

@RestController
public class ImageController {

	private final MongoDb mongoDb;

	private final VisionService visionService;

	private final Validator validator;

	private final ApplicationEventPublisher publisher;

	public ImageController(MongoDb mongoDb, VisionService visionService,
			Validator validator, ApplicationEventPublisher publisher) {
		this.mongoDb = mongoDb;
		this.visionService = visionService;
		this.validator = validator;
		this.publisher = publisher;
	}

	@GetMapping("/image/{id}/{filename:.+}")
	public void downloadImage(HttpServletRequest request, @PathVariable("id") String id,
			@SuppressWarnings("unused") @PathVariable("filename") String filename,
			HttpServletResponse response) throws IOException {

		Image image = this.mongoDb.findFirst(Image.class, CImage.id, id);

		if (image != null && image.getFileId() != null) {
			Document filesDoc = this.mongoDb.getCollection("image.files")
					.find(Filters.eq("_id", image.getFileId()))
					.projection(Projections.fields(Projections.exclude("_id"),
							Projections.include("md5")))
					.first();
			String md5 = "\"" + filesDoc.getString("md5") + "\"";

			String requestETag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
			if (md5.equals(requestETag)) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			response.setHeader(HttpHeaders.ETAG, md5);
			response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000");

			response.setContentType(image.getType());
			response.setContentLengthLong(image.getSize());

			GridFSBucket bucket = this.mongoDb.createBucket("image");
			@SuppressWarnings("resource")
			ServletOutputStream out = response.getOutputStream();
			bucket.downloadToStream(image.getFileId(), out);
			out.flush();
		}
	}

	@ExtDirectMethod(STORE_READ)
	public ExtDirectStoreResult<Image> read(ExtDirectStoreReadRequest request) {

		FindIterable<Image> find = this.mongoDb.getCollection(Image.class).find();
		find.sort(Sorts.orderBy(QueryUtil.getSorts(request)));

		List<Image> list = QueryUtil.toList(find);
		return new ExtDirectStoreResult<>(list.size(), list);
	}

	@PostMapping("/pictureupload")
	@Async
	public void pictureupload(@RequestParam("file") MultipartFile file)
			throws IOException {

		Image image = new Image();
		image.setId(UUID.randomUUID().toString());
		image.setData("data:"+file.getContentType()+";base64,"
				+ Base64.getEncoder().encodeToString(file.getBytes()));
		image.setName(file.getName());
		image.setSize(file.getSize());
		image.setType(file.getContentType());

		ValidationMessagesResult<Image> result = update(image);
		
		publisher.publishEvent(EventBusEvent.of("imageadded", result.getRecords().iterator().next()));
	}

	@ExtDirectMethod(STORE_MODIFY)
	public ValidationMessagesResult<Image> update(Image updatedEntity) {

		List<ValidationMessages> violations = new ArrayList<>();
		violations.addAll(ValidationUtil.validateEntity(this.validator, updatedEntity));

		if (violations.isEmpty()) {
			String data = updatedEntity.getData();
			if (StringUtils.hasText(data) && data.startsWith("data:")) {

				List<Bson> updates = new ArrayList<>();
				updates.add(Updates.set(CImage.name, updatedEntity.getName()));
				updates.add(Updates.set(CImage.size, updatedEntity.getSize()));
				updates.add(Updates.set(CImage.type, updatedEntity.getType()));

				Image updatedImage = this.mongoDb.getCollection(Image.class)
						.findOneAndUpdate(Filters.eq(CImage.id, updatedEntity.getId()),
								Updates.combine(updates),
								new FindOneAndUpdateOptions()
										.returnDocument(ReturnDocument.BEFORE)
										.upsert(true));

				int pos = data.indexOf("base64,");
				if (pos != -1) {

					List<Bson> addUpdates = new ArrayList<>();

					GridFSBucket bucket = this.mongoDb.createBucket("image");
					if (updatedImage != null && updatedImage.getFileId() != null) {
						bucket.delete(updatedImage.getFileId());
					}
					String extract = data.substring(pos + 7);
					byte[] bytes = Base64.getDecoder().decode(extract);

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					InputStream stream = new ByteArrayInputStream(bytes);
					try {
						Thumbnails.of(stream).width(50).outputFormat("jpg")
								.toOutputStream(baos);
						String thumbnailData = Base64.getEncoder()
								.encodeToString(baos.toByteArray());

						addUpdates.add(Updates.set(CImage.thumbnail,
								"data:image/jpeg;base64," + thumbnailData));
					}
					catch (Exception e) {
						Application.logger.error("uploaded", e);
					}

					try (ByteArrayInputStream source = new ByteArrayInputStream(bytes)) {
						ObjectId fileId = bucket.uploadFromStream(updatedEntity.getId(),
								source);
						addUpdates.add(Updates.set(CImage.fileId, fileId));
					}
					catch (IOException e) {
						Application.logger.error("upload document", e);
					}

					try {
						VisionResult result = this.visionService.vision(extract);
						if (result.labels() != null) {
							addUpdates.add(Updates.set(CImage.labels, result.labels()));
						}
						else {
							addUpdates.add(
									Updates.set(CImage.labels, Collections.emptyList()));
						}

						if (result.safeSearch() != null) {
							addUpdates.add(
									Updates.set(CImage.safeSearch, result.safeSearch()));
						}

						if (result.logos() != null) {
							addUpdates.add(Updates.set(CImage.logos, result.logos()));
						}
						else {
							addUpdates.add(
									Updates.set(CImage.logos, Collections.emptyList()));
						}

						if (result.landmarks() != null) {
							addUpdates.add(
									Updates.set(CImage.landmarks, result.landmarks()));
						}
						else {
							addUpdates.add(Updates.set(CImage.landmarks,
									Collections.emptyList()));
						}

						if (result.texts() != null) {
							addUpdates.add(Updates.set(CImage.texts, result.texts()));
						}
						else {
							addUpdates.add(
									Updates.set(CImage.texts, Collections.emptyList()));
						}

						if (result.faces() != null) {
							addUpdates.add(Updates.set(CImage.faces, result.faces()));
						}
						else {
							addUpdates.add(
									Updates.set(CImage.faces, Collections.emptyList()));
						}
					}
					catch (IOException e) {
						Application.logger.error("vision", e);
					}

					if (!addUpdates.isEmpty()) {
						this.mongoDb.getCollection(Image.class).findOneAndUpdate(
								Filters.eq(CImage.id, updatedEntity.getId()),
								Updates.combine(addUpdates));
					}

				}
			}
			else {
				// just update the name
				if (StringUtils.hasText(updatedEntity.getName())) {
					this.mongoDb.getCollection(Image.class).updateOne(
							Filters.eq(CImage.id, updatedEntity.getId()),
							Updates.set(CImage.name, updatedEntity.getName()));
				}
			}

			return new ValidationMessagesResult<>(this.mongoDb.findFirst(Image.class,
					CImage.id, updatedEntity.getId()));
		}

		ValidationMessagesResult<Image> result = new ValidationMessagesResult<>(
				updatedEntity);
		result.setValidations(violations);
		return result;
	}

	@ExtDirectMethod(STORE_MODIFY)
	public ExtDirectStoreResult<Image> destroy(Image destroyBinary) {
		ExtDirectStoreResult<Image> result = new ExtDirectStoreResult<>();

		Image doc = this.mongoDb.getCollection(Image.class)
				.findOneAndDelete(Filters.eq(CImage.id, destroyBinary.getId()));

		if (doc != null && doc.getFileId() != null) {
			GridFSBucket bucket = this.mongoDb.createBucket("image");
			bucket.delete(doc.getFileId());
		}

		result.setSuccess(Boolean.TRUE);
		return result;
	}

}
