package io.metaloom.video.facedetect.inspireface.impl;

import static io.metaloom.video.facedetect.FacedetectorUtils.cropToFace;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.imgscalr.Scalr;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import io.metaloom.inspireface4j.BoundingBox;
import io.metaloom.inspireface4j.Detection;
import io.metaloom.inspireface4j.InspirefaceLib;
import io.metaloom.inspireface4j.InspirefaceSession;
import io.metaloom.inspireface4j.SessionFeature;
import io.metaloom.inspireface4j.data.FaceDetections;
import io.metaloom.video.facedetect.AbstractFacedetector;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video.facedetect.face.FaceBox;
import io.metaloom.video.facedetect.face.impl.FaceBoxImpl;
import io.metaloom.video.facedetect.inspireface.InspireFacedetector;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.impl.MatProvider;
import io.metaloom.video4j.opencv.CVUtils;

public class InspireFacedetectorImpl extends AbstractFacedetector implements InspireFacedetector {

	private static String DEFAULT_PACK_PATH = "packs/Pikachu";

	private InspirefaceSession session;

	/**
	 * Create a new facedetector and use default paths for detection model files.
	 * 
	 * @throws FileNotFoundException
	 */
	public InspireFacedetectorImpl() throws FileNotFoundException {
		this(DEFAULT_PACK_PATH);
	}

	/**
	 * Create a new facedetector.
	 * 
	 * @param packPath
	 * @throws FileNotFoundException
	 */
	public InspireFacedetectorImpl(String packPath) throws FileNotFoundException {
		if (!Files.exists(Paths.get(packPath))) {
			throw new FileNotFoundException("model pack file could not be found at " + packPath);
		}
		session = InspirefaceLib.session(packPath, 640, SessionFeature.ENABLE_FACE_RECOGNITION, SessionFeature.ENABLE_FACE_ATTRIBUTE,
			SessionFeature.ENABLE_MASK_DETECT);
	}

	@Override
	public FaceVideoFrame detectFaces(VideoFrame frame) {
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		BufferedImage img = frame.toImage();
		faceFrame.setFaces(detectFaces(img));
		return faceFrame;
	}

	@Override
	public List<? extends Face> detectFaces(BufferedImage img) {
		int absoluteFaceHeightThreshold = calculateHeightThreshold(img);

		// Detect faces
		List<Face> faces = new ArrayList<>();
		Mat imageMat = MatProvider.mat(img, Imgproc.COLOR_BGRA2BGR565);
		CVUtils.bufferedImageToMat(img, imageMat);
		FaceDetections detections = session.detect(imageMat, true);
		for (Detection detection : detections) {
			int height = detection.box().getHeight();

			// Check if the found face is too small and does not meet the face height threshold.
			if (height > absoluteFaceHeightThreshold) {
				Face face = Face.create(toFaceBox(detection.box()));
				faces.add(face);
			}
		}

		return faces;
	}

	private int calculateHeightThreshold(BufferedImage img) {
		if (minFaceHeightFactor != 0) {
			// Compute minimum face size
			int height = img.getHeight();
			if (Math.round(height * minFaceHeightFactor) > 0) {
				return Math.round(height * minFaceHeightFactor);
			}
		}
		return 0;
	}

	@Override
	public FaceVideoFrame detectEmbeddings(VideoFrame frame) {
		BufferedImage img = frame.toImage();
		List<Face> faces = new ArrayList<>();
		Mat imageMat = MatProvider.mat(img, Imgproc.COLOR_BGRA2BGR565);
		CVUtils.bufferedImageToMat(img, imageMat);
		int faceNr = 0;
		FaceDetections detections = session.detect(imageMat, false);
		for (Detection detection : detections) {
			float[] embedding = session.embedding(imageMat, detections, faceNr);
			Face face = Face.create(toFaceBox(null));
			face.setEmbedding(embedding);
			// face.setLandmarks(faceDesc.getFacialLandmarks());
			faces.add(face);
		}
		return FaceVideoFrame.from(frame).setFaces(faces);

	}

	@Override
	public FaceVideoFrame extractEmbeddings(FaceVideoFrame frame) {
		if (!frame.hasFaces()) {
			return frame;
		}
		BufferedImage img = frame.toImage();

		List<? extends Face> faces = frame.faces();
		for (Face face : faces) {
			// float marginPercent = 0.15f;
			float marginPercent = 0;
			BufferedImage croppedImg = cropToFace(face, img, marginPercent);
			int scaleFactor = 1;
			BufferedImage enhancedCroppedImage = croppedImg;
			if (scaleFactor > 1) {
				int w = croppedImg.getWidth() * scaleFactor;
				int h = croppedImg.getHeight() * scaleFactor;
				enhancedCroppedImage = Scalr.resize(croppedImg, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, w, h, Scalr.OP_ANTIALIAS);
			}

			BufferedImage convertedImg = alignImageType(enhancedCroppedImage);
			img.flush();
			croppedImg.flush();
			enhancedCroppedImage.flush();

			// Convert to Mat
			Mat imageMat = MatProvider.mat(convertedImg, Imgproc.COLOR_BGRA2BGR565);
			CVUtils.bufferedImageToMat(convertedImg, imageMat);
			FaceDetections detections = session.detect(imageMat, false);
			int faceNr = 0;
			for (Detection detection : detections) {
				float[] embedding = session.embedding(imageMat, detections, faceNr);
				// face.setLandmarks(decropLandmarks(face, faceDesc.getFacialLandmarks(), scaleFactor));
				face.setEmbedding(embedding);
				faceNr++;
			}
			MatProvider.released(imageMat);

		}
		return frame;
	}

	@Override
	public FaceVideoFrame detectLandmarks(VideoFrame frame) {
		BufferedImage img = frame.toImage();
		List<Face> faces = new ArrayList<>();
		// List<FaceDescriptor> faceDescriptors = session.landmarks(img);
		// for (FaceDescriptor faceDesc : faceDescriptors) {
		// Face face = Face.create(faceDesc.getFaceBox());
		// face.setLandmarks(faceDesc.getFacialLandmarks());
		// faces.add(face);
		// }
		return FaceVideoFrame.from(frame)
			.setFaces(faces);
	}

	@Override
	public FaceVideoFrame detectLandmarks(FaceVideoFrame frame) {
		if (!frame.hasFaces()) {
			return frame;
		}

		BufferedImage img = frame.toImage();
		List<? extends Face> faces = frame.faces();
		// for (Face face : faces) {
		// BufferedImage faceImg = FacedetectorUtils.cropToFace(face, img);
		// List<FaceDescriptor> faceDescriptors = fixOffset(face, jdlib.getFaceLandmarks(faceImg));
		// for (FaceDescriptor faceDesc : faceDescriptors) {
		// face.setLandmarks(faceDesc.getFacialLandmarks());
		// }
		// }
		return frame;
	}

	// private List<FaceDescriptor> fixOffset(Face face, List<FaceDescriptor> faceLandmarks) {
	// List<FaceDescriptor> faceDescriptors = new ArrayList<>();
	// for (FaceDescriptor mark : faceLandmarks) {
	// List<Point> points = mark.getFacialLandmarks()
	// .stream()
	// .map(p -> {
	// p.x += face.start().x;
	// p.y += face.start().y;
	// return p;
	// })
	// .toList();
	//
	// FaceBox box = face.box();
	// Rectangle rect = new Rectangle(box.getStartX(), box.getStartY(), box.getWidth(), box.getHeight());
	// faceDescriptors.add(new FaceDescriptor(rect, points));
	// }
	// return faceDescriptors;
	// }

	private static BufferedImage alignImageType(BufferedImage img) {
		BufferedImage convertedImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		convertedImg.getGraphics()
			.drawImage(img, 0, 0, null);
		return convertedImg;
	}

	private FaceBox toFaceBox(BoundingBox bbox) {
		FaceBox fbox = new FaceBoxImpl();
		fbox.setHeight(bbox.getHeight());
		fbox.setWidth(bbox.getWidth());
		fbox.setStartX(bbox.getX());
		fbox.setStartY(bbox.getY());
		return fbox;
	}

}
