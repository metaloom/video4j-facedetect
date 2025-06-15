package io.metaloom.video.facedetect.inspireface;

import java.io.FileNotFoundException;

import io.metaloom.video.facedetect.Facedetector;
import io.metaloom.video.facedetect.inspireface.impl.InspireFacedetectorImpl;

public interface InspireFacedetector extends Facedetector {

	static InspireFacedetector create() throws FileNotFoundException {
		return new InspireFacedetectorImpl();
	}

}
