package com.example.kick.cv

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class EmotionModel(context: Context) {

    // TensorFlow Lite 인터프리터
    private lateinit var interpreter: Interpreter

    // 모델 입력 크기 및 클래스 수
    private val inputSize = 48  // 모델에서 사용하는 이미지 크기
    private val outputClasses = 7  // 분류할 감정 클래스 수 (7가지 감정)

    init {
        // TFLite 모델을 로드
        interpreter = Interpreter(loadModelFile(context))
    }

    // TFLite 모델 파일을 로드하는 함수
    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("emotion-model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // 이미지를 전처리하여 모델에 입력할 수 있는 ByteBuffer로 변환하는 함수
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val imgData = ByteBuffer.allocateDirect(4 * inputSize * inputSize)  // (48x48x1) 이미지 크기
        imgData.order(ByteOrder.nativeOrder())

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        val intValues = IntArray(inputSize * inputSize)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        // 흑백 이미지 전처리 (0~255 범위의 값을 0~1 범위로 정규화)
        for (pixel in intValues) {
            val normalizedPixel = (pixel and 0xFF) / 255.0f
            imgData.putFloat(normalizedPixel)
        }

        return imgData
    }

    // 감정 분류를 수행하는 함수
    fun classifyEmotion(bitmap: Bitmap): Int {
        val inputBuffer = preprocessImage(bitmap)

        // 모델의 출력 크기 설정 (감정 클래스 수)
        val outputBuffer = Array(1) { FloatArray(outputClasses) }

        // 모델 실행 (입력: 전처리된 이미지, 출력: 감정 확률 분포)
        interpreter.run(inputBuffer, outputBuffer)

        // 가장 높은 확률을 가진 감정 클래스 인덱스를 반환
        return outputBuffer[0].indices.maxByOrNull { outputBuffer[0][it] } ?: -1
    }

    // Interpreter 리소스 해제
    fun close() {
        interpreter.close()
    }
}
