package ch.rasc.vision.entity;

import java.util.List;

public class Text {

	private String description;

	private List<Vertex> boundingPoly;

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Vertex> getBoundingPoly() {
		return this.boundingPoly;
	}

	public void setBoundingPoly(List<Vertex> boundingPoly) {
		this.boundingPoly = boundingPoly;
	}

}
