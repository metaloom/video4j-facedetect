package io.metaloom.video.facedetect;

import static io.metaloom.video.facedetect.FacedetectorUtils.cropToFace;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.stream.Stream;

import org.junit.Test;

import io.metaloom.video.facedetect.dlib.impl.DLibFacedetector;
import io.metaloom.video4j.Video;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class ExampleTest {

	@Test
	public void testExampleCode() throws FileNotFoundException {
		Video4j.init();
		DLibFacedetector detector = DLibFacedetector.create();
		detector.setMinFaceHeightFactor(0.01f);
		detector.enableCNNDetector();
		detector.enableLandmarks();
		detector.enableLandmarks();

		try (Video video = Videos.open("src/test/resources/pexels-mikhail-nilov-7626566.mp4")) {
			FacedetectorMetrics metrics = FacedetectorMetrics.create();
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				.map(frame -> {
					CVUtils.boxFrame2(frame, 384);
					return frame;
				})
				.map(detector::detect)
				.filter(FaceVideoFrame::hasFace)
				.map(metrics::track)
				.map(detector::markFaces)
				.map(detector::markLandmarks)
				.map(frame -> detector.drawMetrics(frame, metrics, new Point(25, 45)))
				.map(frame -> cropToFace(frame, 0));
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}
}
