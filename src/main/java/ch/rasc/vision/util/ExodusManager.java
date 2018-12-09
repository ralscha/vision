package ch.rasc.vision.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PreDestroy;

import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoFactory;
import com.esotericsoftware.kryo.pool.KryoPool;

import ch.rasc.vision.Application;
import ch.rasc.vision.config.AppConfig;
import ch.rasc.vision.entity.Image;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.PersistentEntityStore;
import jetbrains.exodus.entitystore.PersistentEntityStores;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import net.coobird.thumbnailator.Thumbnails;

@Component
public class ExodusManager {

	private static final String IMAGE = "images";

	private final Environment environment;

	private final PersistentEntityStore persistentEntityStore;

	private final KryoPool kryoPool;

	public ExodusManager(AppConfig appConfig) {
		this.environment = Environments.newInstance(appConfig.getXodusPath());
		this.persistentEntityStore = PersistentEntityStores.newInstance(this.environment);

		KryoFactory factory = () -> {
			Kryo kryo = new Kryo();
			kryo.register(Image.class);
			return kryo;
		};
		this.kryoPool = new KryoPool.Builder(factory).softReferences().build();
	}

	@PreDestroy
	public void destroy() {
		if (this.persistentEntityStore != null) {
			this.persistentEntityStore.close();
		}
	}

	public Image insertImage(Image image) {
		int pos = image.getData().indexOf("base64,");
		String extract = image.getData().substring(pos + 7);
		byte[] bytes = Base64.getDecoder().decode(extract);

		ByteArrayOutputStream thumbnailBaos = new ByteArrayOutputStream();
		InputStream stream = new ByteArrayInputStream(bytes);
		try {
			Thumbnails.of(stream).width(50).outputFormat("jpg")
					.toOutputStream(thumbnailBaos);
		}
		catch (Exception e) {
			Application.logger.error("uploaded", e);
		}

		return this.persistentEntityStore.computeInTransaction(txn -> {
			Entity dp = txn.newEntity(IMAGE);
			long id = txn.getSequence("imageSequence").increment();
			image.setId(id);
			dp.setProperty("id", id);
			dp.setBlob("image", new ByteArrayInputStream(bytes));
			dp.setBlob("thumbnail",
					new ByteArrayInputStream(thumbnailBaos.toByteArray()));

			Kryo kryo = this.kryoPool.borrow();
			try {
				@SuppressWarnings("resource")
				Output output = new Output(32, -1);
				kryo.writeObject(output, image);
				output.close();
				dp.setBlob("obj", new ByteArrayInputStream(output.toBytes()));
			}
			finally {
				this.kryoPool.release(kryo);
			}

			return image;
		});
	}

	public void deleteImage(long id) {
		this.persistentEntityStore.executeInTransaction(txn -> {
			Entity entity = txn.find(IMAGE, "id", id).getFirst();
			if (entity != null) {
				entity.delete();
			}
		});
	}

	public List<Image> readAll() {
		return this.persistentEntityStore.computeInReadonlyTransaction(txn -> {
			return StreamSupport.stream(txn.getAll(IMAGE).spliterator(), false)
					.map(this::toImage).collect(Collectors.toList());
		});
	}

	public Image toImage(Entity entity) {
		return this.kryoPool.run(kryo -> {
			try (Input input = new Input(entity.getBlob("obj"))) {
				return kryo.readObject(input, Image.class);
			}
		});
	}

	public Image findImage(long id) {
		return this.persistentEntityStore.computeInReadonlyTransaction(txn -> {
			Entity entity = txn.find(IMAGE, "id", id).getFirst();
			if (entity != null) {
				return toImage(entity);
			}
			return null;
		});
	}

	public void writeThumbnailBlob(long id, OutputStream out) {
		this.persistentEntityStore.executeInReadonlyTransaction(txn -> {
			Entity entity = txn.find(IMAGE, "id", id).getFirst();
			if (entity != null) {
				try {
					FileCopyUtils.copy(entity.getBlob("thumbnail"), out);
				}
				catch (IOException e) {
					Application.logger.error("write image blog", e);
				}
			}
		});
	}

	public void writeImageBlob(long id, OutputStream out) {
		this.persistentEntityStore.executeInReadonlyTransaction(txn -> {
			Entity entity = txn.find(IMAGE, "id", id).getFirst();
			if (entity != null) {
				try {
					FileCopyUtils.copy(entity.getBlob("image"), out);
				}
				catch (IOException e) {
					Application.logger.error("write image blog", e);
				}
			}
		});
	}

}
