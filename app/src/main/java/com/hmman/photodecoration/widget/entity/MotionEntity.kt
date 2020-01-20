package com.hmman.photodecoration.widget.entity

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.hmman.photodecoration.model.Layer
import com.hmman.photodecoration.util.MathUtils

abstract class MotionEntity(
    val layer: Layer,
    protected var canvasWidth: Int, protected var canvasHeight: Int
) {

    protected val matrix = Matrix()
    private var isSelected = false
    protected var holyScale = 0f
    private val destPoints = FloatArray(10) // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0
    protected val srcPoints = FloatArray(10)

    @NonNull
    private var borderPaint = Paint()

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
        if (layer.isFlipped) { // flip (by X-coordinate) if needed
            rotationInDegree *= -1.0f
            scaleX *= -1.0f
        }
        matrix.preScale(scaleX, scaleY, centerX, centerY)
        matrix.preRotate(rotationInDegree, centerX, centerY)
        matrix.preTranslate(topLeftX, topLeftY)
        matrix.preScale(holyScale, holyScale)
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

    fun draw(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?) {
        updateMatrix()
        canvas.save()
        drawContent(canvas, drawingPaint)
        if (isSelected) { // get alpha from drawingPaint
            val storedAlpha = borderPaint.alpha
            if (drawingPaint != null) {
                borderPaint.alpha = drawingPaint.alpha
            }
            drawSelectedBg(canvas)
            // restore border alpha
            borderPaint.alpha = storedAlpha
        }
        canvas.restore()
    }

    private fun drawSelectedBg(canvas: Canvas) {
        matrix.mapPoints(destPoints, srcPoints)
        canvas.drawLines(destPoints, 0, 8, borderPaint)
        canvas.drawLines(destPoints, 2, 8, borderPaint)
    }

    fun setBorderPaint(@NonNull borderPaint: Paint) {
        this.borderPaint = borderPaint
    }

    protected abstract fun drawContent(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?)
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

}