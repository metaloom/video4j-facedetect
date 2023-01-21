package io.metaloom.video.facedetect;

import static io.metaloom.video4j.opencv.CVUtils.drawText;
import static io.metaloom.video4j.opencv.CVUtils.toCVPoint;

import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public abstract class AbstractFacedetector implements Facedetector {

	private static final float DEFAULT_MIN_FACE_HEIGHT_FACTOR = 0;

	protected float minFaceHeightFactor = DEFAULT_MIN_FACE_HEIGHT_FACTOR;

	private boolean loadEmbeddings = true;

	private boolean loadLandmarks = true;

	@Override
	public float getMinFaceHeightFactor() {
		return minFaceHeightFactor;
	}

	@Override
	public void setMinFaceHeightFactor(float factor) {
		this.minFaceHeightFactor = factor;
	}

	@Override
	public void enableEmbeddings() {
		loadEmbeddings = true;
	}

	@Override
	public void disableEmbeddings() {
		loadEmbeddings = false;
	}

	@Override
	public boolean isLoadEmbeddings() {
		return loadEmbeddings;
	}

	@Override
	public boolean isLandmarksEnabled() {
		return loadLandmarks;
	}

	@Override
	public void enableLandmarks() {
		loadLandmarks = true;

	}

	@Override
	public void disableLandmarks() {
		loadLandmarks = false;
	}


	@Override
	public FaceVideoFrame markLandmarks(FaceVideoFrame frame) {
		for (Face face : frame.faces()) {
			for (java.awt.Point point : face.getLandmarks()) {
				Imgproc.drawMarker(frame.mat(), toCVPoint(point), new Scalar(0, 0, 128), 0, 15);
			}
		}
		return frame;
	}

	@Override
	public FaceVideoFrame drawMetrics(FaceVideoFrame frame, FacedetectorMetrics metrics, java.awt.Point position) {

		Scalar color = new Scalar(255, 255, 255);
		// int font= Imgproc.FONT_HERSHEY_PLAIN;
		double fontScale = 1.0f;
		String text = "Metrics unavailable";
		if (metrics != null) {
			text = metrics.toString();
		}
		// Imgproc.putText(frame.mat(), "ABC", upperLeft, font, fontScale, color);
		drawText(frame, text, new Point(position.x, position.y), fontScale, color, 1);
		return frame;
	}

	@Override
	public FaceVideoFrame markFaces(FaceVideoFrame frame) {
		for (Face face : frame.faces()) {
			Point cvStart = toCVPoint(face.start());
			Point cvEnd = toCVPoint(face.end());
			Imgproc.rectangle(frame.mat(), cvStart, cvEnd, new Scalar(0, 255, 0));
		}
		return frame;
	}

}
