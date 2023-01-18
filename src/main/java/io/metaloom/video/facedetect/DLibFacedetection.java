package io.metaloom.video.facedetect;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import io.metaloom.jdlib.Jdlib;
import io.metaloom.jdlib.util.FaceDescriptor;
import io.metaloom.video4j.VideoFrame;

public class DLibFacedetection extends Facedetection {

	public static final String FACIAL_LANDMARKS_MODEL_PATH = "dlib/shape_predictor_68_face_landmarks.dat";

	public static final String FACIAL_RESNET_MODEL_PATH = "dlib/dlib_face_recognition_resnet_model_v1.dat";

	public static final String CNN_FACE_DETECT_MODEL_PATH = "dlib/mmod_human_face_detector.dat";

	public static Jdlib jdlib;

	static {
		jdlib = new Jdlib(FACIAL_LANDMARKS_MODEL_PATH, FACIAL_RESNET_MODEL_PATH, CNN_FACE_DETECT_MODEL_PATH);
	}

	/**
	 * Scan the frame for faces and return a {@link FaceVideoFrame} which may contain information on found faces.
	 * 
	 * @param frame
	 * @return
	 */
	public static FaceVideoFrame scan(VideoFrame frame) {
		return scan(frame, 0, true, false);
	}

	/**
	 * Scan the frame for faces and return a {@link FaceVideoFrame} which may contain information on the found faces.
	 * 
	 * @param frame
	 * @param minFaceHeightFactor
	 *            Minimum face height threshold factor
	 * @param useCNN
	 * @param loadEmbeddings
	 * @return
	 */
	public static FaceVideoFrame scan(VideoFrame frame, float minFaceHeightFactor, boolean useCNN, boolean loadEmbeddings) {

		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);

		// Store found face area
		BufferedImage img = frame.toImage();

		int absoluteFaceHeightThreshold = 0;
		if (minFaceHeightFactor != 0) {
			// Compute minimum face size
			int height = img.getHeight();
			if (Math.round(height * minFaceHeightFactor) > 0) {
				absoluteFaceHeightThreshold = Math.round(height * minFaceHeightFactor);
			}
		}

		// Detect faces
		List<Face> faces = new ArrayList<>();
		if (useCNN) {
			List<Rectangle> faceRects = jdlib.cnnDetectFace(img);
			for (Rectangle rect : faceRects) {
				int x = rect.x;
				int y = rect.y;
				int width = rect.width;
				int height = rect.height;

				// Check if the found face is too small and does not meet the face height threshold.
				if (height < absoluteFaceHeightThreshold) {
					faces.add(null);
				} else {
					Face face = faceFrame.addFace(x, y, width, height);
					faces.add(face);
					// face.setLandmarks(faceDesc.getFacialLandmarks());
				}
			}
		} else {
			List<FaceDescriptor> faceDescriptors = jdlib.getFaceLandmarks(img);
			for (FaceDescriptor faceDesc : faceDescriptors) {
				int x = faceDesc.getFaceBox().x;
				int y = faceDesc.getFaceBox().y;
				int width = faceDesc.getFaceBox().width;
				int height = faceDesc.getFaceBox().height;

				// Check if the found face is too small and does not meet the face height threshold.
				if (height < absoluteFaceHeightThreshold) {
					faces.add(null);
				} else {
					Face face = faceFrame.addFace(x, y, width, height);
					faces.add(face);
					face.setLandmarks(faceDesc.getFacialLandmarks());
				}
			}
		}

		if (loadEmbeddings) {
			// No need to check for embeddings if we did not find any landmarks
			if (!faces.isEmpty()) {
				List<FaceDescriptor> faceDescriptors = jdlib.getFaceEmbeddings(img);
				int i = 0;
				for (FaceDescriptor faceDesc : faceDescriptors) {
					Face face = faces.get(i);
					if (face != null) {
						face.setLandmarks(faceDesc.getFacialLandmarks());
						face.setEmbeddings(faceDesc.getFaceEmbedding());
					}
					i++;
				}
			}
		}
		return faceFrame;
	}

}
