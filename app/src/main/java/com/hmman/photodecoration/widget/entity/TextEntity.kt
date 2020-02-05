package com.hmman.photodecoration.widget.entity

import android.content.Context
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
import com.hmman.photodecoration.model.TextLayer
import com.hmman.photodecoration.util.BorderUtil
import com.hmman.photodecoration.util.FontProvider
import com.hmman.photodecoration.util.PhotoUtils
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.M)
class TextEntity(
    @NonNull textLayer: TextLayer,
    @IntRange(from = 1) canvasWidth: Int,
    @IntRange(from = 1) canvasHeight: Int,
    @NonNull val fontProvider: FontProvider,
    name: String,
    deleteIcon: Bitmap,
    context: Context
) : MotionEntity(textLayer, canvasWidth, canvasHeight, deleteIcon, name) {

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var bitmap: Bitmap? = null
    private val textLayer = textLayer
    private val context = context

    init {
        updateEntity(false)
        updateRealEntity(false)
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
        val widthAspect = 1F * canvasWidth / width
        val heightAspect = 1F * canvasHeight / height
        // for text we always match text width with parent width
//        holyScale = min(widthAspect, heightAspect)
        holyScale = widthAspect
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
    private fun updateRealEntity(moveToPreviousCenter: Boolean) { // save previous center
        val oldCenter = absoluteCenter()

        val width = bitmap!!.width.toFloat()
        val height = bitmap!!.height.toFloat()
        val widthAspect: Float = 1.0f * PhotoUtils.getInstance(null).width / width

        // for text we always match text width with parent width
        realHolyScale = widthAspect

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
        srcPoints[8] = 0f
        if (moveToPreviousCenter) { // move to previous center
            moveCenterTo(oldCenter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @NonNull
    private fun createBitmap(@NonNull textLayer: TextLayer, @Nullable reuseBmp: Bitmap?): Bitmap? {

        textPaint.style = Paint.Style.FILL
        textPaint.textSize = textLayer.font!!.size * canvasWidth
        textPaint.color = textLayer.font?.color!!

        //In case Text only one character: Paint.MeasureText return wrong size
        val textWidth =
            max(
//                textPaint.measureText(getMaxText(textLayer.text!!)).toInt(),
                getMaxText(textLayer.text!!),
                textPaint.textSize.toInt()
            )

        if (PhotoUtils.getInstance(null).boundsWidth == 0f) PhotoUtils.getInstance(null)
            .boundsWidth = textWidth.toFloat()
        else PhotoUtils.getInstance(null).boundsWidth *= textLayer.scale
        val boundsWidth: Int = textWidth
//        var boundsWidth: Int
//        boundsWidth = if (textWidth > canvasWidth) {
//            textWidth
//        } else {
//            min(canvasWidth, textWidth)
//        }

        // Set initial scale for Text
        val initialScale = if (boundsWidth * 1f / canvasWidth > TextLayer.Limits.MIN_SCALE) {
            boundsWidth * 1f / canvasWidth
        } else {
            TextLayer.Limits.MIN_SCALE
        }

        textLayer.setInitialScale(initialScale)

        val sl = StaticLayout.Builder.obtain(
            textLayer.text.toString(),
            0,
            textLayer.text!!.length,
            textPaint,
            boundsWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setIncludePad(true)
            .setLineSpacing(1f, 1f)
            .build()

        val boundsHeight = sl.height
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

        sl.draw(canvas)
        canvas.restore()
        return bmp
    }

    fun getLayer(): TextLayer {
        return layer as TextLayer
    }

    override fun drawContent(canvas: Canvas, drawingPaint: Paint?) {
        if (bitmap != null) {
            updateEntity(false)
            canvas.drawBitmap(bitmap!!, matrix, drawingPaint)
        }
    }

    override fun drawRealContent(canvas: Canvas, drawingPaint: Paint?) {
        if (bitmap != null) {
            canvas.drawBitmap(bitmap!!, realMatrix, drawingPaint)
        }
    }

    override fun release() {
        if (bitmap != null && !bitmap!!.isRecycled) {
            bitmap!!.recycle()
        }
    }

    override fun clone(): MotionEntity {
        val entity = TextEntity(
            textLayer.cloneTextLayer(),
            canvasWidth,
            canvasHeight,
            fontProvider.clone(),
            name,
            deleteIcon,
            context
        )
        BorderUtil.initEntityBorder(entity, context)
        BorderUtil.initEntityIconBackground(entity, context)
        entity.moveToCanvasCenter()
        entity.layer.scale = entity.layer.initialScale()
        return entity
    }

    fun updateEntity() {
        updateEntity(true)
        updateRealEntity(true)
    }

    private fun getMaxText(text: String): Int {
//        textPaint.measureText(getMaxText(textLayer.text!!)).toInt(),
//        val textPaint: TextPaint
        if (textPaint.measureText(text) > canvasWidth) return canvasWidth
//        val a = text.split(" ")
        val a = text.lines()
        var maxLength = ""
        for (i in a) {
            if (i.length > maxLength.length) maxLength = i
        }

        return textPaint.measureText(maxLength).toInt()
    }


    override val width: Int = if (bitmap != null) bitmap!!.width else 0
    override val height: Int = if (bitmap != null) bitmap!!.height else 0
}