package com.hmman.photodecoration.widget.entity

import android.graphics.*
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.hmman.photodecoration.model.Layer
import com.hmman.photodecoration.util.Constants
import com.hmman.photodecoration.util.MathUtils
import com.hmman.photodecoration.util.PhotoUtils

abstract class MotionEntity(
    val layer: Layer,
    protected var canvasWidth: Int, protected var canvasHeight: Int,
    var deleteIcon: Bitmap
) {

    protected val matrix = Matrix()
    protected val realMatrix = Matrix()
    private var isSelected = false
    protected var holyScale = 0f
    protected var realHolyScale = 0f
    private val destPoints = FloatArray(10) // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0
    protected val srcPoints = FloatArray(10)

    @NonNull
    private var borderPaint = Paint()
    private var closePaint = Paint()
    open fun isSelected(): Boolean {
        return isSelected
    }

    open fun setIsSelected(isSelected: Boolean) {
        this.isSelected = isSelected
    }

    protected fun updateMatrix() {
        matrix.reset()
        val topLeftX: Float = layer.x * canvasWidth
        val topLeftY: Float = layer.y * canvasHeight
        val centerX = topLeftX + width * holyScale * 0.5f
        val centerY = topLeftY + height * holyScale * 0.5f
        // calculate params
        var rotationInDegree: Float = layer.rotationInDegrees
        var scaleX: Float = layer.scale
        val scaleY: Float = layer.scale
        if (layer.isFlipped) {
            // flip (by X-coordinate) if needed
            rotationInDegree *= -1.0f
            scaleX *= -1.0f
        }
        matrix.preScale(scaleX, scaleY, centerX, centerY)
        matrix.preRotate(rotationInDegree, centerX, centerY)
        matrix.preTranslate(topLeftX, topLeftY)
        matrix.preScale(holyScale, holyScale)
    }

    protected open fun updateRealMatrix() {
        realMatrix.reset()
        val topLeftX: Float = layer.x * PhotoUtils.getInstance(null).width
        val topLeftY: Float = layer.y * PhotoUtils.getInstance(null).height
        val centerX: Float = topLeftX + width * realHolyScale * 0.5f
        val centerY: Float = topLeftY + height * realHolyScale * 0.5f

        var rotationInDegree: Float = layer.rotationInDegrees
        var scaleX: Float = layer.scale
        val scaleY: Float = layer.scale
        if (layer.isFlipped) {
            rotationInDegree *= -1.0f
            scaleX *= -1.0f
        }

        realMatrix.preScale(scaleX, scaleY, centerX, centerY)
        realMatrix.preRotate(rotationInDegree, centerX, centerY)
        realMatrix.preTranslate(topLeftX, topLeftY)
        realMatrix.preScale(realHolyScale, realHolyScale)
    }

    fun absoluteCenterX(): Float {
        val topLeftX: Float = layer.x * canvasWidth
        return topLeftX + width * holyScale * 0.5f
    }

    fun absoluteCenterY(): Float {
        val topLeftY: Float = layer.y * canvasHeight
        return topLeftY + height * holyScale * 0.5f
    }

    fun absoluteCenter(): PointF {
        val topLeftX: Float = layer.x * canvasWidth
        val topLeftY: Float = layer.y * canvasHeight
        val centerX = topLeftX + width * holyScale * 0.5f
        val centerY = topLeftY + height * holyScale * 0.5f
        return PointF(centerX, centerY)
    }

    fun moveToCanvasCenter() {
        moveCenterTo(PointF(canvasWidth * 0.5f, canvasHeight * 0.5f))
    }

    fun moveCenterTo(moveToCenter: PointF) {
        val currentCenter = absoluteCenter()
        layer.postTranslate(
            1.0f * (moveToCenter.x - currentCenter.x) / canvasWidth,
            1.0f * (moveToCenter.y - currentCenter.y) / canvasHeight
        )
    }

    private val pA = PointF()
    private val pB = PointF()
    private val pC = PointF()
    private val pD = PointF()

    fun pointInLayerRect(point: PointF): Boolean {
        updateMatrix()
        // map rect vertices
        matrix.mapPoints(destPoints, srcPoints)
        pA.x = destPoints[0]
        pA.y = destPoints[1]
        pB.x = destPoints[2]
        pB.y = destPoints[3]
        pC.x = destPoints[4]
        pC.y = destPoints[5]
        pD.x = destPoints[6]
        pD.y = destPoints[7]
        return MathUtils.pointInTriangle(point, pA, pB, pC)
                || MathUtils.pointInTriangle(point, pA, pD, pC)

    }
    fun pointClose(point: PointF):Boolean{
        updateMatrix()
        // map rect vertices
        matrix.mapPoints(destPoints, srcPoints)
        Log.d(TAG,destPoints[2].toString()+"----" +destPoints[3].toString() +":"+ (destPoints[2]+50).toString() + "----"+ (destPoints[2]-50) +"----"+ destPoints[3] +50 +"----"+ (destPoints[3] - 50))
        return point.x <=  destPoints[2]+100 && point.x >=  destPoints[2]-100 && point.y <= destPoints[3] +100 && point.y >= destPoints[3] - 100


    }
    fun draw(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?) {
        updateMatrix()
        canvas.save()
        drawContent(canvas, drawingPaint)
        if (isSelected) { // get alpha from drawingPaint
            val storedAlpha = borderPaint.alpha
            val closeStoredAlpha = closePaint.alpha
            if (drawingPaint != null) {
                borderPaint.alpha = drawingPaint.alpha
                closePaint.alpha = drawingPaint.alpha
            }
            drawSelectedBg(canvas)

            drawCloseBg(canvas)

            // restore border alpha
            borderPaint.alpha = storedAlpha
            closePaint.alpha = closeStoredAlpha
        }
        canvas.restore()
    }

    fun drawReal(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?) {
        updateRealMatrix()
        canvas.save()
        drawRealContent(canvas, drawingPaint)
        if (isSelected()) {
            val storedAlpha = borderPaint.alpha
            if (drawingPaint != null) {
                borderPaint.alpha = drawingPaint.alpha
            }
            drawSelectedBg(canvas)
            borderPaint.alpha = storedAlpha
        }
        canvas.restore()
    }

    private fun drawSelectedBg(canvas: Canvas) {
        matrix.mapPoints(destPoints, srcPoints)
        canvas.drawLines(destPoints, 0, 8, borderPaint)
        canvas.drawLines(destPoints, 2, 8, borderPaint)
    }
    private fun drawCloseBg(canvas: Canvas) {
        val destPoints = destPoints
        val matrix = matrix
        val imageEntity = this
        matrix.reset()

        matrix.postTranslate(destPoints[2] - imageEntity.deleteIcon.width/2, destPoints[3] - imageEntity.deleteIcon.height/2)
        canvas.drawCircle(destPoints[2], destPoints[3], Constants.RADIUS_DELETE_ICON, borderPaint)

        canvas.drawBitmap(imageEntity.deleteIcon, matrix, borderPaint)
    }
    fun setBorderPaint(@NonNull borderPaint: Paint) {
        this.borderPaint = borderPaint
    }
    fun setClosePaint(@NonNull closePaint: Paint) {
        this.closePaint = closePaint
    }
    protected abstract fun drawContent(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?)
    protected abstract fun drawRealContent(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?)
    abstract val width: Int
    abstract val height: Int

    open fun release() {

    }

    @Throws(Throwable::class)
    protected fun finalize() {
        try {
            release()
        } finally {
        }
    }

    companion object {
        private val TAG = MotionEntity::class.simpleName
    }
}