package ch.rasc.vision.controller;

import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_MODIFY;
import static ch.ralscha.extdirectspring.annotation.ExtDirectMethodType.STORE_READ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Validator;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;
import ch.ralscha.extdirectspring.bean.ExtDirectStoreResult;
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

	private final ExodusManager exodusManager;

	public ImageController(VisionService visionService, Validator validator,
			ExodusManager exodusManager) {
		this.visionService = visionService;
		this.validator = validator;
		this.exodusManager = exodusManager;
	}

	@GetMapping("/image/{id}")
	public void downloadImage(HttpServletResponse response, @PathVariable("id") long id)
			throws IOException {
		@SuppressWarnings("resource")
		ServletOutputStream outputStream = response.getOutputStream();
		this.exodusManager.writeImageBlob(id, outputStream);
		outputStream.flush();
	}

	@GetMapping("/thumbnail/{id}")
	public void downloadThumbnail(HttpServletResponse response,
			@PathVariable("id") long id) throws IOException {
		response.setContentType("image/jpeg");
		@SuppressWarnings("resource")
		ServletOutputStream outputStream = response.getOutputStream();
		this.exodusManager.writeThumbnailBlob(id, outputStream);
		outputStream.flush();
	}

	@ExtDirectMethod(STORE_READ)
	public ExtDirectStoreResult<Image> read() {
		List<Image> results = this.exodusManager.readAll();
		return new ExtDirectStoreResult<>(results.size(), results);
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
