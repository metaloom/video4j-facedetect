package io.metaloom.video.facedetect;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;

public interface Face {

	/**
	 * Return the upper-left start point of the rectangle which contains the found face.
	 * 
	 * @return
	 */
	Point start();

	/**
	 * Return the lower-right end point of the rectangle which contains the found face.
	 * 
	 * @return
	 */
	Point end();

	/**
	 * Return the pixel size of the detected face area.
	 * 
	 * @return
	 */
	Dimension dimension();

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

}
