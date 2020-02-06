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

        //In case Text only on character: Paint.MeasureText return wrong size
        val textWidth = max(
            getMaxText(textLayer.text!!),
            textPaint.textSize.toInt()
        )
        val boundsWidth: Int = textWidth
        
        // Set initial scale for Text
        val initialScale = if (boundsWidth.toFloat() / canvasWidth > TextLayer.Limits.MIN_SCALE) {
            boundsWidth.toFloat() / canvasWidth
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
        val cloneTextLayer = textLayer.cloneTextLayer()
        cloneTextLayer.x = textLayer.x
        cloneTextLayer.y = textLayer.y
        cloneTextLayer.rotationInDegrees = textLayer.rotationInDegrees
//        cloneTextLayer.scale = textLayer.scale
        cloneTextLayer.scale = textLayer.initialScale()

        val entity = TextEntity(
            cloneTextLayer,
            canvasWidth,
            canvasHeight,
            fontProvider.clone(),
            name,
            deleteIcon,
            context
        )
        BorderUtil.initEntityBorder(entity, context)
        BorderUtil.initEntityIconBackground(entity, context)
        if (entity.layer.x == 0f && entity.layer.y == 0f) {
            entity.moveToCanvasCenter()
            entity.layer.scale = entity.layer.initialScale()
        }
        updateEntity()
        return entity
    }

    fun updateEntity() {
        updateEntity(true)
        updateRealEntity(true)
    }

    private fun getMaxText(text: String): Int {
        val a = text.lines()
        var maxLength = textPaint.measureText("")
        for (i in a) {
            if (textPaint.measureText(i) > maxLength) maxLength = textPaint.measureText(i)
        }
        return maxLength.toInt()
    }

//    private fun getLongestLine(text:String): String {
//        var longestLine = ""
//        val textWidth = textPaint.measureText(text)
//        when {
//            canvasWidth > textWidth -> {
//                longestLine = text
//            }
//            else -> {
//                longestLine = text.substring(0, numOfCharInOneLine(text))
//            }
//        }
//        return longestLine
//    }

    fun numOfCharInOneLine(text: String): Int {
        var numOfChar: Int = 0
        for (i in 0..text.length){
            if (textPaint.measureText(text.substring(0, i)).toInt() > canvasWidth){
                numOfChar = i
                break
            }
        }
        return numOfChar
    }

    override val width: Int = if (bitmap != null) bitmap!!.width else 0
    override val height: Int = if (bitmap != null) bitmap!!.height else 0
}