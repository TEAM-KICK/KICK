package com.example.kick

import android.content.Context
import android.graphics.RectF
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class YoloModel(private val context: Context) {

    private lateinit var tflite: Interpreter
    private val inputShape = intArrayOf(1, 3, 640, 640) // Example input shape for YOLOv8
    private val outputShape = intArrayOf(1, 8400, 6) // YOLO output: [batch, boxes, classes + box coordinates]

    init {
        tflite = Interpreter(loadModelFile("yolov8n-face_float32.tflite"))
    }

    @Throws(IOException::class)
    private fun loadModelFile(modelFileName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun runInference(image: TensorImage): List<RectF> {
        val inputBuffer = image.buffer
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, org.tensorflow.lite.DataType.FLOAT32).buffer

        tflite.run(inputBuffer, outputBuffer)

        return processOutput(outputBuffer)
    }

    private fun processOutput(buffer: java.nio.ByteBuffer): List<RectF> {
        val boxes = mutableListOf<RectF>()

        while (buffer.hasRemaining()) {
            val xMin = buffer.float
            val yMin = buffer.float
            val xMax = buffer.float
            val yMax = buffer.float
            val confidence = buffer.float

            // Assuming the first value is the confidence
            if (confidence > 0.5) { // Adjust confidence threshold as needed
                boxes.add(RectF(xMin, yMin, xMax, yMax))
            }
        }
        return boxes
    }

    fun close() {
        tflite.close()
    }
}