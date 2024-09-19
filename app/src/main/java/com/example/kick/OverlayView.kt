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

    // DetectionResult 리스트를 사용하도록 수정
    private var detectionResults: List<DetectionResult> = emptyList()

    // 감정 분류 결과와 함께 바운딩 박스를 설정하는 함수
    fun setBoundingBoxes(results: List<DetectionResult>) {
        detectionResults = results
        invalidate() // onDraw를 호출하여 뷰를 다시 그리게 함
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("OverlayView", "Actual width: $width, Actual height: $height")
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // 각 DetectionResult에 대해 바운딩 박스, confidence, emotionLabel을 그리기
        for (result in detectionResults) {
            // 바운딩 박스를 그리기
            canvas.drawRect(result.boundingBox, boxpaint)

            // confidence 값을 텍스트로 표시
            canvas.drawText("%.2f".format(result.confidence), result.boundingBox.left, result.boundingBox.top - 10, textPaint)

            // emotionLabel 값을 텍스트로 표시
            canvas.drawText(result.emotionLabel, result.boundingBox.left, result.boundingBox.top - 80, textPaint)
        }
    }
}
