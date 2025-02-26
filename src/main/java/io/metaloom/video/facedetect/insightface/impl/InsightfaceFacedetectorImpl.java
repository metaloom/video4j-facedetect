package io.metaloom.video.facedetect.insightface.impl;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.metaloom.facedetection.client.FaceDetectionServerClient;
import io.metaloom.video.facedetect.AbstractFacedetector;
import io.metaloom.video.facedetect.FaceVideoFrame;
import io.metaloom.video.facedetect.face.Face;
import io.metaloom.video4j.VideoFrame;
import io.metaloom.video4j.utils.ImageUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class InsightfaceFacedetectorImpl extends AbstractFacedetector implements InsightfaceFacedetector {

	private final FaceDetectionServerClient client;

	public InsightfaceFacedetectorImpl() {
		this.client = FaceDetectionServerClient.newFaceDetectClient();
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
		List<Face> faces = new ArrayList<>();

		try {
			String encoded = ImageUtils.toBase64JPG(img);
			JsonObject out = client.detect(null, encoded);
			JsonArray facesArray = out.getJsonArray("faces");
			if (facesArray != null) {
				for (int i = 0; i < facesArray.size(); i++) {
					JsonObject faceJson = facesArray.getJsonObject(i);
					JsonArray bbox = faceJson.getJsonArray("bbox");
					int x = bbox.getInteger(0);
					int y = bbox.getInteger(1);
					int width = bbox.getInteger(2) / 2;
					int height = bbox.getInteger(3) / 2;
					Face face = Face.create(new Rectangle(x, y, width, height));
					faces.add(face);
				}
			}
		} catch (IOException | URISyntaxException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return faces;
	}

	@Override
	public FaceVideoFrame detectLandmarks(FaceVideoFrame frame) {
		return frame;
	}

	@Override
	public FaceVideoFrame detectLandmarks(VideoFrame frame) {
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		return faceFrame;
	}

	@Override
	public FaceVideoFrame detectEmbeddings(FaceVideoFrame frame) {
		return frame;
	}

	@Override
	public FaceVideoFrame detectEmbeddings(VideoFrame frame) {
		FaceVideoFrame faceFrame = FaceVideoFrame.from(frame);
		return faceFrame;
	}

}
