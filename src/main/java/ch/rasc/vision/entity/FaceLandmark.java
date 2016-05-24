package ch.rasc.vision.entity;

import ch.rasc.bsoncodec.annotation.BsonDocument;

@BsonDocument
public class FaceLandmark {
	private String type;
	private float x;
	private float y;
	private float z;

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public float getX() {
		return this.x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return this.y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return this.z;
	}

	public void setZ(float z) {
		this.z = z;
	}

}
