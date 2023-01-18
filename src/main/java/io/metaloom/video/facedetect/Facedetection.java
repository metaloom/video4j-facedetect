package io.metaloom.video.facedetect;

import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import io.metaloom.video4j.opencv.CVUtils;

public class Facedetection {

	/**
	 * Add markers for each landmarks of all found faces in the frame.
	 * 
	 * @param frame
	 * @return
	 */
	public static FaceVideoFrame markLandmarks(FaceVideoFrame frame) {
		for (Face face : frame.faces()) {
			for (java.awt.Point point : face.getLandmarks()) {
				Imgproc.drawMarker(frame.mat(), Facedetection.toCVPoint(point), new Scalar(0, 0, 128), 0, 15);
			}
		}
		return frame;
	}

	public static FaceVideoFrame drawMetrics(FaceVideoFrame frame, FacedetectionMetrics metrics, java.awt.Point position) {

		Scalar color = new Scalar(255, 255, 255);
		// int font= Imgproc.FONT_HERSHEY_PLAIN;
		double fontScale = 1.0f;
		String text = "Metrics unavailable";
		if (metrics != null) {
			text = metrics.toString();
		}
		// Imgproc.putText(frame.mat(), "ABC", upperLeft, font, fontScale, color);
		CVUtils.drawText(frame, text, new Point(position.x, position.y), fontScale, color, 1);
		return frame;
	}

	/**
	 * Returns a cropped frame for the given face.
	 * 
	 * @param frame
	 * @param faceIndex
	 * @return Cropped frame or original frame if no cropping could be applied
	 */
	public static FaceVideoFrame cropToFace(FaceVideoFrame frame, int faceIndex) {
		if (frame.faces().size() <= faceIndex) {
			return frame;
		}
		Face face = frame.faces().get(faceIndex);
		CVUtils.crop(frame, face.start(), face.dimension());
		return frame;
	}

	public static Point toCVPoint(java.awt.Point awtPoint) {
		return new Point(awtPoint.getX(), awtPoint.getY());
	}

	public static java.awt.Point toAWTPoint(Point cvPoint) {
		return new java.awt.Point((int) cvPoint.x, (int) cvPoint.y);
	}

	public static FaceVideoFrame markFaces(FaceVideoFrame frame) {
		for (Face face : frame.faces()) {
			Point cvStart = toCVPoint(face.start());
			Point cvEnd = toCVPoint(face.end());
			Imgproc.rectangle(frame.mat(), cvStart, cvEnd, new Scalar(0, 255, 0));
		}
		return frame;
	}

}
