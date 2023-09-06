package com.example.voicehelper.Views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.util.AttributeSet
import android.view.View

class LineSoundView(context: Context, attributeSet: AttributeSet?): View(context, attributeSet) {
    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    private var soundVolume = 3f
    private var isAnimationRunning = false
    private var  randomStartY = mutableListOf(-10f, -3f, 10f, 15f, 5f, 0f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerY = height / 2f
        canvas.drawColor(Color.BLACK)
        repeat(2) {
            canvas.drawPath(drawLineVolumeUp(centerY, it, 3.5), paint)
            canvas.drawPath(drawLineVolumeDown(centerY, it + 2, 1.5), paint)
            canvas.drawPath(drawLineVolumeUp(centerY, it + 4, 1.0), paint)
            canvas.drawPath(drawLineVolumeDown(centerY, it + 4, 1.0), paint)
        }

    }

    private fun drawLineVolumeDown(centerY: Float, index: Int, difference: Double): Path {
        val value = soundVolume * 8
        val path = Path()
        val startY = randomStartY[index]
        path.moveTo(0f, centerY + startY)

        val controlY1 = (centerY - value  + startY)
        val controlY2 = (centerY - value  + startY)

        val x1 = 0f
        val x2 = (width / 6).toFloat()
        val x3 = (width / 3).toFloat() + width / 65
        val x4 = (width / 2).toFloat()
        val x5 = (2 * width / 3).toFloat() - width / 65
        val x6 = (5 * width / 6).toFloat()
        val x7 = width.toFloat()

        val y1 = centerY + startY
        val y2 = centerY + value * (difference - 1).toFloat() + startY
        val y3 = (centerY + value * (difference + 1) + startY).toFloat()

        path.cubicTo(
            x1, y1,
            x2, controlY1,
            x3, y2
        )
        path.cubicTo(
            x3, y2,
            x4, y3,
            x5, y2
        )
        path.cubicTo(
            x5, y2,
            x6, controlY2,
            x7, y1
        )

        return path
    }

    private fun drawLineVolumeUp(centerY: Float, index: Int, difference: Double): Path {
        val path = Path()
        val value = soundVolume * 8
        val startY = randomStartY[index]
        path.moveTo(0f, centerY + startY)

        val controlY1 = (centerY + value  + startY)
        val controlY2 = (centerY + value  + startY)

        val x1 = 0f
        val x2 = (width / 6).toFloat()
        val x3 = (width / 3).toFloat() + width / 65
        val x4 = (width / 2).toFloat()
        val x5 = (2 * width / 3).toFloat() - width / 65
        val x6 = (5 * width / 6).toFloat()
        val x7 = width.toFloat()

        val y1 = centerY + startY
        val y2 = (centerY - value / (difference * 2 ) + startY).toFloat()
        val y3 = (centerY - value / (difference * 0.5) + startY).toFloat()

        path.cubicTo(
            x1, y1,
            x2, controlY1,
            x3, y2
        )
        path.cubicTo(
            x3, y2,
            x4, y3,
            x5, y2
        )
        path.cubicTo(
            x5, y2,
            x6, controlY2,
            x7, y1
        )

        return path
    }

    fun startAnimation(volume: Float) {
        if (!isAnimationRunning) {
            val animator = ValueAnimator.ofFloat(soundVolume, kotlin.math.abs(volume))
            animator.duration = 250
            animator.addUpdateListener { animation ->
                soundVolume = animation.animatedValue as Float
                invalidate()
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    Handler().postDelayed({isAnimationRunning = false
                        val randomDivider = (7..10).random()
                        val resetVolume = if (volume != 0f) kotlin.math.abs(volume) / randomDivider
                        else 0f
                        val resetAnimator = ValueAnimator.ofFloat(soundVolume, resetVolume)
                        resetAnimator.duration = 150
                        resetAnimator.addUpdateListener { animation ->
                            soundVolume = animation.animatedValue as Float
                            invalidate()
                        }
                        resetAnimator.start()}, 0)
                }
            })
            animator.start()
            isAnimationRunning = true
        }
    }
}
