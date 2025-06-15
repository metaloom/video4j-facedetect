package io.metaloom.video.facedetect.opencv.impl;

import java.awt.Dimension;
import java.awt.Point;
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
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.face.Face;
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

	@Override
	public FaceVideoFrame detectFaces(VideoFrame frame) {
		Mat matFrame = frame.mat();
		// Construct a new face frame which contains the detection data
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		faceFrame.setFaces(detectFaces(matFrame));
		return faceFrame;
	}

	@Override
	public List<? extends Face> detectFaces(BufferedImage img) {
		Mat mat = MatProvider.mat();
		CVUtils.bufferedImageToMat(img, mat);
		List<? extends Face> faces = detectFaces(mat);
		MatProvider.released(mat);
		return faces;
	}

	@Override
	public List<? extends Face> detectFaces(Mat imageMat) {

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
			Face face = Face.create(CVUtils.toRectangle(rect));
			faces.add(face);
		}

		return faces;
	}

	@Override
	public FaceVideoFrame detectLandmarks(VideoFrame frame) {
		Mat imageMat = frame.mat();

		MatOfRect faceDetections = new MatOfRect();
		List<MatOfPoint2f> landmarks = new ArrayList<>();
		List<Face> faces = new ArrayList<>();
		if (FACEMARK.fit(imageMat, faceDetections, landmarks)) {

			for (Rect rect : faceDetections.toArray()) {
				Face face = Face.create(CVUtils.toRectangle(rect));
				faces.add(face);
			}
			for (int i = 0; i < landmarks.size(); i++) {
				MatOfPoint2f landmark = landmarks.get(i);
				// Face face = Face.create();
				// Transform point type of list
				faces.get(i)
					.setLandmarks(landmark.toList()
						.stream()
						.map(CVUtils::toAWTPoint)
						.collect(Collectors.toList()));
			}
		}
		return FaceVideoFrame.from(frame)
			.setFaces(faces);
	}

	@Override
	public FaceVideoFrame detectLandmarks(FaceVideoFrame frame) {
		// No need to search for landmarks if we did not find faces.
		if (!frame.hasFaces()) {
			return frame;
		}
		Mat imageMat = frame.mat();
		List<? extends Face> faces = frame.faces();
		Rect[] rects = faces.stream()
			.map(this::toRect)
			.toArray(Rect[]::new);

		MatOfRect faceDetections = new MatOfRect(rects);
		List<MatOfPoint2f> landmarks = new ArrayList<>();
		if (FACEMARK.fit(imageMat, faceDetections, landmarks)) {
			for (int i = 0; i < landmarks.size(); i++) {
				Face face = faces.get(i);
				MatOfPoint2f landmark = landmarks.get(i);
				// Transform point type of list
				face.setLandmarks(landmark.toList()
					.stream()
					.map(CVUtils::toAWTPoint)
					.collect(Collectors.toList()));
			}
		}
		return frame;
	}

	@Override
	public FaceVideoFrame detectEmbeddings(VideoFrame frame) {
		throw new UnsupportedOperationException("The OpenCV implementation currently does not support embeddings");
	}

	@Override
	public FaceVideoFrame extractEmbeddings(FaceVideoFrame frame) {
		throw new UnsupportedOperationException("The OpenCV implementation currently does not support embeddings");
	}

	private Rect toRect(Face face) {
		Point start = face.start();
		Dimension dim = face.dimension();
		return new Rect(start.x, start.y, dim.width, dim.height);
	}
}
