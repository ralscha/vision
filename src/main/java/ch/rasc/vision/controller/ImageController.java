package ch.rasc.vision.controller;

import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_MODIFY;
import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_READ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResult;
import ch.rasc.sse.eventbus.SseEvent;
import ch.rasc.vision.Application;
import ch.rasc.vision.dto.VisionResult;
import ch.rasc.vision.entity.Image;
import ch.rasc.vision.util.ExodusManager;
import ch.rasc.vision.util.ValidationMessages;
import ch.rasc.vision.util.ValidationMessagesResult;
import ch.rasc.vision.util.ValidationUtil;

@RestController
public class ImageController {

	private final VisionService visionService;

	private final Validator validator;

	private final ApplicationEventPublisher publisher;

	private final ExodusManager exodusManager;

	public ImageController(VisionService visionService, Validator validator,
			ApplicationEventPublisher publisher, ExodusManager exodusManager) {
		this.visionService = visionService;
		this.validator = validator;
		this.publisher = publisher;
		this.exodusManager = exodusManager;
	}

	@GetMapping("/image/{id}")
	public ResponseEntity<byte[]> downloadImage(HttpServletRequest request,
			@PathVariable("id") long id) {

		Image image = this.exodusManager.findImage(id);
		if (image != null) {
			byte[] imageBlob = this.exodusManager.getImageBlob(id);
			String md5 = "\"" + DigestUtils.md5DigestAsHex(imageBlob) + "\"";

			String requestETag = request.getHeader(HttpHeaders.IF_NONE_MATCH);
			if (md5.equals(requestETag)) {
				return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
			}

			return ResponseEntity.ok().eTag(md5)
					.cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic())
					.contentType(MediaType.parseMediaType(image.getType()))
					.contentLength(image.getSize()).body(imageBlob);
		}

		return ResponseEntity.notFound().build();

	}

	@ExtDirectMethod(STORE_READ)
	public ExtDirectStoreResult<Image> read() {
		List<Image> results = this.exodusManager.readAll();
		return new ExtDirectStoreResult<>(results.size(), results);
	}

	@PostMapping("/pictureupload")
	@Async
	public void pictureupload(@RequestParam("file") MultipartFile file)
			throws IOException {

		Image image = new Image();
		image.setId(-1L);
		image.setData("data:" + file.getContentType() + ";base64,"
				+ Base64.getEncoder().encodeToString(file.getBytes()));
		image.setName(file.getName());
		image.setSize(file.getSize());
		image.setType(file.getContentType());

		update(image);
		this.publisher.publishEvent(SseEvent.ofEvent("imageadded"));
	}

	@ExtDirectMethod(STORE_MODIFY)
	public ValidationMessagesResult<Image> update(Image updatedEntity) {

		List<ValidationMessages> violations = new ArrayList<>();
		violations.addAll(ValidationUtil.validateEntity(this.validator, updatedEntity));

		if (violations.isEmpty()) {
			String data = updatedEntity.getData();
			if (StringUtils.hasText(data) && data.startsWith("data:")) {

				if (updatedEntity.getId() >= 0) {
					this.exodusManager.deleteImage(updatedEntity.getId());
				}

				try {
					int pos = data.indexOf("base64,");
					String extract = data.substring(pos + 7);

					VisionResult result = this.visionService.vision(extract);
					updatedEntity.setLabels(result.getLabels());
					updatedEntity.setSafeSearch(result.getSafeSearch());
					updatedEntity.setLogos(result.getLogos());
					updatedEntity.setLandmarks(result.getLandmarks());
					updatedEntity.setTexts(result.getTexts());
					updatedEntity.setFaces(result.getFaces());
					updatedEntity.setWeb(result.getWeb());
				}
				catch (Exception e) {
					Application.logger.error("vision", e);
				}

				Image image = this.exodusManager.insertImage(updatedEntity);
				image.setData(null);
				return new ValidationMessagesResult<>(image);
			}
		}

		ValidationMessagesResult<Image> result = new ValidationMessagesResult<>(
				updatedEntity);
		result.setValidations(violations);
		return result;
	}

	@ExtDirectMethod
	public boolean destroy(long id) {
		this.exodusManager.deleteImage(id);
		return true;
	}

}
