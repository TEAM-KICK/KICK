package com.example.kick

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.util.Log

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    // 바운딩 박스를 그릴 때 사용할 RectF를 미리 선언
    private var boundingBoxes: List<RectF> = emptyList()
    private val reusableRectF = RectF() // 미리 할당하여 재사용할 RectF 객체

    private var previewWidth: Float = 1.0f
    private var previewHeight: Float = 1.0f

    fun setBoundingBoxes(boxes: List<RectF>, previewWidth: Float, previewHeight: Float) {
        this.boundingBoxes = boxes
        this.previewWidth = previewWidth
        this.previewHeight = previewHeight
        invalidate()  // onDraw를 호출하여 뷰를 다시 그리게 함
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // OverlayView actual size
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // Calculate padding if there's an aspect ratio mismatch
        Log.d("OverlayView", "previewWidth=$previewWidth, previewHeight=$previewHeight")
        val previewAspectRatio = previewWidth / previewHeight
        val viewAspectRatio = viewWidth / viewHeight
        Log.d("OverlayView", "viewAspectRatio: $viewAspectRatio")

        var horizontalPadding = 0f
        var verticalPadding = 0f

        if (viewAspectRatio > previewAspectRatio) {
            // Horizontal padding occurs
            horizontalPadding = (viewWidth - (previewAspectRatio * viewHeight)) / 2
        } else {
            // Vertical padding occurs
            verticalPadding = (viewHeight - (viewWidth / previewAspectRatio)) / 2
        }

        // Log the padding values
        Log.d("OverlayView", "horizontalPadding=$horizontalPadding, verticalPadding=$verticalPadding")

        // Adjust bounding boxes based on the padding
        for (box in boundingBoxes) {
            val scaleX = (viewWidth - 2 * horizontalPadding) / previewWidth
            val scaleY = (viewHeight - 2 * verticalPadding) / previewHeight

            reusableRectF.set(
                (box.left * scaleX) + horizontalPadding,
                (box.top * scaleY) + 523,
                (box.right * scaleX) + horizontalPadding,
                (box.bottom * scaleY) + 523
            )

//            Log.d("OverlayView", "Scaled boxes: left=${reusableRectF.left}, top=${reusableRectF.top}, right=${reusableRectF.right}, bottom=${reusableRectF.bottom}")

            // Draw the bounding box
            canvas.drawRect(reusableRectF, paint)
        }
    }
}