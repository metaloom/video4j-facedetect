package io.metaloom.video.facedetect.dlib.impl;

import static io.metaloom.video.facedetect.FacedetectorUtils.cropToFace;
import static io.metaloom.video.facedetect.FacedetectorUtils.decropLandmarks;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.imgscalr.Scalr;

import io.metaloom.jdlib.Jdlib;
import io.metaloom.jdlib.util.FaceDescriptor;
import io.metaloom.jdlib.util.ImageUtils;
import io.metaloom.video.facedetect.AbstractFacedetector;
import io.metaloom.video.facedetect.Face;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video4j.VideoFrame;

public class DLibFacedetectorImpl extends AbstractFacedetector implements DLibFacedetector {

	public static final String FACIAL_LANDMARKS_MODEL_PATH = "dlib/shape_predictor_68_face_landmarks.dat";

	public static final String FACIAL_RESNET_MODEL_PATH = "dlib/dlib_face_recognition_resnet_model_v1.dat";

	public static final String CNN_FACE_DETECT_MODEL_PATH = "dlib/mmod_human_face_detector.dat";

	private boolean useCNN = true;

	public Jdlib jdlib;

	/**
	 * Create a new dlib facedetector and use default paths for detection model files.
	 * 
	 * @throws FileNotFoundException
	 */
	public DLibFacedetectorImpl() throws FileNotFoundException {
		this(FACIAL_LANDMARKS_MODEL_PATH, FACIAL_RESNET_MODEL_PATH, CNN_FACE_DETECT_MODEL_PATH);
	}

	/**
	 * Create a new dlib facedetector.
	 * 
	 * @param landmarksModelPath
	 * @param facialResnetModelPath
	 * @param cnnDetectModelPath
	 * @throws FileNotFoundException
	 */
	public DLibFacedetectorImpl(String landmarksModelPath, String facialResnetModelPath, String cnnDetectModelPath) throws FileNotFoundException {
		if (!Files.exists(Paths.get(landmarksModelPath))) {
			throw new FileNotFoundException("landmarks model file could not be found at " + landmarksModelPath);
		}
		if (!Files.exists(Paths.get(facialResnetModelPath))) {
			throw new FileNotFoundException("facial resnet model file could not be found at " + facialResnetModelPath);
		}
		if (!Files.exists(Paths.get(cnnDetectModelPath))) {
			throw new FileNotFoundException("CNN dector model file could not be found at " + cnnDetectModelPath);
		}
		jdlib = new Jdlib(landmarksModelPath, facialResnetModelPath, cnnDetectModelPath);
	}

	@Override
	public void enableCNNDetector() {
		useCNN = true;
	}

	@Override
	public void disableCNNDetector() {
		useCNN = false;
	}

	@Override
	public boolean isCNNUsageEnabled() {
		return useCNN;
	}

	@Override
	public FaceVideoFrame detect(VideoFrame frame) {
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		BufferedImage img = frame.toImage();
		faceFrame.setFaces(detect(img));
		return faceFrame;
	}

	@Override
	public List<? extends Face> detect(BufferedImage img) {
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
				if (height > absoluteFaceHeightThreshold) {
					Face face = Face.create(x, y, width, height);
					faces.add(face);
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
					Face face = Face.create(x, y, width, height);
					face.setLandmarks(faceDesc.getFacialLandmarks());
					faces.add(face);
				}
			}
		}

		if (isLoadEmbeddings()) {
			for (Face face : faces) {
				//float marginPercent = 0.15f;
				float marginPercent = 0;
				BufferedImage croppedImg = cropToFace(face, img, marginPercent);
				int scaleFactor = 1;
				BufferedImage enhancedCroppedImage = croppedImg;
				if (scaleFactor > 1) {
					int w = croppedImg.getWidth() * scaleFactor;
					int h = croppedImg.getHeight() * scaleFactor;
					enhancedCroppedImage = Scalr.resize(croppedImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, w, h,
						Scalr.OP_ANTIALIAS);
				}

				BufferedImage convertedImg = alignImageType(enhancedCroppedImage);
				img.flush();
				croppedImg.flush();
				enhancedCroppedImage.flush();
				// try {
				// System.in.read();
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// No need to check for embeddings if we did not find any landmarks
				// if (!faces.isEmpty()) {
				List<FaceDescriptor> faceDescriptors = jdlib.getFaceEmbeddings(convertedImg);
//				if (!faceDescriptors.isEmpty()) {
//					ImageUtils.showImage(convertedImg);
//				}
				for (FaceDescriptor faceDesc : faceDescriptors) {
					// Rectangle box = faceDesc.getFaceBox();
					// int x = box.x;
					// int y = box.y;
					// int width = box.width;
					// int height = box.height;
					// Face face = faceFrame.addFace(x, y, width, height);
					// faces.add(face);
					// if (faces.size() > i) {
					// Face face = faces.get(i);
					// if (face != null) {
					face.setLandmarks(decropLandmarks(face, faceDesc.getFacialLandmarks(), scaleFactor));
					// face.setLandmarks(faceDesc.getFacialLandmarks());
					face.setEmbeddings(faceDesc.getFaceEmbedding());
					// }
					// }
				}
				// }
			}

		}
		return faces;
	}

	private static BufferedImage alignImageType(BufferedImage img) {
		BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		convertedImg.getGraphics().drawImage(img, 0, 0, null);
		return convertedImg;
	}

}
