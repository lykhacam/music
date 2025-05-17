package com.example.myapplication.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class CircularBarVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var fftBytes: ByteArray? = null
    private val barCount = 120
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    fun updateFFT(bytes: ByteArray) {
        fftBytes = bytes.copyOf()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bytes = fftBytes ?: return

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.75f
        val angleStep = (2 * Math.PI / barCount).toFloat()

        for (i in 0 until barCount) {
            val fftIndex = 2 + i * 2
            if (fftIndex + 1 >= bytes.size) continue

            val re = bytes[fftIndex].toInt()
            val im = bytes[fftIndex + 1].toInt()
            val magnitude = sqrt((re * re + im * im).toDouble()).toFloat()

            val normalized = (magnitude / 128f).coerceIn(0.1f, 2.5f)
            val barLength = normalized * 120f

            val angle = i * angleStep
            val startX = centerX + cos(angle) * radius
            val startY = centerY + sin(angle) * radius
            val stopX = centerX + cos(angle) * (radius + barLength)
            val stopY = centerY + sin(angle) * (radius + barLength)

            canvas.drawLine(startX, startY, stopX, stopY, paint)
        }
    }
}
