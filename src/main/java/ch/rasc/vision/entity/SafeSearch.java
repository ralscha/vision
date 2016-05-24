package ch.rasc.vision.entity;

import ch.rasc.bsoncodec.annotation.BsonDocument;

@BsonDocument
public class SafeSearch {

	private String adult;
	private String spoof;
	private String medical;
	private String violence;
	private float adultRating;
	private float spoofRating;
	private float medicalRating;
	private float violenceRating;

	public String getAdult() {
		return this.adult;
	}

	public void setAdult(String adult) {
		this.adult = adult;
	}

	public String getSpoof() {
		return this.spoof;
	}

	public void setSpoof(String spoof) {
		this.spoof = spoof;
	}

	public String getMedical() {
		return this.medical;
	}

	public void setMedical(String medical) {
		this.medical = medical;
	}

	public String getViolence() {
		return this.violence;
	}

	public void setViolence(String violence) {
		this.violence = violence;
	}

	public float getAdultRating() {
		return this.adultRating;
	}

	public void setAdultRating(float adultRating) {
		this.adultRating = adultRating;
	}

	public float getSpoofRating() {
		return this.spoofRating;
	}

	public void setSpoofRating(float spoofRating) {
		this.spoofRating = spoofRating;
	}

	public float getMedicalRating() {
		return this.medicalRating;
	}

	public void setMedicalRating(float medicalRating) {
		this.medicalRating = medicalRating;
	}

	public float getViolenceRating() {
		return this.violenceRating;
	}

	public void setViolenceRating(float violenceRating) {
		this.violenceRating = violenceRating;
	}

}
