package com.example.kick

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.util.Log

import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val boxpaint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 70f
        style = Paint.Style.FILL
    }

    // 바운딩 박스를 그릴 때 사용할 RectF를 미리 선언
    private var boundingBoxes: List<Pair<RectF, Float>> = emptyList()
    private val reusableRectF = RectF() // 미리 할당하여 재사용할 RectF 객체

    fun setBoundingBoxes(boxes: List<Pair<RectF, Float>>) {
        boundingBoxes = boxes
        invalidate() // onDraw를 호출하여 뷰를 다시 그리게 함
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("OverlayView", "Actual width: $width, Actual height: $height")
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        for ((box, confidence) in boundingBoxes) {
            // 바운딩 박스를 그리기 (예: 사각형)
            canvas.drawRect(box, boxpaint)

            // confidence 값을 텍스트로 표시
            canvas.drawText("%.2f".format(confidence), box.left, box.top - 10, textPaint)
        }

    }
}