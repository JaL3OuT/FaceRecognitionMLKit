package com.example.facerecognitionmlkit.detect.mlkit

import android.util.Log
import androidx.annotation.GuardedBy
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import java.io.IOException
import java.nio.ByteBuffer

class CodeDetector(private val callback: DetectorCallback?)  : IFrameDetector {

    interface DetectorCallback {
        fun onSuccess(frameData: ByteBuffer, results: List<Barcode>, frameMetadata: FrameMetadata)
        fun onFailure(exception: Exception)
    }

    companion object {
        private const val TAG = "CodeDetector"
    }

    private lateinit var detector: BarcodeScanner

   private val delegatedetector = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC)
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

    override fun process(data: ByteBuffer, frameMetadata: FrameMetadata) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage()
        }    }

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

    private fun processImage(processingImage: ByteBuffer, processingMetaData: FrameMetadata) {
        val image = InputImage.fromByteBuffer(
            processingImage,
            processingMetaData.width,
            processingMetaData.height,
            270,
            InputImage.IMAGE_FORMAT_NV21
        )
        detector = BarcodeScanning.getClient(delegatedetector)

        detector.process(image).addOnSuccessListener { results -> callback!!.onSuccess(processingImage , results , processingMetaData)
            processLatestImage()



        }.addOnFailureListener {
            e ->
            callback!!.onFailure(e)
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