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

    fun setBoundingBoxes(boxes: List<RectF>, previewWidth: Float, previewHeight: Float) {
        boundingBoxes = boxes

        // OverlayView의 화면 크기 계산
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        Log.d("OverlayView", "OverlayView 크기: width=$viewWidth, height=$viewHeight")
        Log.d("OverlayView", "PreviewView/TextureView size: width=$previewWidth, height=$previewHeight")
        // 모델 출력 좌표를 뷰의 좌표로 변환 (0-1 사이의 값을 절대 좌표로 변환)
        for (box in boundingBoxes) {

            reusableRectF.set(
                box.left,
                box.top ,
                box.right,
                box.bottom,
            )

            Log.d("OverlayView", "Scaled boxes: left=${reusableRectF.left}, top=${reusableRectF.top}, right=${reusableRectF.right}, bottom=${reusableRectF.bottom}")

            // 박스 크기가 너무 작지 않을 때만 그리기
            if ((reusableRectF.right - reusableRectF.left) > 0.1 * viewWidth &&
                (reusableRectF.bottom - reusableRectF.top) > 0.1 * viewHeight
            ) {
//                Log.d("OverlayView", "Bounding box drawn: $reusableRectF")
            }
        }

        invalidate() // onDraw를 호출하여 뷰를 다시 그리게 함
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("OverlayView", "Actual width: $width, Actual height: $height")
        for (box in boundingBoxes) {
            // 스케일링된 박스를 그리기
            canvas.drawRect(reusableRectF, paint)
        }

    }
}