package io.metaloom.video.facedetect.impl;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import io.metaloom.video.facedetect.Face;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;

public class FaceVideoFrameImpl implements FaceVideoFrame {

	private VideoFrame delegate;

	private final List<Face> faces = new ArrayList<>();

	public FaceVideoFrameImpl(VideoFrame frame) {
		this.delegate = frame;
	}

	@Override
	public long number() {
		return delegate.number();
	}

	@Override
	public Mat mat() {
		return delegate.mat();
	}

	@Override
	public BufferedImage toImage() {
		return delegate.toImage();
	}

	@Override
	public Video origin() {
		return delegate.origin();
	}

	@Override
	public void setMat(Mat frame) {
		delegate.setMat(frame);
	}

	@Override
	public <T> T getMeta() {
		return delegate.getMeta();
	}

	@Override
	public <T> void setMeta(T meta) {
		delegate.setMeta(meta);
	}

	@Override
	public void close() throws Exception {
		delegate.close();
	}

	@Override
	public Face addFace(int x, int y, int width, int height) {
		Face face = new FaceImpl(x, y, width, height);
		faces.add(face);
		return face;
	}

	@Override
	public List<Face> faces() {
		return faces;
	}

	@Override
	public boolean hasFace() {
		return !faces.isEmpty();
	}

}
