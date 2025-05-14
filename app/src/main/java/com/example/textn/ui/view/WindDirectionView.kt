package com.example.textn.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.example.textn.R

class WindDirectionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.WHITE // Default color, can be customized
    }

    private val arrowPath = Path()
    private var windDirection = 0 // 0 degrees is North, 90 is East, etc.

    init {
        // Read custom attributes from XML if provided
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.WindDirectionView)
        paint.color = typedArray.getColor(R.styleable.WindDirectionView_arrowColor, Color.WHITE)
        windDirection = typedArray.getInt(R.styleable.WindDirectionView_windDirection, 0)
        typedArray.recycle()
    }

    fun setWindDirection(direction: Int) {
        windDirection = direction
        invalidate() // Request redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val size = Math.min(width, height) * 0.4f

        // Create arrow path
        arrowPath.reset()

        // Rotate canvas according to wind direction
        // Meteorological convention: 0° = North, 90° = East, etc.
        // And the arrow points in the direction the wind is blowing TO (opposite from where it comes FROM)
        canvas.save()
        canvas.rotate(windDirection.toFloat(), centerX, centerY)

        // Draw arrow
        arrowPath.moveTo(centerX, centerY - size) // Top point
        arrowPath.lineTo(centerX - size / 2, centerY + size / 2) // Bottom left
        arrowPath.lineTo(centerX + size / 2, centerY + size / 2) // Bottom right
        arrowPath.close() // Complete triangle

        canvas.drawPath(arrowPath, paint)

        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 48 // Default size in pixels

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> Math.min(desiredSize, widthSize)
            else -> desiredSize
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(desiredSize, heightSize)
            else -> desiredSize
        }

        setMeasuredDimension(width, height)
    }
}