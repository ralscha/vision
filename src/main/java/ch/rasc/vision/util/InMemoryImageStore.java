package ch.rasc.vision.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import ch.rasc.vision.Application;
import ch.rasc.vision.entity.Image;
import net.coobird.thumbnailator.Thumbnails;

@Component
public class InMemoryImageStore {

	private final AtomicLong nextId = new AtomicLong();

	private final ConcurrentSkipListMap<Long, StoredImage> images = new ConcurrentSkipListMap<>();

	public Image insertImage(Image image) {
		byte[] bytes = extractBytes(image.getData());
		byte[] thumbnailBytes = createThumbnail(bytes);
		long id = this.nextId.incrementAndGet();

		Image storedImage = copyImage(image);
		storedImage.setId(id);
		storedImage.setData(null);

		this.images.put(id, new StoredImage(storedImage, bytes, thumbnailBytes));
		return copyImage(storedImage);
	}

	public void deleteImage(long id) {
		this.images.remove(id);
	}

	public List<Image> readAll() {
		List<Image> result = new ArrayList<>(this.images.size());
		for (StoredImage storedImage : this.images.descendingMap().values()) {
			result.add(copyImage(storedImage.image()));
		}
		return result;
	}

	public void writeThumbnailBlob(long id, OutputStream out) {
		StoredImage storedImage = this.images.get(id);
		if (storedImage != null) {
			write(out, storedImage.thumbnailBytes());
		}
	}

	public void writeImageBlob(long id, OutputStream out) {
		StoredImage storedImage = this.images.get(id);
		if (storedImage != null) {
			write(out, storedImage.imageBytes());
		}
	}

	private static byte[] extractBytes(String data) {
		int pos = data.indexOf("base64,");
		String extract = data.substring(pos + 7);
		return Base64.getDecoder().decode(extract);
	}

	private static byte[] createThumbnail(byte[] bytes) {
		ByteArrayOutputStream thumbnailBaos = new ByteArrayOutputStream();
		try (InputStream stream = new ByteArrayInputStream(bytes)) {
			Thumbnails.of(stream).width(50).outputFormat("jpg")
					.toOutputStream(thumbnailBaos);
			return thumbnailBaos.toByteArray();
		}
		catch (IOException e) {
			Application.logger.error("uploaded", e);
			return new byte[0];
		}
	}

	private static void write(OutputStream out, byte[] data) {
		try {
			FileCopyUtils.copy(data, out);
		}
		catch (IOException e) {
			Application.logger.error("write image blob", e);
		}
	}

	private static Image copyImage(Image source) {
		Image copy = new Image();
		copy.setId(source.getId());
		copy.setName(source.getName());
		copy.setLabels(source.getLabels());
		copy.setLogos(source.getLogos());
		copy.setLandmarks(source.getLandmarks());
		copy.setTexts(source.getTexts());
		copy.setFaces(source.getFaces());
		copy.setSafeSearch(source.getSafeSearch());
		copy.setWeb(source.getWeb());
		copy.setType(source.getType());
		copy.setSize(source.getSize());
		copy.setData(source.getData());
		return copy;
	}

	private static final class StoredImage {

		private final Image image;

		private final byte[] imageBytes;

		private final byte[] thumbnailBytes;

		private StoredImage(Image image, byte[] imageBytes, byte[] thumbnailBytes) {
			this.image = image;
			this.imageBytes = imageBytes;
			this.thumbnailBytes = thumbnailBytes;
		}

		private Image image() {
			return this.image;
		}

		private byte[] imageBytes() {
			return this.imageBytes;
		}

		private byte[] thumbnailBytes() {
			return this.thumbnailBytes;
		}
	}

}