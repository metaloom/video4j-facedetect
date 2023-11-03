package io.metaloom.video.facedetect;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import io.metaloom.video.facedetect.impl.FaceImpl;

/**
 * A {@link Face} contains information about a detected face. This can include area of detection, facial landmarks and retrieved embeddings of the face.
 */
public interface Face {

	static Face create(Rectangle box, int imageWidth, int imageHeight) {
		return new FaceImpl(box, imageWidth, imageHeight);
	}

	/**
	 * Return the upper-left start point of the rectangle which contains the found face.
	 * 
	 * @return
	 */
	Point start();

	/**
	 * Return the upper-left start point relative to the provided image format.
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @return
	 */
	Point start(int imageWidth, int imageHeight);

	/**
	 * Return the pixel size of the detected face area.
	 * 
	 * @return
	 */
	Dimension dimension();

	/**
	 * Return dimension relative to the given image format.
	 * 
	 * @param imageWidth
	 * @param imageHeight
	 * @return
	 */
	Dimension dimension(int imageWidth, int imageHeight);

	Rectangle box();

	/**
	 * Set the landmarks for the face.
	 * 
	 * @param facialLandmarks
	 */
	void setLandmarks(List<Point> facialLandmarks);

	/**
	 * Return the list of landmarks.
	 * 
	 * @return
	 */
	List<Point> getLandmarks();

	/**
	 * Set the embeddings for the face.
	 * 
	 * @param faceEmbeddings
	 */
	void setEmbeddings(float[] faceEmbeddings);

	/**
	 * Return the embeddings for the face.
	 * 
	 * @return
	 */
	float[] getEmbeddings();

	/**
	 * Check whether an embedding has been stored with the face.
	 * 
	 * @return
	 */
	boolean hasLandmarks();

	boolean hasEmbedding();

}
