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
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF


class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: OverlayView
    private lateinit var cameraExecutor: ExecutorService
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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
            val bitmap = previewView.bitmap ?: return@post  // Get bitmap from PreviewView

//            val resizedBitmap = resizeBitmap(bitmap, 640, 640)
            val resizedBitmap = letterboxBitmap(bitmap, 640, 640)
            // YOLO TorchScript inference (preprocess and run inference)
            val boxes = yoloModel.runInference(resizedBitmap)

            // Adjust bounding boxes and display them on the overlay
            overlayView.setBoundingBoxes(boxes, previewView.width.toFloat(), previewView.height.toFloat())

            imageProxy.close()  // Close the image proxy
        }
    }

    fun letterboxBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height

        // Calculate the scale to maintain the aspect ratio
        val scale = minOf(targetWidth / originalWidth.toFloat(), targetHeight / originalHeight.toFloat())

        // Calculate the new scaled dimensions
        val scaledWidth = (originalWidth * scale).toInt()
        val scaledHeight = (originalHeight * scale).toInt()

        // Create a new empty bitmap with the target dimensions
        val letterboxBitmap = Bitmap.createBitmap(targetWidth, targetHeight, bitmap.config)

        // Create a canvas to draw on the new bitmap
        val canvas = Canvas(letterboxBitmap)

        // Fill the canvas with black (letterbox padding)
        canvas.drawColor(Color.BLACK)

        // Calculate the position to center the image on the canvas
        val left = (targetWidth - scaledWidth) / 2
        val top = (targetHeight - scaledHeight) / 2

        // Draw the scaled bitmap onto the canvas using Rect for both source and destination
        val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
        val dstRect = Rect(left, top, left + scaledWidth, top + scaledHeight)
        canvas.drawBitmap(bitmap, srcRect, dstRect, null)

        return letterboxBitmap
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val matrix = Matrix()
        matrix.postScale(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
                it.setSurfaceProvider(previewView.surfaceProvider)  // Use PreviewView's SurfaceProvider
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase ->
                    analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                        processImage(imageProxy)  // Analyze image data
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
