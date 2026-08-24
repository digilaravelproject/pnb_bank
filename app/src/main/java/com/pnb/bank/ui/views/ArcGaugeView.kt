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
        val targetScore = score.coerceIn(300, 900).toFloat()
        android.animation.ValueAnimator.ofFloat(300f, targetScore).apply {
            duration = 1500
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener { animation ->
                scoreValue = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        // Stroke size
        val stroke = 36f
        paint.strokeWidth = stroke

        // The circle diameter is width - stroke * 2 (to prevent edge clipping)
        val diameter = width - stroke * 2
        val left = stroke
        val top = stroke
        val right = width - stroke
        val bottom = top + diameter * 2f // Multiply by 2 so the center is at the bottom of our half-circle
        rectF.set(left, top, right, bottom)

        // Gradient color stops matching PNB theme (PNB Red -> Orange -> Green)
        val colors = intArrayOf(
            Color.parseColor("#D32F2F"), // Red
            Color.parseColor("#F57C00"), // Orange
            Color.parseColor("#388E3C")  // Green
        )

        // Set gradient from left to right of the arc
        val gradient = LinearGradient(
            rectF.left, rectF.top,
            rectF.right, rectF.top,
            colors,
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient

        // Draw track (translucent black outline)
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
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
