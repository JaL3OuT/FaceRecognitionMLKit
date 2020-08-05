package com.example.facerecognitionmlkit.detect.mlkit

import android.util.Log
import androidx.annotation.GuardedBy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

import java.nio.ByteBuffer

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
class FaceDetector(private val callback: DetectorCallback?) : IFrameDetector {

    interface DetectorCallback {
        fun onSuccess(frameData: ByteBuffer, results: List<Face>, frameMetadata: FrameMetadata)
        fun onFailure(exception: Exception)
    }

    companion object {
        private const val TAG = "FaceDetector"
    }

    private lateinit var detector: FaceDetector
    private val delegateDetector =
        FaceDetectorOptions.Builder()
            .enableTracking()
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .build()


    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    @Synchronized
    override fun process(data: ByteBuffer, frameMetadata: FrameMetadata) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage()
        }
    }

    @Synchronized
    private fun processLatestImage() {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage!!, processingMetaData!!)
        }
    }

    private fun processImage(data: ByteBuffer, frameMetadata: FrameMetadata) {

        val image = InputImage.fromByteBuffer(
            data,
            frameMetadata.width,
            frameMetadata.height,
            270,
            InputImage.IMAGE_FORMAT_NV21
        )



        detector = FaceDetection.getClient(delegateDetector)

        detector.process(image)
            .addOnSuccessListener { results ->
                callback?.onSuccess(data, results, frameMetadata)
                processLatestImage()
            }
            .addOnFailureListener { e ->
                callback?.onFailure(e)
            }
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

}
