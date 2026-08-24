package com.pnb.bank.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ArcGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var scoreValue: Float = 300f // Min: 300, Max: 900
    private val minScore = 300f
    private val maxScore = 900f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 36f
        strokeCap = Paint.Cap.ROUND
    }

    private val rectF = RectF()

    fun setScore(score: Int) {
        scoreValue = score.coerceIn(300, 900).toFloat()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        // Keep bounds padding
        val size = Math.min(width, height * 2) - 60f
        val left = (width - size) / 2f
        val top = 30f
        rectF.set(left, top, left + size, top + size)

        // Gradient color stops matching PNB theme (PNB Red -> Orange -> Green)
        val colors = intArrayOf(
            Color.parseColor("#D32F2F"), // Red
            Color.parseColor("#F57C00"), // Orange
            Color.parseColor("#388E3C")  // Green
        )

        // Set gradient
        val gradient = LinearGradient(
            rectF.left, rectF.centerY(),
            rectF.right, rectF.centerY(),
            colors,
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient

        // Draw track (translucent black outline)
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 36f
            strokeCap = Paint.Cap.ROUND
            color = Color.parseColor("#15000000")
        }
        canvas.drawArc(rectF, 180f, 180f, false, trackPaint)

        // Draw progress arc
        val progressPercentage = (scoreValue - minScore) / (maxScore - minScore)
        val sweepAngle = 180f * progressPercentage
        canvas.drawArc(rectF, 180f, sweepAngle, false, paint)
    }
}
