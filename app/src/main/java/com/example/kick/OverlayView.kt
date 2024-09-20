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
            // 바운딩 박스를 그리기 전에 adjustForPadding을 통해 좌표 변환
            val adjustedBox = adjustForPadding(
                result.boundingBox.left,
                result.boundingBox.top,
                result.boundingBox.right,
                result.boundingBox.bottom,
                width,  // OverlayView의 실제 너비
                height  // OverlayView의 실제 높이
            )

            // 조정된 바운딩 박스 그리기
            canvas.drawRect(adjustedBox, boxpaint)

            // confidence 값을 텍스트로 표시
            canvas.drawText("%.2f".format(result.confidence), adjustedBox.left, adjustedBox.top - 10, textPaint)

            // emotionLabel 값을 텍스트로 표시
            canvas.drawText(result.emotionLabel, adjustedBox.left, adjustedBox.top - 80, textPaint)
        }
    }


    fun adjustForPadding(left: Float, top: Float, right: Float, bottom: Float, overlayViewWidth: Int, overlayViewHeight: Int): RectF {
        val inputSize = 640  // YOLO 모델의 입력 크기 (640x640)
        val imageAspectRatio = inputSize.toFloat() / inputSize.toFloat()
        val viewAspectRatio = overlayViewWidth.toFloat() / overlayViewHeight.toFloat()

        var actualImageWidth = overlayViewWidth
        var actualImageHeight = overlayViewHeight
        var horizontalPadding = 0f
        var verticalPadding = 0f

        // 좌우 여백이 생긴 경우
        if (viewAspectRatio < imageAspectRatio) {
            actualImageHeight = overlayViewHeight
            actualImageWidth = (overlayViewHeight * imageAspectRatio).toInt()
            horizontalPadding = (overlayViewWidth - actualImageWidth) / 2f
        }
        // 상하 여백이 생긴 경우
        else {
            actualImageWidth = overlayViewWidth
            actualImageHeight = (overlayViewWidth / imageAspectRatio).toInt()
            verticalPadding = (overlayViewHeight - actualImageHeight) / 2f
        }

        // 좌표 변환 (이미 계산된 left, top, right, bottom 사용)
        val adjustedLeft = left * actualImageWidth + horizontalPadding
        val adjustedRight = right * actualImageWidth + horizontalPadding
        val adjustedTop = top * actualImageHeight + verticalPadding
        val adjustedBottom = bottom * actualImageHeight + verticalPadding

        return RectF(adjustedLeft, adjustedTop, adjustedRight, adjustedBottom)  // 변환된 좌표 반환
    }

}
