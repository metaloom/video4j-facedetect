package io.metaloom.video.facedetect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import org.junit.jupiter.api.Test;

public class FaceTest {

	@Test
	public void testFaceLarger() {
		Face face = Face.create(new Rectangle(320, 20, 100, 100), 500, 300);
		Point start = face.start(1000, 600);
		assertEquals(640, start.x);
		assertEquals(40, start.y);

		Dimension dim = face.dimension(1000, 600);
		assertEquals(200, dim.height);
		assertEquals(200, dim.width);
	}

	@Test
	public void testFaceSmaller() {
		Face face = Face.create(new Rectangle(320, 20, 100, 100), 500, 300);
		Point start = face.start(250, 150);
		assertEquals(160, start.x);
		assertEquals(10, start.y);

		Dimension dim = face.dimension(250, 150);
		assertEquals(50, dim.height);
		assertEquals(50, dim.width);
	}

}
