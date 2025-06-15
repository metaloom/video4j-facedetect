package io.metaloom.video.facedetect.inspireface;

import java.io.FileNotFoundException;

import io.metaloom.video.facedetect.Facedetector;
import io.metaloom.video.facedetect.inspireface.impl.InspireFacedetectorImpl;

public interface InspireFacedetector extends Facedetector {

	public static String DEFAULT_PACK_PATH = "packs/Pikachu";

	public static InspireFacedetector create() throws FileNotFoundException {
		return create(DEFAULT_PACK_PATH, 320, true, true);
	}

	/**
	 * Create a new InspireFace detector.
	 * 
	 * @param supportEmbeddings
	 * @param supportAttributes
	 * @return
	 */
	public static InspireFacedetector create(String packPath, int detectPixelLevel, boolean supportEmbeddings, boolean supportAttributes)
		throws FileNotFoundException {
		return new InspireFacedetectorImpl(packPath, detectPixelLevel, supportEmbeddings, supportAttributes);
	}

}
