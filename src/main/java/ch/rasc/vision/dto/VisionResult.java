package ch.rasc.vision.dto;

import java.util.List;

import ch.rasc.vision.entity.Face;
import ch.rasc.vision.entity.Label;
import ch.rasc.vision.entity.Landmark;
import ch.rasc.vision.entity.Logo;
import ch.rasc.vision.entity.SafeSearch;
import ch.rasc.vision.entity.Text;
import ch.rasc.vision.entity.Web;

public class VisionResult {

	private List<Label> labels;

	private SafeSearch safeSearch;

	private List<Logo> logos;

	private List<Landmark> landmarks;

	private List<Text> texts;

	private List<Face> faces;

	private Web web;

	public List<Label> getLabels() {
		return this.labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	public SafeSearch getSafeSearch() {
		return this.safeSearch;
	}

	public void setSafeSearch(SafeSearch safeSearch) {
		this.safeSearch = safeSearch;
	}

	public List<Logo> getLogos() {
		return this.logos;
	}

	public void setLogos(List<Logo> logos) {
		this.logos = logos;
	}

	public List<Landmark> getLandmarks() {
		return this.landmarks;
	}

	public void setLandmarks(List<Landmark> landmarks) {
		this.landmarks = landmarks;
	}

	public List<Text> getTexts() {
		return this.texts;
	}

	public void setTexts(List<Text> texts) {
		this.texts = texts;
	}

	public List<Face> getFaces() {
		return this.faces;
	}

	public void setFaces(List<Face> faces) {
		this.faces = faces;
	}

	public Web getWeb() {
		return this.web;
	}

	public void setWeb(Web web) {
		this.web = web;
	}

}
