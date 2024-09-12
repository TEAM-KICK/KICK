package com.example.kick

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

import org.tensorflow.lite.Interpreter


class YoloModel(private val context: Context) {

    private lateinit var interpreter: Interpreter

    init {
        loadModel()  // Load the TensorFlow Lite model
    }

    // Load the TensorFlow Lite model from assets
    private fun loadModel() {
        val modelPath = assetFilePath(context, "yolov8n-face_float32.tflite")
        val modelFile = File(modelPath)
        if (modelFile.exists()) {
            interpreter = Interpreter(modelFile)
        } else {
            Log.e("YoloModel", "Model file not found!")
        }
    }

    // Helper method to get asset file path
    @Throws(IOException::class)
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) return file.absolutePath

        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }
        return file.absolutePath
    }

    // Preprocess the image to be used as input to the model
    private fun preprocessImage(bitmap: Bitmap, inputSize: Int): ByteBuffer {
//        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val inputBuffer = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4)  // 1 batch, 3 channels (RGB), 4 bytes per float
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        for (pixel in pixels) {
            // Normalize each pixel (RGB) from [0, 255] to [0, 1] and add to buffer
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        return inputBuffer
    }
    // Post-process the output from the model to extract bounding boxes
    private fun processOutput(outputBuffer: ByteBuffer, overlayViewWidth: Int, overlayViewHeight: Int): List<Pair<RectF, Float>> {
        outputBuffer.rewind()
        val outputData = FloatArray(5 * 8400)
        outputBuffer.asFloatBuffer().get(outputData)

        val boxes = mutableListOf<Pair<RectF, Float>>()
        for (i in 0 until 8400) {
            val xCenter = outputData[i]
            val yCenter = outputData[8400 + i]
            val width = outputData[2*8400 + i]
            val height = outputData[3*8400 + i]
            val confidence = outputData[4*8400 + i]

            // Only consider boxes with confidence above a threshold
            if (confidence > 0.55) {
                Log.d("YoloModel", "xCenter:$xCenter, yCenter:$yCenter, Width:$width, Height:$height, Confidence:$confidence ")
                val adjustedRect = adjustForPadding(xCenter, yCenter, width, height, overlayViewWidth, overlayViewHeight)
                boxes.add(Pair(adjustedRect, confidence))

            }
        }

        return boxes
    }

    fun adjustForPadding(xCenter: Float, yCenter: Float, width: Float, height: Float, overlayViewWidth: Int, overlayViewHeight: Int): RectF {
        val inputSize = 640  // 모델의 입력 크기 (640x640)
        val imageAspectRatio = inputSize.toFloat() / inputSize.toFloat()
        val viewAspectRatio = overlayViewWidth.toFloat() / overlayViewHeight.toFloat()

        var actualImageWidth = overlayViewWidth
        var actualImageHeight = overlayViewHeight
        var horizontalPadding = 0f
        var verticalPadding = 0f
        Log.d("Padding", "viewAspectRatio:$viewAspectRatio, imageAspectRatio:$imageAspectRatio")
        // 좌우 여백이 생긴 경우 (이미지가 세로로 더 긴 경우)
        if (viewAspectRatio < imageAspectRatio) {
            actualImageHeight = overlayViewHeight
            actualImageWidth = (overlayViewHeight * imageAspectRatio).toInt()
            horizontalPadding = (overlayViewWidth - actualImageWidth) / 2f
        }
        // 상하 여백이 생긴 경우 (이미지가 가로로 더 긴 경우)
        else {
            actualImageWidth = overlayViewWidth
            actualImageHeight = (overlayViewWidth / imageAspectRatio).toInt()
            verticalPadding = (overlayViewHeight - actualImageHeight) / 2f
        }
        Log.d("Padding", "horizontalPadding:$horizontalPadding, verticalPadding:$verticalPadding, actualWidth:$actualImageWidth, actualHeight:$actualImageHeight")
        // 좌표 변환 (좌우 및 상하 여백을 고려하여 변환)
        val xMin = (xCenter - width / 2) * actualImageWidth + horizontalPadding
        val xMax = (xCenter + width / 2) * actualImageWidth + horizontalPadding
        val yMin = (yCenter - height / 2) * actualImageHeight + verticalPadding
        val yMax = (yCenter + height / 2) * actualImageHeight + verticalPadding

        return RectF(xMin, yMin, xMax, yMax)  // 여백 적용된 좌표 반환
    }



    // Run inference on the model with a preprocessed image
    fun runInference(bitmap: Bitmap, overlayViewWidth: Int, overlayViewHeight: Int): List<Pair<RectF, Float>> {
        val inputSize = 640  // Assuming the model requires 640x640 input size
        val inputBuffer = preprocessImage(bitmap, inputSize)

        // Prepare output buffer to receive results (shape: [1, 5, 8400])
        val outputBuffer = ByteBuffer.allocateDirect(4 * 5 * 8400)
        outputBuffer.order(ByteOrder.nativeOrder())

        // Run inference
        interpreter.run(inputBuffer, outputBuffer)

        // Process the output to extract bounding boxes
        return processOutput(outputBuffer, overlayViewWidth, overlayViewHeight)
    }
}
