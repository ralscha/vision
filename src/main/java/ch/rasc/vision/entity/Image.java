package ch.rasc.vision.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import ch.rasc.extclassgenerator.Model;

@Model(value = "Vision.model.Image", readMethod = "imageController.read",
		createMethod = "imageController.update", updateMethod = "imageController.update",
		rootProperty = "records", identifier = "negative", extend = "Vision.model.Base",
		hasMany = { "Label", "Logo", "Landmark", "Text", "Face" }, writeAllFields = false)
@JsonInclude(Include.NON_NULL)
public class Image {

	private long id;

	private String name;

	private List<Label> labels;

	private List<Logo> logos;

	private List<Landmark> landmarks;

	private List<Text> texts;

	private List<Face> faces;

	private SafeSearch safeSearch;

	private Web web;

	private String type;

	private long size;

	private String data;

	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Label> getLabels() {
		return this.labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getSize() {
		return this.size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
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
