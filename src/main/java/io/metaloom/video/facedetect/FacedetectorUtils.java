package io.metaloom.video.facedetect;

import static io.metaloom.video4j.opencv.CVUtils.crop;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.imgscalr.Scalr;

public final class FacedetectorUtils {

	private FacedetectorUtils() {
	}

	public static List<Point> decropLandmarks(Face face, ArrayList<Point> facialLandmarks, int descaleFactor) {
		return facialLandmarks.stream().map(point -> {
			int x = face.start().x;
			int y = face.start().y;
			int xP = point.x;
			int yP = point.y;
			xP = xP / descaleFactor;
			yP = yP / descaleFactor;
			return new Point(x + xP, y + yP);
		}).collect(Collectors.toList());
		// List<Point> correctedPoints = new ArrayList<>();
		// for()
		// return correctedPoints;
	}

	public static BufferedImage cropToFace(Face face, BufferedImage img) {
		Dimension dim = face.dimension();
		int x = face.start().x;
		int y = face.start().y;
		int dimX = dim.width;
		int dimY = dim.height;

		// Clamp crop origin
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}

		// Clamp crop size
		if (x + dimX > img.getWidth()) {
			dimX = img.getWidth() - x;
		}
		if (y + dimY > img.getHeight()) {
			dimY = img.getHeight() - y;
		}
		return Scalr.crop(img, x, y, dimX, dimY);
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
		crop(frame, face.start(), face.dimension());
		return frame;
	}

	/**
	 * Crop the image to the face area.
	 * 
	 * @param face
	 *            Face to be used a source for the cropping area
	 * @param img
	 *            Image to be cropped
	 * @param marginPercent
	 *            Optional extra margin to be added to the crop. This can be useful in order to expand the crop area.
	 * @return Cropped image
	 */
	public static BufferedImage cropToFace(Face face, BufferedImage img, float marginPercent) {
		System.out.println();
		Dimension dim = face.dimension();
		int xExtra = (int) (marginPercent * dim.getWidth());
		int yExtra = (int) (marginPercent * dim.getHeight());
		int x = face.start().x;
		int y = face.start().y;
		int dimX = dim.width;
		int dimY = dim.height;

		// Apply extra spacing
		x = x - xExtra;
		y = y - yExtra;
		dimX = dimX + xExtra + xExtra;
		dimY = dimY + yExtra + yExtra;

		// Clamp crop origin
		if (x < 0) {
			x = 0;
		}
		if (y < 0) {
			y = 0;
		}

		// Clamp crop size
		if (x + dimX > img.getWidth()) {
			dimX = img.getWidth() - x;
		}
		if (y + dimY > img.getHeight()) {
			dimY = img.getHeight() - y;
		}

		return Scalr.crop(img, x, y, dimX, dimY);
	}

}
