package ch.rasc.vision.entity;

import java.util.List;

import ch.rasc.bsoncodec.annotation.BsonDocument;
import ch.rasc.extclassgenerator.Model;

@BsonDocument
@Model(value = "Vision.model.Face", extend = "Vision.model.Base")
public class Face {

	private Float rollAngle;
	private Float panAngle;
	private Float tiltAngle;
	private Float detectionConfidence;
	private Float landmarkingConfidence;

	private String joy;
	private String sorrow;
	private String anger;
	private String surprise;
	private String underExposed;
	private String blurred;
	private String headwear;

	private float joyRating;
	private float sorrowRating;
	private float angerRating;
	private float surpriseRating;
	private float underExposedRating;
	private float blurredRating;
	private float headwearRating;

	private List<Vertex> boundingPoly;
	private List<Vertex> fdBoundingPoly;
	private List<FaceLandmark> landmarks;

	public Float getRollAngle() {
		return this.rollAngle;
	}

	public void setRollAngle(Float rollAngle) {
		this.rollAngle = rollAngle;
	}

	public Float getPanAngle() {
		return this.panAngle;
	}

	public void setPanAngle(Float panAngle) {
		this.panAngle = panAngle;
	}

	public Float getTiltAngle() {
		return this.tiltAngle;
	}

	public void setTiltAngle(Float tiltAngle) {
		this.tiltAngle = tiltAngle;
	}

	public Float getDetectionConfidence() {
		return this.detectionConfidence;
	}

	public void setDetectionConfidence(Float detectionConfidence) {
		this.detectionConfidence = detectionConfidence;
	}

	public Float getLandmarkingConfidence() {
		return this.landmarkingConfidence;
	}

	public void setLandmarkingConfidence(Float landmarkingConfidence) {
		this.landmarkingConfidence = landmarkingConfidence;
	}

	public String getJoy() {
		return this.joy;
	}

	public void setJoy(String joy) {
		this.joy = joy;
	}

	public String getSorrow() {
		return this.sorrow;
	}

	public void setSorrow(String sorrow) {
		this.sorrow = sorrow;
	}

	public String getAnger() {
		return this.anger;
	}

	public void setAnger(String anger) {
		this.anger = anger;
	}

	public String getSurprise() {
		return this.surprise;
	}

	public void setSurprise(String surprise) {
		this.surprise = surprise;
	}

	public String getUnderExposed() {
		return this.underExposed;
	}

	public void setUnderExposed(String underExposed) {
		this.underExposed = underExposed;
	}

	public String getBlurred() {
		return this.blurred;
	}

	public void setBlurred(String blurred) {
		this.blurred = blurred;
	}

	public String getHeadwear() {
		return this.headwear;
	}

	public void setHeadwear(String headwear) {
		this.headwear = headwear;
	}

	public float getJoyRating() {
		return this.joyRating;
	}

	public void setJoyRating(float joyRating) {
		this.joyRating = joyRating;
	}

	public float getSorrowRating() {
		return this.sorrowRating;
	}

	public void setSorrowRating(float sorrowRating) {
		this.sorrowRating = sorrowRating;
	}

	public float getAngerRating() {
		return this.angerRating;
	}

	public void setAngerRating(float angerRating) {
		this.angerRating = angerRating;
	}

	public float getSurpriseRating() {
		return this.surpriseRating;
	}

	public void setSurpriseRating(float surpriseRating) {
		this.surpriseRating = surpriseRating;
	}

	public float getUnderExposedRating() {
		return this.underExposedRating;
	}

	public void setUnderExposedRating(float underExposedRating) {
		this.underExposedRating = underExposedRating;
	}

	public float getBlurredRating() {
		return this.blurredRating;
	}

	public void setBlurredRating(float blurredRating) {
		this.blurredRating = blurredRating;
	}

	public float getHeadwearRating() {
		return this.headwearRating;
	}

	public void setHeadwearRating(float headwearRating) {
		this.headwearRating = headwearRating;
	}

	public List<Vertex> getBoundingPoly() {
		return this.boundingPoly;
	}

	public void setBoundingPoly(List<Vertex> boundingPoly) {
		this.boundingPoly = boundingPoly;
	}

	public List<Vertex> getFdBoundingPoly() {
		return this.fdBoundingPoly;
	}

	public void setFdBoundingPoly(List<Vertex> fdBoundingPoly) {
		this.fdBoundingPoly = fdBoundingPoly;
	}

	public List<FaceLandmark> getLandmarks() {
		return this.landmarks;
	}

	public void setLandmarks(List<FaceLandmark> landmarks) {
		this.landmarks = landmarks;
	}

}
