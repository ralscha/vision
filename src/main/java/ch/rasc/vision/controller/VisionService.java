package ch.rasc.vision.controller;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.Likelihood;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.cloud.vision.v1.WebDetection;
import com.google.cloud.vision.v1.WebDetection.WebEntity;
import com.google.cloud.vision.v1.WebDetection.WebImage;
import com.google.cloud.vision.v1.WebDetection.WebPage;
import com.google.protobuf.ByteString;

import ch.rasc.vision.config.AppConfig;
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
import ch.rasc.vision.entity.Web;
import ch.rasc.vision.entity.WebUrl;

@Service
public class VisionService {

	private final AppConfig appConfig;

	public VisionService(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public VisionResult vision(String base64data) throws IOException, Exception {
		com.google.cloud.vision.v1.Image image = com.google.cloud.vision.v1.Image
				.newBuilder()
				.setContent(ByteString.copyFrom(Base64.getDecoder().decode(base64data)))
				.build();
		return annotate(image);
	}

	public VisionResult visionFromStorage(String objectName) throws IOException, Exception {
		com.google.cloud.vision.v1.Image image = com.google.cloud.vision.v1.Image
				.newBuilder().setSource(ImageSource.newBuilder().setGcsImageUri(
						"gs://" + storageBucket() + "/" + objectName))
				.build();
		return annotate(image);
	}

	public SignedUploadTarget createSignedUploadTarget(String fileName,
			String contentType) throws IOException {
		String objectName = "uploads/" + UUID.randomUUID() + "-" + sanitizeFileName(fileName);
		BlobInfo blobInfo = BlobInfo.newBuilder(storageBucket(), objectName)
				.setContentType(StringUtils.hasText(contentType) ? contentType
						: "application/octet-stream")
				.build();

		URL signedUrl = storage().signUrl(blobInfo, 15, TimeUnit.MINUTES,
				SignUrlOption.httpMethod(HttpMethod.PUT),
				SignUrlOption.withV4Signature(),
				SignUrlOption.withExtHeaders(
						java.util.Map.of("Content-Type", blobInfo.getContentType())));

		return new SignedUploadTarget(signedUrl.toString(), objectName,
				blobInfo.getContentType());
	}

	public byte[] downloadStorageObject(String objectName) throws IOException {
		try (ReadChannel channel = storage()
				.reader(BlobId.of(storageBucket(), objectName));
				java.io.InputStream inputStream = java.nio.channels.Channels
						.newInputStream(channel)) {
			return inputStream.readAllBytes();
		}
	}

	private VisionResult annotate(com.google.cloud.vision.v1.Image image)
			throws IOException, Exception {
		ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(
				Files.newInputStream(Paths.get(this.appConfig.getCredentialsPath())));
		ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
				.setCredentialsProvider(FixedCredentialsProvider.create(credentials))
				.build();
		try (ImageAnnotatorClient vision = ImageAnnotatorClient.create(settings)) {

			List<AnnotateImageRequest> requests = new ArrayList<>();
			AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
					.addFeatures(
							Feature.newBuilder().setType(Feature.Type.FACE_DETECTION)
									.build())
					.addFeatures(
							Feature.newBuilder().setType(Feature.Type.LANDMARK_DETECTION)
									.build())
					.addFeatures(
							Feature.newBuilder().setType(Feature.Type.LOGO_DETECTION)
									.build())
					.addFeatures(Feature.newBuilder()
							.setType(Feature.Type.LABEL_DETECTION).setMaxResults(20)
							.build())
					.addFeatures(
							Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION)
									.build())
					.addFeatures(Feature.newBuilder()
							.setType(Feature.Type.SAFE_SEARCH_DETECTION).build())
					.addFeatures(Feature.newBuilder().setType(Feature.Type.WEB_DETECTION)
							.setMaxResults(10).build())
					.setImage(image).build();
			requests.add(request);

			// Performs label detection on the image file
			BatchAnnotateImagesResponse response = vision.batchAnnotateImages(requests);
			List<AnnotateImageResponse> responses = response.getResponsesList();
			VisionResult result = new VisionResult();
			if (!responses.isEmpty()) {

				for (AnnotateImageResponse resp : responses) {
					if (!resp.getLabelAnnotationsList().isEmpty()) {
						List<Label> labels = new ArrayList<>();
						for (EntityAnnotation ea : resp.getLabelAnnotationsList()) {
							Label l = new Label();
							l.setScore(ea.getScore());
							l.setDescription(ea.getDescription());
							labels.add(l);
						}
						result.setLabels(labels);
					}
					if (!resp.getLandmarkAnnotationsList().isEmpty()) {
						List<Landmark> landmarks = new ArrayList<>();
						for (EntityAnnotation ea : resp.getLandmarkAnnotationsList()) {
							Landmark l = new Landmark();
							l.setScore(ea.getScore());
							l.setDescription(ea.getDescription());

							if (!ea.getBoundingPoly().getVerticesList().isEmpty()) {
								l.setBoundingPoly(ea.getBoundingPoly().getVerticesList()
										.stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}
							if (!ea.getLocationsList().isEmpty()) {
								l.setLocations(ea.getLocationsList().stream().map(loc -> {
									LngLat ll = new LngLat();
									ll.setLng(loc.getLatLng().getLongitude());
									ll.setLat(loc.getLatLng().getLatitude());
									return ll;
								}).collect(Collectors.toList()));
							}
							landmarks.add(l);
						}
						result.setLandmarks(landmarks);
					}
					if (!resp.getLogoAnnotationsList().isEmpty()) {
						List<Logo> logos = new ArrayList<>();
						for (EntityAnnotation ea : resp.getLogoAnnotationsList()) {
							Logo l = new Logo();
							l.setScore(ea.getScore());
							l.setDescription(ea.getDescription());

							if (!ea.getBoundingPoly().getVerticesList().isEmpty()) {
								l.setBoundingPoly(ea.getBoundingPoly().getVerticesList()
										.stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}
							logos.add(l);
						}
						result.setLogos(logos);
					}
					if (!resp.getTextAnnotationsList().isEmpty()) {
						List<Text> texts = new ArrayList<>();
						for (EntityAnnotation ea : resp.getTextAnnotationsList()) {
							Text t = new Text();
							t.setDescription(ea.getDescription());

							if (!ea.getBoundingPoly().getVerticesList().isEmpty()) {
								t.setBoundingPoly(ea.getBoundingPoly().getVerticesList()
										.stream().filter(Objects::nonNull).map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}

							texts.add(t);
						}
						result.setTexts(texts);
					}
					if (!resp.getFaceAnnotationsList().isEmpty()) {
						List<Face> faces = new ArrayList<>();
						for (FaceAnnotation fa : resp.getFaceAnnotationsList()) {
							Face face = new Face();
							face.setRollAngle(fa.getRollAngle());
							face.setPanAngle(fa.getPanAngle());
							face.setTiltAngle(fa.getTiltAngle());
							face.setDetectionConfidence(fa.getDetectionConfidence());
							face.setLandmarkingConfidence(fa.getLandmarkingConfidence());

							face.setJoy(fa.getJoyLikelihood());
							face.setJoyRating(likelihoodToNumber(fa.getJoyLikelihood()));

							face.setSorrow(fa.getSorrowLikelihood());
							face.setSorrowRating(
									likelihoodToNumber(fa.getSorrowLikelihood()));

							face.setAnger(fa.getAngerLikelihood());
							face.setAngerRating(
									likelihoodToNumber(fa.getAngerLikelihood()));

							face.setSurprise(fa.getSurpriseLikelihood());
							face.setSurpriseRating(
									likelihoodToNumber(fa.getSurpriseLikelihood()));

							face.setUnderExposed(fa.getUnderExposedLikelihood());
							face.setUnderExposedRating(
									likelihoodToNumber(fa.getUnderExposedLikelihood()));

							face.setBlurred(fa.getBlurredLikelihood());
							face.setBlurredRating(
									likelihoodToNumber(fa.getBlurredLikelihood()));

							face.setHeadwear(fa.getHeadwearLikelihood());
							face.setHeadwearRating(
									likelihoodToNumber(fa.getHeadwearLikelihood()));

							if (!fa.getBoundingPoly().getVerticesList().isEmpty()) {
								face.setBoundingPoly(fa.getBoundingPoly()
										.getVerticesList().stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}

							if (!fa.getFdBoundingPoly().getVerticesList().isEmpty()) {
								face.setFdBoundingPoly(fa.getFdBoundingPoly()
										.getVerticesList().stream().map(v -> {
											Vertex vertex = new Vertex();
											vertex.setX(v.getX());
											vertex.setY(v.getY());
											return vertex;
										}).collect(Collectors.toList()));
							}

							if (!fa.getLandmarksList().isEmpty()) {
								face.setLandmarks(
										fa.getLandmarksList().stream().map(l -> {
											FaceLandmark fl = new FaceLandmark();
											fl.setType(l.getType());
											fl.setX(l.getPosition().getX());
											fl.setY(l.getPosition().getY());
											fl.setZ(l.getPosition().getZ());
											return fl;
										}).collect(Collectors.toList()));
							}

							faces.add(face);
						}
						result.setFaces(faces);
					}
					SafeSearchAnnotation safeSearchAnnotation = resp
							.getSafeSearchAnnotation();
					if (!safeSearchAnnotation.equals(
							SafeSearchAnnotation.getDefaultInstance())) {
						SafeSearch safeSearch = new SafeSearch();
						safeSearch.setAdult(safeSearchAnnotation.getAdult());
						safeSearch.setAdultRating(
								likelihoodToNumber(safeSearchAnnotation.getAdult()));
						safeSearch.setMedical(safeSearchAnnotation.getMedical());
						safeSearch.setMedicalRating(
								likelihoodToNumber(safeSearchAnnotation.getMedical()));
						safeSearch.setSpoof(safeSearchAnnotation.getSpoof());
						safeSearch.setSpoofRating(
								likelihoodToNumber(safeSearchAnnotation.getSpoof()));
						safeSearch.setViolence(safeSearchAnnotation.getViolence());
						safeSearch.setViolenceRating(
								likelihoodToNumber(safeSearchAnnotation.getViolence()));

						result.setSafeSearch(safeSearch);
					}

					WebDetection webDetection = resp.getWebDetection();
					if (!webDetection.equals(WebDetection.getDefaultInstance())) {
						Web web = new Web();
						List<WebImage> fullMatchingImagesList = webDetection
								.getFullMatchingImagesList();
						List<WebPage> pagesWithMatchingImagesList = webDetection
								.getPagesWithMatchingImagesList();
						List<WebImage> partialMatchingImagesList = webDetection
								.getPartialMatchingImagesList();
						List<WebEntity> webEntitiesList = webDetection
								.getWebEntitiesList();

						if (!fullMatchingImagesList.isEmpty()) {
							web.setFullMatchingImages(
									fullMatchingImagesList.stream().map(e -> {
										WebUrl wu = new WebUrl();
										wu.setScore(e.getScore());
										wu.setUrl(e.getUrl());
										return wu;
									}).collect(Collectors.toList()));
						}

						if (!pagesWithMatchingImagesList.isEmpty()) {
							web.setPagesWithMatchingImages(
									pagesWithMatchingImagesList.stream().map(e -> {
										WebUrl wu = new WebUrl();
										wu.setScore(e.getScore());
										wu.setUrl(e.getUrl());
										return wu;
									}).collect(Collectors.toList()));
						}

						if (!partialMatchingImagesList.isEmpty()) {
							web.setPartialMatchingImages(
									partialMatchingImagesList.stream().map(e -> {
										WebUrl wu = new WebUrl();
										wu.setScore(e.getScore());
										wu.setUrl(e.getUrl());
										return wu;
									}).collect(Collectors.toList()));
						}

						if (!webEntitiesList.isEmpty()) {
							web.setWebEntities(webEntitiesList.stream().map(e -> {
								ch.rasc.vision.entity.WebEntity we = new ch.rasc.vision.entity.WebEntity();
								we.setDescription(e.getDescription());
								we.setEntityId(e.getEntityId());
								we.setScore(e.getScore());
								return we;
							}).collect(Collectors.toList()));
						}

						result.setWeb(web);
					}
				}
			}

			return result;
		}
	}

	private Storage storage() throws IOException {
		ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(
				Files.newInputStream(Paths.get(this.appConfig.getCredentialsPath())));
		return StorageOptions.newBuilder().setCredentials(credentials).build()
				.getService();
	}

	private String storageBucket() {
		if (!StringUtils.hasText(this.appConfig.getStorageBucket())) {
			throw new IllegalStateException(
					"app.storage-bucket must be configured for signed uploads");
		}
		return this.appConfig.getStorageBucket();
	}

	private static String sanitizeFileName(String fileName) {
		if (!StringUtils.hasText(fileName)) {
			return "upload.bin";
		}

		String sanitized = fileName.replace('\\', '-').replace('/', '-')
				.replaceAll("[^A-Za-z0-9._-]", "-");
		return sanitized.isBlank() ? "upload.bin"
				: new String(sanitized.getBytes(StandardCharsets.UTF_8),
						StandardCharsets.UTF_8);
	}

	public record SignedUploadTarget(String uploadUrl, String objectName,
			String contentType) {
	}

	private static float likelihoodToNumber(Likelihood likelihood) {
		return switch (likelihood) {
		case VERY_UNLIKELY -> 0.2f;
		case UNLIKELY -> 0.4f;
		case POSSIBLE -> 0.6f;
		case LIKELY -> 0.8f;
		case VERY_LIKELY -> 1f;
		case UNKNOWN, UNRECOGNIZED -> 0f;
		};
	}

}
