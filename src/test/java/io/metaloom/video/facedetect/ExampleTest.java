package io.metaloom.video.facedetect;

import java.awt.Point;
import java.util.stream.Stream;

import org.junit.Test;

import io.metaloom.video4j.Video;
import io.metaloom.video4j.Video4j;
import io.metaloom.video4j.Videos;
import io.metaloom.video4j.opencv.CVUtils;
import io.metaloom.video4j.utils.VideoUtils;

public class ExampleTest {

	@Test
	public void testExampleCode() {
		Video4j.init();
		try (Video video = Videos.open("src/test/resources/pexels-mikhail-nilov-7626566.mp4")) {
			FacedetectionMetrics metrics = FacedetectionMetrics.create();
			Stream<FaceVideoFrame> frameStream = video.streamFrames()
				.filter(frame -> {
					return frame.number() % 5 == 0;
				})
				.map(frame -> {
					CVUtils.boxFrame2(frame, 384);
					return frame;
				})
				.map(frame -> DLibFacedetection.scan(frame, 0.01f, false, true))
				.filter(FaceVideoFrame::hasFace)
				.map(metrics::track)
				.map(Facedetection::markFaces)
				.map(Facedetection::markLandmarks)
				.map(frame -> Facedetection.drawMetrics(frame, metrics, new Point(25, 45)))
				.map(frame -> {
					return Facedetection.cropToFace(frame, 0);
				});
			VideoUtils.showVideoFrameStream(frameStream);
		}
	}
}
