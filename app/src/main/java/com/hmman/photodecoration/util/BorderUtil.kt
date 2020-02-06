package com.hmman.photodecoration.util

import android.content.Context
import android.graphics.Paint
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import com.hmman.photodecoration.R
import com.hmman.photodecoration.widget.entity.MotionEntity

class BorderUtil {
    companion object {
        fun initEntityBorder(@NonNull entity: MotionEntity, context: Context) { // init stroke
            val strokeSize = context.resources.getDimensionPixelSize(R.dimen.stroke_size)
            val borderPaint = Paint()
            borderPaint.strokeWidth = strokeSize.toFloat()
            borderPaint.isAntiAlias = true
            borderPaint.color = ContextCompat.getColor(context, R.color.stroke_color)
            entity.setBorderPaint(borderPaint)
        }

        fun initEntityIconBackground(entity: MotionEntity, context: Context) {
            val iconBackground = Paint()
            iconBackground.isAntiAlias = true
            iconBackground.style = Paint.Style.FILL
            iconBackground.color = ContextCompat.getColor(context, R.color.fill_color)
            entity.setIconBackground(iconBackground)
        }
    }
}