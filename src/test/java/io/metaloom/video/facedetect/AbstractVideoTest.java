package io.metaloom.video.facedetect;

import org.junit.BeforeClass;

import io.metaloom.video4j.Video4j;

public class AbstractVideoTest {

	@BeforeClass
	public static void setup() {
		Video4j.init();
	}

	public static final String FEMALE_FACE_ROTATE = "src/test/resources/pexels-cottonbro-8090198.mp4";
	public static final String MALE_FACE_ROTATE = "src/test/resources/pexels-cottonbro-8090418.m4v";
	public static final String FACE_CLOSEUP = "src/test/resources/pexels-mikhail-nilov-7626566.mp4";
	public static final String MALE_GLASSES_FRONTAL = "src/test/resources/production ID_4100353.mp4";
	public static final String MALE_OLDER = "src/test/resources/pexels-kindel-media-8164110.mp4";
	public static final String MALE_LAPTOP = "src/test/resources/production ID_5125919.mp4";
	public static final String TWO_PERSON_ROTATING_3 = "src/test/resources/pexels-jack-sparrow-5977460.mp4";
	public static final String TWO_PERSON_ROTATING_2 = "src/test/resources/pexels-jack-sparrow-5977265.mp4";
	public static final String TWO_PERSON_ROTATING = "src/test/resources/production ID_4428751.mp4";

	protected void sleep(int timeMs) {
		try {
			Thread.sleep(timeMs);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
