package com.example.kick

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Canvas
import android.graphics.Color

import android.os.Handler
import android.os.Looper

import android.view.Surface
import android.view.TextureView


class CameraActivity : AppCompatActivity() {

    // private lateinit var previewView: PreviewView
    private lateinit var textureView: TextureView
    private lateinit var overlayView: OverlayView
    private lateinit var cameraExecutor: ExecutorService
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA  // Use front camera


    // YoloModel
    private lateinit var yoloModel: YoloModel

    companion object {
        private const val TAG = "CameraActivity"
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            finish()  // Close the activity if permission is denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        textureView = findViewById(R.id.textureView)
        overlayView = findViewById(R.id.overlayView)
        cameraExecutor = Executors.newSingleThreadExecutor()


        yoloModel = YoloModel(this)

        if (isCameraPermissionGranted()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }


    private fun normalizeBitmap(bitmap: Bitmap): TensorImage {
        // TensorImage를 float32로 초기화
        val tensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)

        // Bitmap을 TensorImage로 로드 (올바른 Bitmap 사용)
        tensorImage.load(bitmap)

        // 정규화 작업을 위해 ByteBuffer와 FloatBuffer 생성
        val byteBuffer = tensorImage.buffer
        val floatBuffer = FloatArray(byteBuffer.remaining())

        // 정규화: 각 픽셀 값을 255.0으로 나누어 0~1 범위로 변환
        for (i in floatBuffer.indices) {
            floatBuffer[i] = (byteBuffer.get().toInt() and 0xFF) / 255.0f
        }

        // 정규화된 FloatBuffer를 사용해 TensorImage 업데이트
        val normalizedTensorImage = TensorImage(org.tensorflow.lite.DataType.FLOAT32)
        normalizedTensorImage.load(tensorImage.tensorBuffer) // 텐서 버퍼로 로드

        return normalizedTensorImage
    }


    private fun letterboxBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // 원본 이미지의 비율을 유지하며, 목표 크기에 맞추기 위해 스케일 계산
        val scale = minOf(targetWidth / originalWidth.toFloat(), targetHeight / originalHeight.toFloat())

        // 스케일된 이미지의 크기 계산
        val scaledWidth = (originalWidth * scale).toInt()
        val scaledHeight = (originalHeight * scale).toInt()

        // 새로운 빈 비트맵 생성 (타겟 크기로)
        val letterboxBitmap = Bitmap.createBitmap(targetWidth, targetHeight, bitmap.config)
        val canvas = Canvas(letterboxBitmap)

        // 빈 배경을 채움 (여기서는 검정색)
        canvas.drawColor(Color.BLACK)

        // 스케일된 이미지를 중앙에 그리기
        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f
        canvas.drawBitmap(bitmap, left, top, null)

        return letterboxBitmap
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            Log.d(TAG, "Main handler post started")

            val bitmap = textureView.bitmap
            if (bitmap == null) {
                Log.d(TAG, "Bitmap is null")
                return@post
            }

            Log.d(TAG, "Bitmap retrieved successfully")

            // 모델 입력 크기로 조정
            val resizedBitmap = resizeBitmap(bitmap, 640, 640)
//            val resizedBitmap = letterboxBitmap(bitmap, 640, 640)
            Log.d(TAG, "Bitmap resized")

            // 정규화된 TensorImage 생성
            val tensorImage = normalizeBitmap(resizedBitmap)
            Log.d(TAG, "TensorImage created and normalized")

            // YOLO 모델 추론
            val boxes = yoloModel.runInference(tensorImage)
            Log.d(TAG, "Inference run successfully, found ${boxes.size} boxes," +
                    "$boxes")

//            overlayView.setBoundingBoxes(boxes)
            overlayView.setBoundingBoxes(boxes, textureView.width.toFloat(), textureView.height.toFloat())
            Log.d(TAG, "Bounding boxes set on overlay view")

            imageProxy.close()
            Log.d(TAG, "ImageProxy closed")
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val matrix = Matrix()
        matrix.postScale(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

//    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
//        return Bitmap.createScaledBitmap(bitmap, width, height, true)
//    }


    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider { request ->
                    val surface = Surface(textureView.surfaceTexture)
                    request.provideSurface(surface, cameraExecutor) { }
                }
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase ->
                    analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        processImage(imageProxy)  // 이미지 처리를 위해 processImage() 호출
                    })
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}
