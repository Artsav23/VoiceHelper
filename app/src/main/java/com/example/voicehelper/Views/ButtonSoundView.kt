package com.example.voicehelper.Views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.*
import android.view.animation.LinearInterpolator

class ButtonSoundView (context: Context, attributeSet: AttributeSet): View(context, attributeSet) {
    private var soundVolume = 0.8f
    private var isAnimationRunning = false

    private val paint = Paint().apply {
        color = Color.argb(60, 255,255,255)
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f

        val radius1 = soundVolume * width / 4.5f
        val radius2 = soundVolume * width / 5.5f
        val radius3 = soundVolume * width / 6.5f

        canvas.apply {
            drawColor(Color.BLACK)
            drawCircle(centerX, centerY, radius1, paint)
            drawCircle(centerX, centerY, radius2, paint)
            drawCircle(centerX, centerY, radius3, paint)
        }
    }

    fun startAnimation(volume: Float) {
        val value = if (volume <= 0.5f)
            1f + volume
        else if (volume/7 < 1f)
            volume / 7 + 1f
        else volume / 7

        if (!isAnimationRunning) {
            val animator = ValueAnimator.ofFloat(soundVolume, kotlin.math.abs(value), soundVolume)
            animator.duration = 1000
            animator.addUpdateListener { animation ->
                soundVolume = animation.animatedValue as Float
                invalidate()
            }
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
            animator.start()
            isAnimationRunning = true
        }
    }
}