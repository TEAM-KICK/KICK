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

import android.graphics.RectF

import java.io.File
import java.io.FileOutputStream
import java.io.IOException


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

    /* --- Camera Permission --- */
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

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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


    /* --- Execute Yolo --- */
    private fun rotateBitmap(source: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun imageToBitmap(imageProxy: ImageProxy): Bitmap {
        val bitmap = imageProxy.toBitmap()
        return Bitmap.createScaledBitmap(bitmap, 640, 640, true)
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String) {
        val file = File(getExternalFilesDir(null), fileName)
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)  // PNG로 이미지 저장
            fileOutputStream.flush()
            Log.d("Camera", "Image saved: ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Camera", "Failed to save image: ${e.message}")
        } finally {
            fileOutputStream?.close()
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post {
            // calc rotation degree
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
//            Log.d("Camera", "ratationDegree: $rotationDegrees")

            // resize image
            val resizedBitmap = imageToBitmap(imageProxy)
//            Log.d("Camera", "Resized Bitmap width: ${resizedBitmap.width}, height: ${resizedBitmap.height}")

            // rotate image
            val rotatedBitmap = rotateBitmap(resizedBitmap, rotationDegrees)

            // save resized image
            saveBitmapToFile(rotatedBitmap, "resized_image.png")

            // YOLO Tensorflow Lite inference
            val boxes: List<Pair<RectF, Float>> = yoloModel.runInference(rotatedBitmap,
                overlayView.width, overlayView.height)

            // Adjust bounding boxes and display them on the overlay
            overlayView.setBoundingBoxes(boxes)

            imageProxy.close()  // Close the image proxy
        }
    }


    /* --- Start Camera --- */
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
