package com.example.facerecognitionmlkit.detect.mlkit

import com.example.facerecognitionmlkit.detect.mlkit.FrameMetadata
import java.nio.ByteBuffer

interface IFrameDetector {

    fun process(data: ByteBuffer, frameMetadata: FrameMetadata)

    fun stop()

}