package com.hien.photodecoration.widget.entity

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
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur
import com.hien.photodecoration.model.TextLayer
import com.hien.photodecoration.util.BorderUtil
import com.hien.photodecoration.util.FontProvider
import com.hien.photodecoration.util.PhotoUtils
import kotlin.math.max
import kotlin.math.min

@RequiresApi(Build.VERSION_CODES.M)
class TextEntity(
    @NonNull textLayer: TextLayer,
    @IntRange(from = 1) canvasWidth: Int,
    @IntRange(from = 1) canvasHeight: Int,
    @NonNull val fontProvider: FontProvider,
    name: String,
    context: Context
) : MotionEntity(textLayer, canvasWidth, canvasHeight, name) {

    private val textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var bitmap: Bitmap? = null
    private val textLayer = textLayer
    private val context = context
    private var singleText = false
    private var init = false

    init {
        init = true
        updateEntity(false)
        updateRealEntity(false)
    }

    private fun updateEntity(moveToPreviousCenter: Boolean) { // save previous center
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
        val heightAspect = 1f * canvasHeight / height
        // for text we always match text width with parent width
        holyScale = min(widthAspect, heightAspect)
//        holyScale = widthAspect
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
//            moveToCanvasCenter()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateRealEntity(moveToPreviousCenter: Boolean) { // save previous center
        val oldCenter = absoluteCenter()

        val width = bitmap!!.width.toFloat()
        val height = bitmap!!.height.toFloat()
        val widthAspect: Float = 1.0f * PhotoUtils.getInstance(null).width / width
        val heightAspect: Float = 1f * PhotoUtils.getInstance(null).height / height
        // for text we always match text width with parent width
        realHolyScale = min(heightAspect, widthAspect)

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
        textPaint.typeface = fontProvider.getTypeface(textLayer.font!!.typeface)
        textPaint.textSize =
            textLayer.font!!.size * max(canvasHeight, canvasWidth) + 20


        textPaint.color = textLayer.font?.color!!

        //In case Text only on character: Paint.MeasureText return wrong size
//        val textWidth = max(
//            getMaxText(textLayer.text!!),
//            textPaint.textSize.toInt()
//        )
        var boundsWidth = getMaxText(textLayer.text!!)
        if (boundsWidth.toFloat() / max(canvasHeight, canvasWidth) < TextLayer.Limits.MIN_SCALE) {
            textLayer.setInitialScale(TextLayer.Limits.MIN_SCALE)
        } else {
            textLayer.setInitialScale(boundsWidth.toFloat() / max(canvasHeight, canvasWidth))
            if (init) {
                textLayer.dynamicMinScale =
                    boundsWidth.toFloat() / max(canvasHeight, canvasWidth) / 2
                textLayer.dynamicMaxScale =
                    boundsWidth.toFloat() / max(canvasHeight, canvasWidth) * 3
            }
        }

        if (singleText) {
            boundsWidth *= 2
        }

        val sl = StaticLayout.Builder.obtain(
            textLayer.text.toString(),
            0,
            textLayer.text!!.length,
            textPaint,
            boundsWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1f)
            .build()
        val boundsHeight = sl.height
//        val bmpHeight = (canvasHeight * max(
//            TextLayer.Limits.MIN_BITMAP_HEIGHT,
//            1.0f * boundsHeight / canvasHeight
//        )).toInt()
        val bmpHeight = boundsHeight

        var bmp: Bitmap
        if (reuseBmp != null && reuseBmp.width == boundsWidth && reuseBmp.height == bmpHeight) {
            bmp = reuseBmp
            bmp.eraseColor(Color.TRANSPARENT) // erase color when reusing
        } else {
            bmp = Bitmap.createBitmap(boundsWidth, bmpHeight, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(bmp)
        canvas.save()

        if (boundsHeight < bmpHeight) {
            val textYCoordinate = ((bmpHeight - boundsHeight) / 2).toFloat()
            canvas.translate(0f, textYCoordinate)
        }

        sl.draw(canvas)
        canvas.restore()

        bmp = blurBitmap(bmp, 1.0f, context)
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
        cloneTextLayer.scale = textLayer.initialScale()

        val entity = TextEntity(
            cloneTextLayer,
            canvasWidth,
            canvasHeight,
            fontProvider.clone(),
            name,
            context
        )
        BorderUtil.initEntityBorder(entity, context)
        BorderUtil.initEntityIconBackground(entity, context)
        if (entity.layer.x == 0f && entity.layer.y == 0f) {
            entity.moveToCanvasCenter()
            entity.layer.scale = entity.layer.initialScale()
        }
//        updateEntity()
        return entity
    }

    fun updateEntity() {
        updateEntity(true)
        updateRealEntity(true)
    }

    private fun getMaxText(text: String): Int {
        val lines = text.lines()
        if (text.count() == 1) {
            singleText = true
        }
        var maxLength = textPaint.measureText("")
        for (i in lines) {
            if (textPaint.measureText(i) > maxLength) maxLength = textPaint.measureText(i)
        }
        return (maxLength.toInt())
    }

    fun pxToDp(px: Int): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    // blur bitmap to remove jagged edge
    private fun blurBitmap(bitmap: Bitmap, radius: Float, context: Context): Bitmap {
        //Create renderscript
        val rs = RenderScript.create(context)

        //Create allocation from Bitmap
        val allocationIn = Allocation.createFromBitmap(rs, bitmap)

        val t = allocationIn.type

        //Create allocation with the same type
        val allocationOut = Allocation.createTyped(rs, t)

        //Create script
        val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        //Set blur radius (maximum 25.0)
        blurScript.setRadius(radius)
        //Set input for script
        blurScript.setInput(allocationIn)
        //Call script for output allocation
        blurScript.forEach(allocationOut)

        //Copy script result into bitmap
        allocationOut.copyTo(bitmap)

        //Destroy everything to free memory
        allocationIn.destroy()
        allocationOut.destroy()
        blurScript.destroy()
        t.destroy()
        rs.destroy()
        return bitmap
    }

    override val width: Int = if (bitmap != null) bitmap!!.width else 0
    override val height: Int = if (bitmap != null) bitmap!!.height else 0
}