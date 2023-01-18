# Video4j - Facedetect

This project contains APIs that provide access to different [Face detection](https://en.wikipedia.org/wiki/Face_detection) implementations via the [Video4j](https://github.com/metaloom/video4j) library.

## Status

There are still some kinks which need to be worked out. Thus the library has not yet been published. The API may change in the future.

## Example

```java
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
```

## Available Detectors

### OpenCV

The OpenCV classifier based face detection needs to be initialized before usage.

```java
// Face detection classifiers
CVFacedetection.loadLbpcascade();
CVFacedetection.loadHaarcascade();

// Landmark detection models
CVFacedetection.loadLBFLandmarkModel();
CVFacedetection.loadKazemiLandmarkModel();

// Open video and load frames
See Video4j API on how to handle videos

// Run face detection 
// The landmark detection can be turned of if not needed. In this case no landmark model is needed.
FaceVideoFrame faceframe = CVFacedetection.scan(frame, 0.05f, true);
```

# dlib

The face detector implementation loads the needed models automatically.
At the moment two options are available:

* HOG face detector
* CNN face detector which can utilize GPU

```java
// Open video and load frames
…

// Run the face detection using dlib
boolean useCNN = true;
boolean loadEmbeddings = true;
FaceVideoFrame faceframe = DLibFacedetection.scan(frame, 0.05f, useCNN, loadEmbeddings);
```

## Face Extraction data

```java
faceframe.hasFace(); // Check if the frame contains a detected face
List<Face> faces = faceframe.faces(); // Access the faces
Face face = faces.get(0);
face.start(); // Upper left point of the face
face.end(); // Lower right point of the face
Dimension dim = face.dimension(); // Dimension of the face area in pixel
List<Point> landmarks = face.getLandmarks(); // Load the detected landmarks
face.getEmbeddings(); // Access the embeddings vector data
```

## Model sources

dlib:

* [dlib/dlib_face_recognition_resnet_model_v1.dat](https://github.com/davisking/dlib-models/blob/master/dlib_face_recognition_resnet_model_v1.dat.bz2)
* [dlib/mmod_human_face_detector.dat](http://dlib.net/files/mmod_human_face_detector.dat.bz2)
* [dlib/shape_predictor_68_face_landmarks.dat](https://raw.githubusercontent.com/italojs/facial-landmarks-recognition/master/shape_predictor_68_face_landmarks.dat)

OpenCV:

* [opencv/face_landmark_model.dat](https://raw.githubusercontent.com/opencv/opencv_3rdparty/contrib_face_alignment_20170818/face_landmark_model.dat)
* [opencv/lbfmodel.yaml](https://raw.githubusercontent.com/kurnianggoro/GSOC2017/master/data/lbfmodel.yaml)


## Test footage sources

* https://www.pexels.com/video/video-of-woman-being-scanned-8090198/
* https://www.pexels.com/video/video-of-man-getting-examined-8090418/
* https://www.pexels.com/video/frustrated-young-guy-yelling-over-his-laptop-5125919/
* https://www.pexels.com/video/man-people-office-relationship-4100353/
* https://www.pexels.com/video/portrait-of-young-woman-touching-her-necklace-7626566/
* https://www.pexels.com/video/an-upset-man-touching-his-head-8164110/
* https://www.pexels.com/video/man-and-woman-talking-to-each-other-inside-the-office-5977460/
* https://www.pexels.com/video/meeting-new-client-4428751/
* https://www.pexels.com/video/a-man-and-a-woman-working-while-eating-at-a-cafe-5977265/

