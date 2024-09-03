package com.example.kick

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }
    private var boundingBoxes: List<RectF> = emptyList()

    fun setBoundingBoxes(boxes: List<RectF>) {
        boundingBoxes = boxes
        invalidate() // 이 메소드는 onDraw를 호출하여 뷰를 다시 그리게 합니다.
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (box in boundingBoxes) {
            canvas.drawRect(box, paint)
        }
    }
}