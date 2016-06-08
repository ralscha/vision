package ch.rasc.vision.entity;

import ch.rasc.bsoncodec.annotation.BsonDocument;

@BsonDocument
public class Vertex {
	private Integer x;
	private Integer y;
	public Integer getX() {
		return x;
	}
	public void setX(Integer x) {
		this.x = x;
	}
	public Integer getY() {
		return y;
	}
	public void setY(Integer y) {
		this.y = y;
	}



}
