package io.metaloom.video.facedetect;

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

import io.metaloom.video4j.VideoFrame;

public class CVFacedetection extends Facedetection {

	public static final CascadeClassifier FACE_DETECTOR = new CascadeClassifier();
	public static Facemark FACEMARK;

	public static void loadHaarcascade() {
		String profileXML = "src/main/resources/haarcascade_frontalface_alt.xml";
		loadProfile(profileXML);
	}

	public static void loadKazemiLandmarkModel() {
		FACEMARK = org.opencv.face.Face.createFacemarkKazemi();
		FACEMARK.loadModel("opencv/face_landmark_model.dat");
	}

	public static void loadLBFLandmarkModel() {
		FACEMARK = org.opencv.face.Face.createFacemarkLBF();
		FACEMARK.loadModel("opencv/lbfmodel.yaml");
	}

	public static void loadAAMLandmarkModel() {
		FACEMARK = org.opencv.face.Face.createFacemarkAAM();
		FACEMARK.loadModel("opencv/lbfmodel.yaml");
	}

	public static void loadLbpcascade() {
		String profileXML = "src/main/resources/lbpcascade_frontalface_improved.xml";
		loadProfile(profileXML);
	}

	private static void loadProfile(String profileXML) {
		if (!FACE_DETECTOR.load(profileXML)) {
			throw new RuntimeException("Could not load " + profileXML);
		}
	}

	/**
	 * Scan the frame for faces and return a {@link FaceVideoFrame} which may contain information on found faces.
	 * 
	 * @param frame
	 * @param minFaceSize
	 *            Factor of the minimum face height in pixel compared to the frame height (0.2f = 20%), 0 disables the check.
	 * @param loadLandmarks
	 * @return
	 */
	public static FaceVideoFrame scan(VideoFrame frame, float minFaceSize, boolean loadLandmarks) {
		Mat matFrame = frame.mat();

		// Detecting faces
		MatOfRect faceDetections = new MatOfRect();

		// Detect faces
		if (minFaceSize != 0) {
			int absoluteFaceSize = 0;
			// Compute minimum face size
			int height = matFrame.rows();
			if (Math.round(height * minFaceSize) > 0) {
				absoluteFaceSize = Math.round(height * minFaceSize);
			}

			FACE_DETECTOR.detectMultiScale(matFrame, faceDetections, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(absoluteFaceSize, absoluteFaceSize), new Size());
		} else {
			FACE_DETECTOR.detectMultiScale(matFrame, faceDetections);
		}

		// Construct a new face frame which contains the detection data
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);

		// Store found face area
		List<Face> faces = new ArrayList<>();
		for (Rect rect : faceDetections.toArray()) {
			Face face = faceFrame.addFace(rect.x, rect.y, rect.width, rect.height);
			faces.add(face);
		}

		// No need to search for landmarks if we did not find faces.
		if (!faces.isEmpty() && loadLandmarks) {
			List<MatOfPoint2f> landmarks = new ArrayList<>();
			if (FACEMARK.fit(matFrame, faceDetections, landmarks)) {
				for (int i = 0; i < landmarks.size(); i++) {
					Face face = faces.get(i);
					MatOfPoint2f landmark = landmarks.get(i);
					// Transform point type of list
					face.setLandmarks(landmark.toList().stream().map(Facedetection::toAWTPoint).collect(Collectors.toList()));
				}
			}
		}

		return faceFrame;
	}

}
