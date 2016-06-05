package ch.rasc.vision.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;

import ch.rasc.vision.config.AppConfig;
import ch.rasc.vision.dto.ImmutableVisionResult;
import ch.rasc.vision.dto.ImmutableVisionResult.Builder;
import ch.rasc.vision.dto.VisionResult;
import ch.rasc.vision.entity.Face;
import ch.rasc.vision.entity.FaceLandmark;
import ch.rasc.vision.entity.Label;
import ch.rasc.vision.entity.Landmark;
import ch.rasc.vision.entity.LngLat;
import ch.rasc.vision.entity.Logo;
import ch.rasc.vision.entity.SafeSearch;
import ch.rasc.vision.entity.Text;
import ch.rasc.vision.entity.Vertex;

@Service
public class VisionService {

	private final AppConfig appConfig;

	public VisionService(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public VisionResult vision(String base64data) throws IOException {
		Builder builder = ImmutableVisionResult.builder();
		if (StringUtils.hasText(this.appConfig.getVisionKey())) {
			BatchAnnotateImagesResponse response = sendRequest(base64data);
			//System.out.println(response);
			List<AnnotateImageResponse> responses = response.getResponses();
			if (responses != null) {

				for (AnnotateImageResponse resp : responses) {
					if (resp.getLabelAnnotations() != null) {
						for (EntityAnnotation ea : resp.getLabelAnnotations()) {
							Label l = new Label();
							l.setScore(ea.getScore());
							l.setDescription(ea.getDescription());
							builder.addLabels(l);
						}
					}
					if (resp.getLandmarkAnnotations() != null) {
						for (EntityAnnotation ea : resp.getLandmarkAnnotations()) {
							Landmark l = new Landmark();
							l.setScore(ea.getScore());
							l.setDescription(ea.getDescription());

							if (ea.getBoundingPoly() != null) {
								l.setBoundingPoly(ea.getBoundingPoly().getVertices()
										.stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}
							if (ea.getLocations() != null) {
								l.setLocations(ea.getLocations().stream().map(loc -> {
									LngLat ll = new LngLat();
									ll.setLng(loc.getLatLng().getLongitude());
									ll.setLat(loc.getLatLng().getLatitude());
									return ll;
								}).collect(Collectors.toList()));
							}

							builder.addLandmarks(l);
						}
					}
					if (resp.getLogoAnnotations() != null) {
						for (EntityAnnotation ea : resp.getLogoAnnotations()) {
							Logo l = new Logo();
							l.setScore(ea.getScore());
							l.setDescription(ea.getDescription());

							if (ea.getBoundingPoly() != null) {
								l.setBoundingPoly(ea.getBoundingPoly().getVertices()
										.stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}

							builder.addLogos(l);
						}
					}
					if (resp.getTextAnnotations() != null) {
						for (EntityAnnotation ea : resp.getTextAnnotations()) {
							Text t = new Text();
							t.setDescription(ea.getDescription());

							if (ea.getBoundingPoly() != null) {
								t.setBoundingPoly(ea.getBoundingPoly().getVertices()
										.stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}

							builder.addTexts(t);
						}
					}
					if (resp.getFaceAnnotations() != null) {
						for (FaceAnnotation fa : resp.getFaceAnnotations()) {
							Face face = new Face();
							face.setRollAngle(fa.getRollAngle());
							face.setPanAngle(fa.getPanAngle());
							face.setTiltAngle(fa.getTiltAngle());
							face.setDetectionConfidence(fa.getDetectionConfidence());
							face.setLandmarkingConfidence(fa.getLandmarkingConfidence());
							face.setJoy(fa.getJoyLikelihood());
							face.setSorrow(fa.getSorrowLikelihood());
							face.setAnger(fa.getAngerLikelihood());
							face.setSurprise(fa.getSurpriseLikelihood());
							face.setUnderExposed(fa.getUnderExposedLikelihood());
							face.setBlurred(fa.getBlurredLikelihood());
							face.setHeadwear(fa.getHeadwearLikelihood());
							face.setJoyRating(
									Likelihood.of(fa.getJoyLikelihood()).getRating());
							face.setSorrowRating(
									Likelihood.of(fa.getSorrowLikelihood()).getRating());
							face.setAngerRating(
									Likelihood.of(fa.getAngerLikelihood()).getRating());
							face.setSurpriseRating(Likelihood
									.of(fa.getSurpriseLikelihood()).getRating());
							face.setUnderExposedRating(Likelihood
									.of(fa.getUnderExposedLikelihood()).getRating());
							face.setBlurredRating(
									Likelihood.of(fa.getBlurredLikelihood()).getRating());
							face.setHeadwearRating(Likelihood
									.of(fa.getHeadwearLikelihood()).getRating());

							if (fa.getBoundingPoly() != null) {
								face.setBoundingPoly(fa.getBoundingPoly().getVertices()
										.stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}

							if (fa.getFdBoundingPoly() != null) {
								face.setFdBoundingPoly(fa.getFdBoundingPoly()
										.getVertices().stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}

							if (fa.getLandmarks() != null) {
								face.setLandmarks(fa.getLandmarks().stream().map(l -> {
									FaceLandmark fl = new FaceLandmark();
									fl.setType(l.getType());
									fl.setX(l.getPosition().getX());
									fl.setY(l.getPosition().getY());
									fl.setZ(l.getPosition().getZ());
									return fl;
								}).collect(Collectors.toList()));
							}

							builder.addFaces(face);
						}
					}
					if (resp.getSafeSearchAnnotation() != null) {

						SafeSearch safeSearch = new SafeSearch();
						safeSearch.setAdult(resp.getSafeSearchAnnotation().getAdult());
						safeSearch
								.setMedical(resp.getSafeSearchAnnotation().getMedical());
						safeSearch.setSpoof(resp.getSafeSearchAnnotation().getSpoof());
						safeSearch.setViolence(
								resp.getSafeSearchAnnotation().getViolence());

						safeSearch.setAdultRating(
								Likelihood.of(resp.getSafeSearchAnnotation().getAdult())
										.getRating());
						safeSearch.setMedicalRating(
								Likelihood.of(resp.getSafeSearchAnnotation().getMedical())
										.getRating());
						safeSearch.setSpoofRating(
								Likelihood.of(resp.getSafeSearchAnnotation().getSpoof())
										.getRating());
						safeSearch.setViolenceRating(Likelihood
								.of(resp.getSafeSearchAnnotation().getViolence())
								.getRating());

						builder.safeSearch(safeSearch);
					}
				}
			}
		}
		return builder.build();
	}

	private BatchAnnotateImagesResponse sendRequest(String base64data)
			throws IOException {

		ApacheHttpTransport httpTransport = new ApacheHttpTransport();
		Vision.Builder builder = new Vision.Builder(httpTransport, new JacksonFactory(),
				null);

		builder.setApplicationName("Vision Demo Application");
		builder.setVisionRequestInitializer(
				new VisionRequestInitializer(this.appConfig.getVisionKey()));
		Vision vision = builder.build();

		BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();

		AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

		// Add the image
		com.google.api.services.vision.v1.model.Image base64EncodedImage = new com.google.api.services.vision.v1.model.Image();
		base64EncodedImage.setContent(base64data);
		annotateImageRequest.setImage(base64EncodedImage);

		// add the features we want
		List<Feature> features = new ArrayList<>();
		features.add(newFeature(DetectionType.FACE_DETECTION));
		features.add(newFeature(DetectionType.LANDMARK_DETECTION));
		features.add(newFeature(DetectionType.LOGO_DETECTION));
		features.add(newFeature(DetectionType.LABEL_DETECTION));
		features.add(newFeature(DetectionType.TEXT_DETECTION));
		features.add(newFeature(DetectionType.SAFE_SEARCH_DETECTION));
		// features.add(newFeature(DetectionType.IMAGE_PROPERTIES));

		annotateImageRequest.setFeatures(features);

		batchAnnotateImagesRequest
				.setRequests(Collections.singletonList(annotateImageRequest));

		Vision.Images.Annotate annotateRequest = vision.images()
				.annotate(batchAnnotateImagesRequest);

		return annotateRequest.execute();

	}

	private static Feature newFeature(DetectionType detectionType) {
		Feature feature = new Feature();
		feature.setType(detectionType.name());
		return feature;
	}

}
