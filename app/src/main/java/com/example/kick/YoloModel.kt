package com.example.kick

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer
import java.nio.ByteOrder


class YoloModel(private val context: Context) {

    private val module: Module

    init {
        // Load the TorchScript model from assets
        module = Module.load(assetFilePath(context, "yolov8n-face.torchscript"))
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

    fun runInference(bitmap: Bitmap): List<RectF> {
        // Prepare the input tensor
        val inputTensor = preprocessImage(bitmap)

        // Run the model - directly receive the output tensor
        val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()

        // Process the output and return bounding boxes
        return processOutput(outputTensor)
    }

    private fun preprocessImage(bitmap: Bitmap): Tensor {
        // Create a buffer that matches the expected shape [1, 3, 640, 640]
        val inputBuffer = FloatBuffer.allocate(1 * 3 * 640 * 640)

        val intValues = IntArray(640 * 640)  // Prepare to hold pixel data
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixelIndex = 0
        for (i in 0 until 640) {
            for (j in 0 until 640) {
                val pixelValue = intValues[pixelIndex++]
                // Normalize and add pixel data to the buffer
                inputBuffer.put(((pixelValue shr 16 and 0xFF) / 255.0f))  // Red
                inputBuffer.put(((pixelValue shr 8 and 0xFF) / 255.0f))   // Green
                inputBuffer.put(((pixelValue and 0xFF) / 255.0f))         // Blue
            }
        }

        // Convert to a PyTorch tensor
        return Tensor.fromBlob(inputBuffer.array(), longArrayOf(1, 3, 640, 640))
    }

    private fun sigmoid(x: Float): Float {
        return (1 / (1 + Math.exp(-x.toDouble()))).toFloat()
    }


    private fun debugOutput(output: Tensor) {
        // Tensor에서 데이터를 FloatArray로 추출
        val outputData = output.dataAsFloatArray

        // Output 데이터 출력
        Log.d("YoloModel", "Tensor shape: ${output.shape().joinToString(", ")}")
        Log.d("YoloModel", "Output data: ${outputData.joinToString(", ")}")
    }

    private fun processOutput(output: Tensor): List<RectF> {

        debugOutput(output)

        val boxes = mutableListOf<RectF>()

        // Output tensor에서 데이터를 FloatBuffer로 추출
        val outputData = output.dataAsFloatArray


        val numBoxes = outputData.size / 5

        // 각 경계 상자의 좌표와 신뢰도(confidence) 추출
        for (i in 0 until numBoxes) {

            val offset = i * 5
            val xCenter = outputData[offset]     // center x
            val yCenter = outputData[offset + 1]  // center y
            val width = outputData[offset + 2]    // width
            val height = outputData[offset + 3]
            val confidence = outputData[offset+4] // raw confidence

//            val xCenter = outputData[i] // xCenter는 0~8399
//            val yCenter = outputData[i + 8400] // yCenter는 8400~16799
//            val width = outputData[i + 16800] // width는 16800~25199
//            val height = outputData[i + 25200] // height는 25200~33599
//            val confidence = outputData[i + 33600] // confidence는 33600~42000
//            val confidence = sigmoid(rawconfidence) // sigmoid 적용

            val xMin = xCenter - width / 2
            val yMin = yCenter - height / 2
            val xMax = xCenter + width / 2
            val yMax = yCenter + height / 2


            // 신뢰도 필터링 (confidence threshold)
            if (confidence >= 0.25) {
                boxes.add(RectF(xMin, yMin, xMax, yMax))
                Log.d("YoloModel", "xMin:$xMin , yMin:$yMin, xMax:$xMax, yMax:$yMax, confidence:$confidence")
            }
        }

        return boxes
    }
}