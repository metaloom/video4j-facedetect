package io.metaloom.video.facedetect.opencv.impl;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.face.Facemark;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import io.metaloom.video.facedetect.AbstractFacedetector;
import io.metaloom.video.facedetect.Face;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.opencv.CVFacedetector;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.opencv.CVUtils;

public class CVFacedetectorImpl extends AbstractFacedetector implements CVFacedetector {

	public static final CascadeClassifier FACE_DETECTOR = new CascadeClassifier();
	public static Facemark FACEMARK;

	public CVFacedetectorImpl() {
	}

	@Override
	public void loadHaarcascadeClassifier() {
		String profileXML = "src/main/resources/haarcascade_frontalface_alt.xml";
		loadClassifierProfile(profileXML);
	}

	@Override
	public void loadLbpcascadeClassifier() {
		String profileXML = "src/main/resources/lbpcascade_frontalface_improved.xml";
		loadClassifierProfile(profileXML);
	}

	@Override
	public void loadClassifierProfile(String profileXML) {
		if (!FACE_DETECTOR.load(profileXML)) {
			throw new RuntimeException("Could not load " + profileXML);
		}
	}

	@Override
	public void loadKazemiFacemarkModel() {
		loadKazemiFacemarkModel("opencv/face_landmark_model.dat");
	}

	@Override
	public void loadKazemiFacemarkModel(String path) {
		FACEMARK = org.opencv.face.Face.createFacemarkKazemi();
		FACEMARK.loadModel(path);
	}

	@Override
	public void loadLBFLandmarkModel() {
		loadLBFFacemarkModel("opencv/lbfmodel.yaml");
	}

	public void loadLBFFacemarkModel(String path) {
		FACEMARK = org.opencv.face.Face.createFacemarkLBF();
		FACEMARK.loadModel(path);
	}

	@Override
	public void loadAAMLandmarkModel() {
		loadAAMFacemarkModel("opencv/lbfmodel.yaml");
	}

	@Override
	public void loadAAMFacemarkModel(String path) {
		FACEMARK = org.opencv.face.Face.createFacemarkAAM();
		FACEMARK.loadModel(path);
	}

	/**
	 * Scan the frame for faces and return a {@link FaceVideoFrame} which may contain information on found faces.
	 * 
	 * @param frame
	 * @return
	 */
	public FaceVideoFrame detect(VideoFrame frame) {
		Mat matFrame = frame.mat();
		// Construct a new face frame which contains the detection data
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		faceFrame.setFaces(detect(matFrame));
		return faceFrame;
	}

	@Override
	public List<? extends Face> detect(BufferedImage img) {
		Mat mat = MatProvider.mat();
		CVUtils.bufferedImageToMat(img, mat);
		List<? extends Face> faces = detect(mat);
		MatProvider.released(mat);
		return faces;
	}

	@Override
	public List<? extends Face> detect(Mat imageMat) {

		// Detecting faces
		MatOfRect faceDetections = new MatOfRect();
		List<Face> faces = new ArrayList<>();

		// Detect faces
		if (minFaceHeightFactor != 0) {
			int absoluteFaceSize = 0;
			// Compute minimum face size
			int height = imageMat.rows();
			if (Math.round(height * minFaceHeightFactor) > 0) {
				absoluteFaceSize = Math.round(height * minFaceHeightFactor);
			}

			FACE_DETECTOR.detectMultiScale(imageMat, faceDetections, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(absoluteFaceSize, absoluteFaceSize), new Size());
		} else {
			FACE_DETECTOR.detectMultiScale(imageMat, faceDetections);
		}

		// Store found face area
		for (Rect rect : faceDetections.toArray()) {
			Face face = Face.create(rect.x, rect.y, rect.width, rect.height);
			faces.add(face);
		}

		// No need to search for landmarks if we did not find faces.
		if (!faces.isEmpty() && isLandmarksEnabled()) {
			List<MatOfPoint2f> landmarks = new ArrayList<>();
			if (FACEMARK.fit(imageMat, faceDetections, landmarks)) {
				for (int i = 0; i < landmarks.size(); i++) {
					Face face = faces.get(i);
					MatOfPoint2f landmark = landmarks.get(i);
					// Transform point type of list
					face.setLandmarks(landmark.toList().stream().map(CVUtils::toAWTPoint).collect(Collectors.toList()));
				}
			}
		}
		return faces;
	}

	@Override
	public void enableEmbeddings() {
		throw new UnsupportedOperationException("The OpenCV implementation currently does not support embeddings");
	}

	@Override
	public boolean isLoadEmbeddings() {
		return false;
	}

}
