package io.metaloom.video.facedetect;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.opencv.core.Scalar;

import io.metaloom.video.facedetect.dlib.impl.DLibFacedetector;
import io.metaloom.video.facedetect.opencv.CVFacedetector;
import io.metaloom.video4j.VideoFile;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class FacedetectPreviewUITest extends AbstractVideoTest {

	public static final String TEST_VIDEO = FEMALE_FACE_ROTATE;

	public static final double SEEK_TO = 0.05f;

	/**
	 * Define the minimum height for faces in the frame in percent of the total height of the frame.
	 */
	public static final float MIN_FACE_HEIGHT_THRESHOLD = 0.01f;

	private static final long FRAME_LIMIT = 55;

	@Test
	public void testDLIBCPU() throws FileNotFoundException {
		DLibFacedetector detector = DLibFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);
		detector.disableCNNDetector();
		detector.enableLandmarks();
		detector.enableLandmarks();

		runFaceDetect("dlib - HOG / resnet_v1 (CPU)", detector);

	}

	@Test
	public void testDLIBCNN() throws FileNotFoundException {
		DLibFacedetector detector = DLibFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);
		detector.enableEmbeddings();
		detector.enableLandmarks();
		detector.enableCNNDetector();

		runFaceDetect("dlib - mmod_human_face (GPU)", detector);
	}

	@Test
	public void testOpenCVHaarcascade() {
		CVFacedetector detector = CVFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);
		detector.loadHaarcascadeClassifier();
		detector.loadKazemiFacemarkModel();
		detector.enableLandmarks();

		runFaceDetect("OpenCV - Haarcascade / Kazemi", detector);

	}

	@Test
	public void testOpenCVLbpcascade() {
		CVFacedetector detector = CVFacedetector.create();
		detector.setMinFaceHeightFactor(MIN_FACE_HEIGHT_THRESHOLD);
		detector.loadLbpcascadeClassifier();
		detector.loadLBFLandmarkModel();

		runFaceDetect("OpenCV - lbpcascade / LBFLandmarkModel", detector);
	}

	// @Test
	// public void testZKeepOpen() throws IOException {
	// System.in.read();
	// }

	public static void runFaceDetect(String label, Facedetector detector) {
		FacedetectorMetrics metrics = FacedetectorMetrics.create();
		try (VideoFile video = Videos.open(TEST_VIDEO)) {
			video.seekToFrameRatio(SEEK_TO);
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				// .map(CVUtils::toGreyScale)
				.map(frame -> {
					CVUtils.boxFrame2(frame, 512);
					// CVUtils.boxFrame2(frame, 1024);
					return frame;
				})
				.map(detector::detect)
				// .filter(FaceVideoFrame::hasFace)
				.map(metrics::track)
				.map(detector::markFaces)
				.map(detector::markLandmarks)
				.map(frame -> {
					return CVUtils.drawText(frame, label, new org.opencv.core.Point(25, 25), 1.0f, new Scalar(255, 255, 255), 1);
				})
				.map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)))
				.limit(FRAME_LIMIT);
			// .map(frame -> {
			// return Facedetection.cropToFace(frame, 0);
			// });
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}

}
