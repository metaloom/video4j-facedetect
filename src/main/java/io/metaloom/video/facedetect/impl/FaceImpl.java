package io.metaloom.video.facedetect.impl;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import io.metaloom.video.facedetect.Face;

public class FaceImpl implements Face {

	private final Rectangle box;
	private final int width;
	private final int height;

	private final List<Point> landmarks = new ArrayList<>();

	private float[] embeddings = null;

	public FaceImpl(Rectangle box, int width, int height) {
		this.box = box;
		this.width = width;
		this.height = height;
	}

	@Override
	public Point start() {
		return box.getLocation();
	}

	@Override
	public Point start(int imageWidth, int imageHeight) {
		float startXFactor = (float) box.x / (float) this.width;
		float startYFactor = (float) box.y / (float) this.height;
		int x = (int) (startXFactor * imageWidth);
		int y = (int) (startYFactor * imageHeight);
		return new Point(x, y);

	}

	@Override
	public Dimension dimension() {
		return new Dimension(box.width, box.height);
	}

	@Override
	public Dimension dimension(int imageWidth, int imageHeight) {
		float boxWidthFactor = (float) box.width / (float) this.width;
		float boxHeightFactor = (float) box.height / (float) this.height;
		int w = (int) (boxWidthFactor * imageWidth);
		int h = (int) (boxHeightFactor * imageHeight);
		return new Dimension(w, h);
	}

	@Override
	public void setLandmarks(List<Point> facialLandmarks) {
		this.landmarks.addAll(facialLandmarks);
	}

	@Override
	public List<Point> getLandmarks() {
		return landmarks;
	}

	@Override
	public void setEmbeddings(float[] faceEmbeddings) {
		this.embeddings = faceEmbeddings;
	}

	@Override
	public float[] getEmbeddings() {
		return embeddings;
	}

	@Override
	public boolean hasLandmarks() {
		return !getLandmarks().isEmpty();
	}

	@Override
	public boolean hasEmbedding() {
		return embeddings != null;
	}

	@Override
	public Rectangle box() {
		return new Rectangle(start(), dimension());
	}

	@Override
	public String toString() {
		return "Face at " + box.x + ":" + box.y + " (" + box.width + " x " + box.height + ") landmarks: "
			+ (landmarks == null ? "null" : landmarks.size())
			+ ", embeddings: " + (embeddings == null ? "null" : embeddings.length);
	}
}
