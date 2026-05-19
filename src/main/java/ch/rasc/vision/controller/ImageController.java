package ch.rasc.vision.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ch.rasc.vision.Application;
import ch.rasc.vision.dto.VisionResult;
import ch.rasc.vision.entity.Image;
import ch.rasc.vision.util.InMemoryImageStore;
import ch.rasc.vision.util.ValidationMessages;
import ch.rasc.vision.util.ValidationMessagesResult;
import ch.rasc.vision.util.ValidationUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;

@RestController
public class ImageController {

	private final VisionService visionService;

	private final Validator validator;

	private final InMemoryImageStore imageStore;

	public ImageController(VisionService visionService, Validator validator,
			InMemoryImageStore imageStore) {
		this.visionService = visionService;
		this.validator = validator;
		this.imageStore = imageStore;
	}

	@GetMapping("/image/{id}")
	public void downloadImage(HttpServletResponse response, @PathVariable("id") long id)
			throws IOException {
		@SuppressWarnings("resource")
		ServletOutputStream outputStream = response.getOutputStream();
		this.imageStore.writeImageBlob(id, outputStream);
		outputStream.flush();
	}

	@GetMapping("/thumbnail/{id}")
	public void downloadThumbnail(HttpServletResponse response,
			@PathVariable("id") long id) throws IOException {
		response.setContentType("image/jpeg");
		@SuppressWarnings("resource")
		ServletOutputStream outputStream = response.getOutputStream();
		this.imageStore.writeThumbnailBlob(id, outputStream);
		outputStream.flush();
	}

	@GetMapping("/api/images")
	public List<Image> listImages() {
		return new ArrayList<>(read());
	}

	@PostMapping("/api/images")
	public ValidationMessagesResult<Image> saveImage(@RequestBody Image updatedEntity) {
		return update(updatedEntity);
	}

	@DeleteMapping("/api/images/{id}")
	public boolean deleteImage(@PathVariable("id") long id) {
		return destroy(id);
	}

	private List<Image> read() {
		return this.imageStore.readAll();
	}

	private ValidationMessagesResult<Image> update(Image updatedEntity) {

		List<ValidationMessages> violations = new ArrayList<>();
		violations.addAll(ValidationUtil.validateEntity(this.validator, updatedEntity));

		if (violations.isEmpty()) {
			String data = updatedEntity.getData();
			if (StringUtils.hasText(data) && data.startsWith("data:")) {

				if (updatedEntity.getId() >= 0) {
					this.imageStore.deleteImage(updatedEntity.getId());
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

				Image image = this.imageStore.insertImage(updatedEntity);
				image.setData(null);
				return new ValidationMessagesResult<>(image);
			}
		}

		ValidationMessagesResult<Image> result = new ValidationMessagesResult<>(
				updatedEntity);
		result.setValidations(violations);
		return result;
	}

	private boolean destroy(long id) {
		this.imageStore.deleteImage(id);
		return true;
	}

}
