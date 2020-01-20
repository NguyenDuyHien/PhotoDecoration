package com.hmman.photodecoration.widget.entity

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.annotation.IntRange
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import com.hmman.photodecoration.model.Layer
import com.hmman.photodecoration.model.TextLayer
import com.hmman.photodecoration.util.FontProvider
import kotlin.math.max
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.M)
class TextEntity(
    @NonNull textLayer: TextLayer,
    @IntRange(from = 1) canvasWidth: Int,
    @IntRange(from = 1) canvasHeight: Int,
    @NonNull val fontProvider: FontProvider
) : MotionEntity(textLayer, canvasWidth, canvasHeight) {

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var bitmap: Bitmap? = null

    init {
        updateEntity(false)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateEntity(moveToPreviousCenter: Boolean) { // save previous center
        val oldCenter = absoluteCenter()
        val newBmp: Bitmap = createBitmap(layer as TextLayer, bitmap)!!
        // recycle previous bitmap (if not reused) as soon as possible
        if (bitmap != null && bitmap != newBmp && !bitmap!!.isRecycled) {
            bitmap!!.recycle()
        }
        bitmap = newBmp
        val width: Float = bitmap!!.width.toFloat()
        val height: Float = bitmap!!.height.toFloat()
        val widthAspect = 1F * canvasWidth/width
        val heightAspect =1F * canvasHeight/height
        // for text we always match text width with parent width
        holyScale = min(widthAspect, heightAspect)

        // initial position of the entity
        srcPoints[0] = 0f
        srcPoints[1] = 0f
        srcPoints[2] = width
        srcPoints[3] = 0f
        srcPoints[4] = width
        srcPoints[5] = height
        srcPoints[6] = 0f
        srcPoints[7] = height
        srcPoints[8] = 0f

        if (moveToPreviousCenter) { // move to previous center
            moveCenterTo(oldCenter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @NonNull
    private fun createBitmap(@NonNull textLayer: TextLayer, @Nullable reuseBmp: Bitmap?): Bitmap? {
        val boundsWidth = canvasWidth

        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 45F
        textPaint.color =  textLayer.font?.color!!
//        textPaint.typeface = fontProvider.getTypeface(textLayer.font!!.typeface!!)

        @Suppress("DEPRECATION")
        val s2 = StaticLayout(
            textLayer.text,
            textPaint,
            boundsWidth,
            Layout.Alignment.ALIGN_CENTER,
            1f,
            1f,
            true
        )

        val boundsHeight = s2.height
        val bmpHeight = (canvasHeight * max(
            TextLayer.Limits.MIN_BITMAP_HEIGHT,
            1.0f * boundsHeight / canvasHeight
        )).toInt()

        val bmp: Bitmap
        if (reuseBmp != null && reuseBmp.width == boundsWidth && reuseBmp.height == bmpHeight) {
            bmp = reuseBmp
            bmp.eraseColor(Color.TRANSPARENT) // erase color when reusing
        } else {
            bmp = Bitmap.createBitmap(boundsWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bmp)
        canvas.save()

        if (boundsHeight < bmpHeight) {
            val textYCoordinate = (bmpHeight - boundsHeight) / 2.toFloat()
            canvas.translate(0f, textYCoordinate)
        }

        s2.draw(canvas)
        canvas.restore()
        return bmp
    }

    fun getLayer(): TextLayer {
        return layer as TextLayer
    }

    override fun drawContent(canvas: Canvas, drawingPaint: Paint?) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, matrix, drawingPaint)
        }
    }

    override fun release() {
        if (bitmap != null && !bitmap!!.isRecycled) {
            bitmap!!.recycle()
        }
    }

    override val width: Int = if (bitmap != null) bitmap!!.width else 0
    override val height: Int = if (bitmap != null) bitmap!!.height else 0

}