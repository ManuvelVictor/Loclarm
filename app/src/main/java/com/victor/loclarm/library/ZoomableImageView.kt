package com.victor.loclarm.library

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private var scale = 1f
    private val gestureDetector: GestureDetectorCompat
    private var isDragging = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val minScale = 1f
    private val maxScale = 4f

    init {
        scaleType = ScaleType.FIT_CENTER
        gestureDetector = GestureDetectorCompat(context, GestureListener())
        setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            handleTouch(event)
            true
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyFitCenter()
    }

    private fun applyFitCenter() {
        if (drawable != null) {
            val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
            val drawableRect = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
            matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER)
            imageMatrix = matrix
        }
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY
                if (abs(dx) > 5 || abs(dy) > 5) {
                    isDragging = true
                }
                if (isDragging) {
                    matrix.set(savedMatrix)
                    matrix.postTranslate(dx, dy)

                    constrainMatrix()

                    imageMatrix = matrix
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isDragging) {
                    performClick()
                }
            }
        }
    }

    private fun constrainMatrix() {
        val rect = RectF(0f, 0f, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
        matrix.mapRect(rect)

        val viewRect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        // Ensure the image is within the view bounds
        val dx: Float = when {
            rect.width() <= viewRect.width() -> (viewRect.width() - rect.width()) / 2 - rect.left
            rect.left > viewRect.left -> -rect.left
            rect.right < viewRect.right -> viewRect.right - rect.right
            else -> 0f
        }

        val dy: Float = when {
            rect.height() <= viewRect.height() -> (viewRect.height() - rect.height()) / 2 - rect.top
            rect.top > viewRect.top -> -rect.top
            rect.bottom < viewRect.bottom -> viewRect.bottom - rect.bottom
            else -> 0f
        }

        matrix.postTranslate(dx, dy)
        imageMatrix = matrix
    }

    private fun doubleTapZoom() {
        val currentScale = getScale()
        val targetScale = if (currentScale < maxScale) maxScale else minScale

        val scaleMatrix = Matrix()
        scaleMatrix.set(matrix)
        scaleMatrix.postScale(targetScale / currentScale, targetScale / currentScale, width / 2f, height / 2f)
        matrix.set(scaleMatrix)

        constrainMatrix()

        imageMatrix = matrix
    }

    private fun getScale(): Float {
        val values = FloatArray(9)
        matrix.getValues(values)
        return values[Matrix.MSCALE_X]
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            doubleTapZoom()
            return true
        }
    }
}