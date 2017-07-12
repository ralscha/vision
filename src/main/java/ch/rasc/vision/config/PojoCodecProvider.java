package ch.rasc.vision.config;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import ch.rasc.vision.entity.Face;
import ch.rasc.vision.entity.FaceCodec;
import ch.rasc.vision.entity.FaceLandmark;
import ch.rasc.vision.entity.FaceLandmarkCodec;
import ch.rasc.vision.entity.Image;
import ch.rasc.vision.entity.ImageCodec;
import ch.rasc.vision.entity.Label;
import ch.rasc.vision.entity.LabelCodec;
import ch.rasc.vision.entity.Landmark;
import ch.rasc.vision.entity.LandmarkCodec;
import ch.rasc.vision.entity.LngLat;
import ch.rasc.vision.entity.LngLatCodec;
import ch.rasc.vision.entity.Logo;
import ch.rasc.vision.entity.LogoCodec;
import ch.rasc.vision.entity.SafeSearch;
import ch.rasc.vision.entity.SafeSearchCodec;
import ch.rasc.vision.entity.Text;
import ch.rasc.vision.entity.TextCodec;
import ch.rasc.vision.entity.UUIDStringGenerator;
import ch.rasc.vision.entity.Vertex;
import ch.rasc.vision.entity.VertexCodec;
import ch.rasc.vision.entity.Web;
import ch.rasc.vision.entity.WebCodec;
import ch.rasc.vision.entity.WebEntity;
import ch.rasc.vision.entity.WebEntityCodec;
import ch.rasc.vision.entity.WebUrl;
import ch.rasc.vision.entity.WebUrlCodec;

public final class PojoCodecProvider implements CodecProvider {
	private final UUIDStringGenerator uUIDStringGenerator;

	public PojoCodecProvider() {
		this(new UUIDStringGenerator());
	}

	public PojoCodecProvider(final UUIDStringGenerator uUIDStringGenerator) {
		this.uUIDStringGenerator = uUIDStringGenerator;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
		if (clazz.equals(Image.class)) {
			return (Codec<T>) new ImageCodec(registry, this.uUIDStringGenerator);
		}
		if (clazz.equals(Label.class)) {
			return (Codec<T>) new LabelCodec();
		}
		if (clazz.equals(SafeSearch.class)) {
			return (Codec<T>) new SafeSearchCodec();
		}
		if (clazz.equals(Logo.class)) {
			return (Codec<T>) new LogoCodec(registry);
		}
		if (clazz.equals(Vertex.class)) {
			return (Codec<T>) new VertexCodec();
		}
		if (clazz.equals(LngLat.class)) {
			return (Codec<T>) new LngLatCodec();
		}
		if (clazz.equals(Landmark.class)) {
			return (Codec<T>) new LandmarkCodec(registry);
		}
		if (clazz.equals(Text.class)) {
			return (Codec<T>) new TextCodec(registry);
		}
		if (clazz.equals(Face.class)) {
			return (Codec<T>) new FaceCodec(registry);
		}
		if (clazz.equals(FaceLandmark.class)) {
			return (Codec<T>) new FaceLandmarkCodec();
		}
		if (clazz.equals(Web.class)) {
			return (Codec<T>) new WebCodec(registry);
		}
		if (clazz.equals(WebEntity.class)) {
			return (Codec<T>) new WebEntityCodec();
		}
		if (clazz.equals(WebUrl.class)) {
			return (Codec<T>) new WebUrlCodec();
		}
		return null;
	}
}
