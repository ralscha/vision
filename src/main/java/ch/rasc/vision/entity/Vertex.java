package ch.rasc.vision.entity;

import ch.rasc.bsoncodec.annotation.BsonDocument;

@BsonDocument
public class Vertex {
	private int x;
	private int y;

	public int getX() {
		return this.x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int y) {
		this.y = y;
	}

}
