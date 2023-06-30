package com.example.voicehelper

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class LineSoundView(context: Context, attributeSet: AttributeSet?): View(context, attributeSet) {
    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    private var soundVolume = 51f
    private var isAnimationRunning = false
    private var  randomStartY = MutableList(6){(1..40).random()}

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val centerY = height / 2
        canvas?.drawColor(Color.BLACK)
        repeat(3) {
            canvas?.drawPath(drawLineVolumeUp(centerY, it), paint)
        }
        repeat(3) {
            canvas?.drawPath(drawLineVolumeDown(centerY, it), paint)
        }
    }

    private fun drawLineVolumeDown(centerY: Int, index: Int): Path {
        val path = Path()
        val startY = randomStartY[index + 3]
        path.moveTo(0f, centerY.toFloat() + startY)
        path.quadTo(
            (width / 6).toFloat(),
            (centerY - soundVolume / 1.7 + startY).toFloat(),
            (width / 3).toFloat(),
            (centerY + startY).toFloat()
        )
        path.quadTo(
            (width / 2).toFloat(),
            (centerY + soundVolume * 2 + startY),
            (2 * width / 3).toFloat(),
            (centerY + startY).toFloat()
        )
        path.quadTo(
            (5 * width / 6).toFloat(),
            (centerY - soundVolume / 1.7 + startY).toFloat(),
            (width).toFloat(),
            (centerY + startY).toFloat()
        )
        return path
    }

    private fun drawLineVolumeUp(centerY: Int, index: Int): Path {
        val path = Path()
        val startY = randomStartY[index]
        path.moveTo(0f, centerY.toFloat() + startY)
        path.quadTo(
            (width / 6).toFloat(),
            (centerY + soundVolume / 1.7 + startY).toFloat(),
            (width / 3).toFloat(),
            (centerY + startY).toFloat()
        )
        path.quadTo(
            (width / 2).toFloat(),
            (centerY - soundVolume * 2 + startY),
            (2 * width / 3).toFloat(),
            (centerY + startY).toFloat()
        )
        path.quadTo(
            (5 * width / 6).toFloat(),
            (centerY + soundVolume / 1.7 + startY).toFloat(),
            (width).toFloat(),
            (centerY + startY).toFloat()
        )

        return path
    }

    fun startAnimation(volume: Float) {
        if (!isAnimationRunning) {
            val animator = ValueAnimator.ofFloat(soundVolume, volume)
            animator.duration = 200
            animator.addUpdateListener { animation ->
                soundVolume = animation.animatedValue as Float
                invalidate()
            }
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    isAnimationRunning = false
                }
            })
            animator.start()
            isAnimationRunning = true
        }
    }
}
