package io.metaloom.video.facedetect;

import java.awt.Point;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opencv.core.Scalar;

import io.metaloom.video4j.Video;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FacedetectPreviewUITest extends AbstractVideoTest {

	public static final boolean USE_CNN_FACE_DETECT = true;

	public static final boolean CPU_FACE_DETECT = false;

	public static final boolean DONT_LOAD_EMBEDDINGS = false;

	public static final boolean LOAD_EMBEDDINGS = true;

	public static final boolean LOAD_LANDMARKS = true;

	public static final String TEST_VIDEO = FEMALE_FACE_ROTATE;

	public static final double SEEK_TO = 0.05f;

	/**
	 * Define the minimum height for faces in the frame in percent of the total height of the frame.
	 */
	public static final float MIN_FACE_HEIGHT_THRESHOLD = 0.01f;

	private static final long FRAME_LIMIT = 55;

	@Test
	public void testDLIBCPU() {
		runFaceDetect("dlib - HOG / resnet_v1 (CPU)", frame -> {
			return DLibFacedetection.scan(frame, MIN_FACE_HEIGHT_THRESHOLD, CPU_FACE_DETECT, LOAD_EMBEDDINGS && LOAD_LANDMARKS);
		});
	}

	@Test
	public void testDLIBCNN() {
		runFaceDetect("dlib - mmod_human_face (GPU)", frame -> {
			return DLibFacedetection.scan(frame, MIN_FACE_HEIGHT_THRESHOLD, USE_CNN_FACE_DETECT, LOAD_EMBEDDINGS && LOAD_LANDMARKS);
		});
	}

	@Test
	public void testOpenCVHaarcascade() {
		CVFacedetection.loadHaarcascade();
		CVFacedetection.loadKazemiLandmarkModel();

		runFaceDetect("OpenCV - Haarcascade / Kazemi", frame -> {
			return CVFacedetection.scan(frame, MIN_FACE_HEIGHT_THRESHOLD, LOAD_LANDMARKS);
		});
	}

	@Test
	public void testOpenCVLbpcascade() {
		CVFacedetection.loadLbpcascade();
		CVFacedetection.loadLBFLandmarkModel();

		runFaceDetect("OpenCV - lbpcascade / LBFLandmarkModel", frame -> {
			return CVFacedetection.scan(frame, MIN_FACE_HEIGHT_THRESHOLD, LOAD_LANDMARKS);
		});
	}

//	@Test
//	public void testZKeepOpen() throws IOException {
//		System.in.read();
//	}

	public static void runFaceDetect(String label, Function<VideoFrame, FaceVideoFrame> detector) {
		try (Video video = Videos.open(TEST_VIDEO)) {
			video.seekToFrameRatio(SEEK_TO);
			FacedetectionMetrics metrics = FacedetectionMetrics.create();
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				// .map(CVUtils::toGreyScale)
				.map(frame -> {
					CVUtils.boxFrame2(frame, 512);
					//CVUtils.boxFrame2(frame, 1024);
					return frame;
				})
				.map(detector)
				// .filter(FaceVideoFrame::hasFace)
				.map(metrics::track)
				.map(Facedetection::markFaces)
				.map(Facedetection::markLandmarks)
				.map(frame -> {
					CVUtils.drawText(frame, label, new org.opencv.core.Point(25, 25), 1.0f, new Scalar(255, 255, 255), 1);
					return frame;
				})
				.map(frame -> Facedetection.drawMetrics(frame, metrics, new Point(25, 45)))
				.limit(FRAME_LIMIT);
			// .map(frame -> {
			// return Facedetection.cropToFace(frame, 0);
			// });
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}

}
