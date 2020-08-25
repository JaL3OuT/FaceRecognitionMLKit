package com.example.facerecognitionmlkit

import android.util.Log
import com.example.facerecognitionmlkit.detect.mlkit.*
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.face.Face
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.nio.ByteBuffer

class MLKitActivity : AbstractActivity() {

    companion object {
        private const val TAG = "MLKitActivity"
    }

    private var mCameraSource: MLCameraSource? = null

    /**
     * Creates and starts the camera.
     */
    override fun createCameraSource() {
        mCameraSource = MLCameraSource(this, mGraphicOverlay)
      /*  mCameraSource?.apply {
            setFrameDetector(
                FaceDetector(object : FaceDetector.DetectorCallback {
                    override fun onSuccess(
                        frameData: ByteBuffer,
                        results: List<Face>,
                        frameMetadata: FrameMetadata
                    ) {
                        if (results.isEmpty()) {
                            // No faces in frame, so clear frame of any previous faces.
                            mGraphicOverlay.clear()
                        } else {
                            // We have faces
                            results.forEach { face ->
                                val existingFace = mGraphicOverlay
                                    .find(face.trackingId!!) as FaceGraphic?
                                if (existingFace == null) {


                                    Log.e("face", face.toString())
                                    // A new face has been detected.
                                    val faceGraphic = FaceGraphic(
                                        face.trackingId!!,
                                        mGraphicOverlay
                                    )
                                    mGraphicOverlay.add(faceGraphic)
                                } else {
                                    // We have an existing face, update its position.
                                    existingFace.updateFace(face)
                                }
                            }
                            mGraphicOverlay.postInvalidate()
                        }
                    }

                    override fun onFailure(exception: Exception) {
                        exception.printStackTrace()
                    }

                })
            )
        }*/

        mCameraSource?.apply {
            setFrameDetector( CodeDetector( object : CodeDetector.DetectorCallback{
                override fun onSuccess(
                    frameData: ByteBuffer,
                    results: List<Barcode>,
                    frameMetadata: FrameMetadata
                ) {
                    if (results.isEmpty())
                    {
                        Log.e(TAG ,"pas de code a bar " )
                    }
                    else
                    {
                        results.forEach {  barcode: Barcode ->
                            Log.e("CODE" , barcode.displayValue.toString())
                            textView.text = barcode.displayValue.toString()
                        }
                    }

                }

                override fun onFailure(exception: Exception) {
                    exception.printStackTrace()
                }

            }
            ))
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.
     */
    override fun startCameraSource() {
        checkGooglePlayServices()

        if (mCameraSource != null) {
            try {
                mCameraPreview.start(mCameraSource!!, mGraphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    /**
     * Releases the resources associated with the camera source.
     */
    override fun releaseCameraSource() {
        if (mCameraSource != null) {
            mCameraSource!!.release()
            mCameraSource = null
        }
    }
}
