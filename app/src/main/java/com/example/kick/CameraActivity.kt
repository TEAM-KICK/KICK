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

import android.os.Handler
import android.os.Looper

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var cameraExecutor: ExecutorService
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA  // Use front camera

    // YoloModel
    private lateinit var yoloModel: YoloModel

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

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.overlayView)
        cameraExecutor = Executors.newSingleThreadExecutor()


        yoloModel = YoloModel(this)

        if (isCameraPermissionGranted()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            Log.d(TAG, "Main handler post started")

            val bitmap = previewView.bitmap
            if (bitmap == null) {
                Log.d(TAG, "Bitmap is null")
                return@post
            }

            Log.d(TAG, "Bitmap retrieved successfully")

            val resizedBitmap = resizeBitmap(bitmap, 640, 640) // 모델의 입력 크기로 조정
            Log.d(TAG, "Bitmap resized")

            val tensorImage = TensorImage.fromBitmap(resizedBitmap)
            Log.d(TAG, "TensorImage created")

            val boxes = yoloModel.runInference(tensorImage)
            Log.d(TAG, "Inference run successfully, found ${boxes.size} boxes")

            overlayView.setBoundingBoxes(boxes)
            Log.d(TAG, "Bounding boxes set on overlay view")

            imageProxy.close()
            Log.d(TAG, "ImageProxy closed")
        }
    }

//    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
//        val matrix = Matrix()
//        matrix.postScale(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
//        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


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
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase ->
                    analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        processImage(imageProxy)
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

    companion object {
        private const val TAG = "CameraActivity"
    }
}
