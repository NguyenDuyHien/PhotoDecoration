package com.hmman.photodecoration.widget.entity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.annotation.IntRange
import androidx.annotation.NonNull
import com.hmman.photodecoration.model.Layer
import com.hmman.photodecoration.util.PhotoUtils
import kotlin.math.min

class ImageEntity(
    @NonNull layer: Layer,
    @NonNull val bitmap: Bitmap,
    @IntRange(from = 1) canvasWidth: Int,
    @IntRange(from = 1) canvasHeight: Int,
    deleteIcon: Bitmap
) : MotionEntity(layer, canvasWidth, canvasHeight, deleteIcon) {

    init {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        val widthAspect = 1F * canvasWidth/width
        val heightAspect = 1F * canvasHeight/height
        val realWidthAspect = 1F * PhotoUtils.getInstance(null).width/width
        val realHeightAspect = 1F * PhotoUtils.getInstance(null).height/height

        holyScale = min(widthAspect, heightAspect)
        realHolyScale = min(realWidthAspect, realHeightAspect)

        srcPoints[0] = 0f
        srcPoints[1] = 0f
        srcPoints[2] = width
        srcPoints[3] = 0f
        srcPoints[4] = width
        srcPoints[5] = height
        srcPoints[6] = 0f
        srcPoints[7] = height
        srcPoints[8] = 0f
    }

    override fun drawContent(canvas: Canvas, drawingPaint: Paint?) {
        canvas.drawBitmap(bitmap, matrix, drawingPaint)
    }

    override fun drawRealContent(canvas: Canvas, drawingPaint: Paint?) {
        canvas.drawBitmap(bitmap, realMatrix, drawingPaint)
    }

    override val width: Int
        get() = bitmap.width
    override val height: Int
        get() = bitmap.height

    override fun release() {
        if (!bitmap.isRecycled) bitmap.recycle()
    }
}