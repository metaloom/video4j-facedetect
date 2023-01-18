package io.metaloom.video.facedetect;

import java.util.List;

import io.metaloom.video.facedetect.impl.FaceVideoFrameImpl;
import io.metaloom.video4j.VideoFrame;

public interface FaceVideoFrame extends VideoFrame {

	static FaceVideoFrame from(VideoFrame frame) {
		return new FaceVideoFrameImpl(frame);
	}

	/**
	 * Add an area of a face to the list of faces.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @return added face
	 */
	Face addFace(int x, int y, int width, int height);

	/**
	 * Return a set of found faces.
	 * 
	 * @return
	 */
	List<Face> faces();

	/**
	 * Check whether the frame contains detected faces.
	 * 
	 * @return
	 */
	boolean hasFace();
}
