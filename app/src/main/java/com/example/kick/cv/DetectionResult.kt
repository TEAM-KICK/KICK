package com.example.kick.cv

import android.graphics.RectF

data class DetectionResult(
        val boundingBox: RectF,
        val confidence: Float,
        val emotionLabel: String
)