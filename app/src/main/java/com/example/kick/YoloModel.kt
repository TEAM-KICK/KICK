package com.example.kick

import android.content.Context
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import android.graphics.Bitmap

class YoloModel(private val context: Context) {

    private var interpreter: Interpreter
    private val outputShape = intArrayOf(1, 8400, 6) // YOLO output: [batch, boxes, classes + box coordinates]

    init {
        interpreter = Interpreter(loadModelFile("yolov8n-face_float32.tflite"))

        // 모델 입력 크기를 확인하는 부분
        val inputShape = interpreter.getInputTensor(0).shape() // [1, height, width, channels]
        val inputHeight = inputShape[1]
        val inputWidth = inputShape[2]
        val inputChannels = inputShape[3]

        Log.d("YoloModel", "Model input shape: Height=$inputHeight, Width=$inputWidth, Channels=$inputChannels")
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
        // 입력 버퍼 준비
        val inputBuffer = convertBitmapToByteBuffer(image.bitmap, 640, 640)

        // 출력 버퍼 준비
        val outputBuffer = ByteBuffer.allocateDirect(4 * 8400 * 6).order(ByteOrder.nativeOrder())

        Log.d("YoloModel", "Running inference...")
        // 모델 실행
        interpreter.run(inputBuffer, outputBuffer)

        Log.d("YoloModel", "Inference completed. Processing output...")

        return processOutput(outputBuffer)
    }

    // 입력 이미지를 ByteBuffer로 변환하는 메소드
    private fun convertBitmapToByteBuffer(bitmap: Bitmap, width: Int, height: Int): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * width * height * 3) // 640 * 640 * 3 채널, float32 타입
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(width * height)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until height) {
            for (j in 0 until width) {
                val value = intValues[pixel++]

                // RGB 값 추출 및 정규화
                byteBuffer.putFloat(((value shr 16 and 0xFF) / 255.0f)) // Red
                byteBuffer.putFloat(((value shr 8 and 0xFF) / 255.0f))  // Green
                byteBuffer.putFloat(((value and 0xFF) / 255.0f))       // Blue
            }
        }

        return byteBuffer
    }

    private fun processOutput(buffer: ByteBuffer): List<RectF> {
        buffer.rewind()
        val boxes = mutableListOf<RectF>()

        while (buffer.hasRemaining()) {
            val xMin = buffer.float
            val yMin = buffer.float
            val xMax = buffer.float
            val yMax = buffer.float
            val confidence = buffer.float


            if (confidence > 0.9) { // 신뢰도 임계값, 필요시 조정
                boxes.add(RectF(xMin, yMin, xMax, yMax))
                Log.d("YoloModel", "xMin:$xMin , yMin:$yMin, xMax:$xMax, yMax:$yMax, confidence:$confidence")
            }
        }

        return boxes
    }

    fun close() {
        interpreter.close()
    }
}
