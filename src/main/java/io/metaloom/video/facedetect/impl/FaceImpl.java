package io.metaloom.video.facedetect.impl;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import io.metaloom.video.facedetect.Face;

public class FaceImpl implements Face {

	private final int x;
	private final int y;
	private final int width;
	private final int height;

	private final List<Point> landmarks = new ArrayList<>();

	private float[] embeddings;

	public FaceImpl(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public Point start() {
		return new Point(x, y);
	}

	@Override
	public Point end() {
		return new Point(width + x, height + y);
	}

	@Override
	public Dimension dimension() {
		return new Dimension(width, height);
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
	public String toString() {
		return "Face at " + x + ":" + y + " (" + width + " x " + height + ") landmarks: " + (landmarks == null ? "null" : landmarks.size())
			+ ", embeddings: " + (embeddings == null ? "null" : embeddings.length);
	}
}
