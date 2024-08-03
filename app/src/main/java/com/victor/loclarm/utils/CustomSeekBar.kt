package com.victor.loclarm.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.victor.loclarm.R


class CustomSeekBar : AppCompatSeekBar {
    private var textPaint: Paint? = null
    private var tickPaint: Paint? = null
    private val labels = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        textPaint!!.color = resources.getColor(R.color.black, null)
        textPaint!!.textSize = 30f

        tickPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        tickPaint!!.color = resources.getColor(R.color.black, null)
        tickPaint!!.strokeWidth = 6f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width - paddingLeft - paddingRight
        val height = height
        val step = width / 10f

        for (i in labels.indices) {
            val x = paddingLeft + i * step
            canvas.drawLine(x, (height - 60).toFloat(), x, (height - 50).toFloat(), tickPaint!!)
            val textWidth = textPaint!!.measureText(labels[i].toString())
            canvas.drawText(labels[i].toString(), x - textWidth / 2, (height).toFloat(), textPaint!!)
        }
    }
}
