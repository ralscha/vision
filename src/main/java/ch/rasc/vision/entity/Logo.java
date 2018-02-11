package ch.rasc.vision.entity;

import java.util.List;

import ch.rasc.extclassgenerator.Model;

@Model(value = "Vision.model.Logo", extend = "Vision.model.Base")
public class Logo {

	private String description;

	private Float score;

	private List<Vertex> boundingPoly;

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Float getScore() {
		return this.score;
	}

	public void setScore(Float score) {
		this.score = score;
	}

	public List<Vertex> getBoundingPoly() {
		return this.boundingPoly;
	}

	public void setBoundingPoly(List<Vertex> boundingPoly) {
		this.boundingPoly = boundingPoly;
	}

}
