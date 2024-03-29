package com.hien.photodecoration.widget.entity

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.hien.photodecoration.model.Layer
import com.hien.photodecoration.util.MathUtils
import com.hien.photodecoration.util.PhotoUtils

abstract class MotionEntity(
    var layer: Layer,
    protected var canvasWidth: Int, protected var canvasHeight: Int,

    var name: String
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
    private var iconBackgroundPaint = Paint()
    open fun isSelected(): Boolean {
        return isSelected
    }

    open fun setIsSelected(isSelected: Boolean) {
        this.isSelected = isSelected
    }

    private fun updateMatrix() {
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

    fun pointInLayerRectIcon(point: PointF, iconEntity: IconEntity): Boolean {
        iconEntity.pA.x = iconEntity.destPoints[0]
        iconEntity.pA.y = iconEntity.destPoints[1]
        iconEntity.pB.x = iconEntity.destPoints[2]
        iconEntity.pB.y = iconEntity.destPoints[3]
        iconEntity.pC.x = iconEntity.destPoints[4]
        iconEntity.pC.y = iconEntity.destPoints[5]
        iconEntity.pD.x = iconEntity.destPoints[6]
        iconEntity.pD.y = iconEntity.destPoints[7]
        return MathUtils.pointInTriangle(point, iconEntity.pA, iconEntity.pB, iconEntity.pC) ||
                MathUtils.pointInTriangle(point, iconEntity.pA, iconEntity.pD, iconEntity.pC)
    }

    fun pointClose(point: PointF): Boolean {
        updateMatrix()
        // map rect vertices
        matrix.mapPoints(destPoints, srcPoints)
        Log.d(
            TAG,
            destPoints[2].toString() + "----" + destPoints[3].toString() + ":" + (destPoints[2] + 50).toString() + "----" + (destPoints[2] - 50) + "----" + destPoints[3] + 50 + "----" + (destPoints[3] - 50)
        )
        return point.x <= destPoints[2] + 100 && point.x >= destPoints[2] - 100 && point.y <= destPoints[3] + 100 && point.y >= destPoints[3] - 100


    }

    fun pointGesture(point: PointF): Boolean {
        updateMatrix()
        // map rect vertices
        matrix.mapPoints(destPoints, srcPoints)
//        Log.d(TAG,destPoints[2].toString()+"----" +destPoints[3].toString() +":"+ (destPoints[2]+50).toString() + "----"+ (destPoints[2]-50) +"----"+ destPoints[3] +50 +"----"+ (destPoints[3] - 50))
        return point.x <= destPoints[0] + 100 && point.x >= destPoints[0] - 100 && point.y <= destPoints[1] + 100 && point.y >= destPoints[1] - 100


    }

    //    fun draw(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?) {
//        updateMatrix()
//        canvas.save()
//        drawContent(canvas, drawingPaint)
//        if (isSelected) { // get alpha from drawingPaint
//            val storedAlpha = borderPaint.alpha
//            val closeStoredAlpha = closePaint.alpha
//            if (drawingPaint != null) {
//                borderPaint.alpha = drawingPaint.alpha
//                closePaint.alpha = drawingPaint.alpha
//            }
//            drawSelectedBg(canvas)
//
//            drawCloseBg(canvas)
//
//            // restore border alpha
//            borderPaint.alpha = storedAlpha
//            closePaint.alpha = closeStoredAlpha
//        }
//        canvas.restore()
//    }
    fun draw(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?, icons: MutableList<IconEntity>) {
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
            drawIcons(canvas, icons)
//            drawCloseBg(canvas)

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


    private fun drawIcons(canvas: Canvas, icons: MutableList<IconEntity>) {
        var x = 0f
        var y = 0f
        for (iconEntity in icons) {
            if (iconEntity.gravity == IconEntity.LEFT_TOP) {
                x = destPoints[0]
                y = destPoints[1]
            } else if (iconEntity.gravity == IconEntity.RIGHT_BOTTOM) {
                x = destPoints[4]
                y = destPoints[5]
            } else if (iconEntity.gravity == IconEntity.RIGHT_TOP) {
                x = destPoints[2]
                y = destPoints[3]
            }
            configIconMatrix(iconEntity, x, y)
            canvas.drawBitmap(iconEntity.bitmapIcon, iconEntity.matrix, null)
            canvas.drawCircle(x, y, iconEntity.radius, iconBackgroundPaint)
        }
    }

    private fun configIconMatrix(iconEntity: IconEntity, x: Float, y: Float) {
        iconEntity.matrix.reset()
        iconEntity.matrix.postTranslate(x - iconEntity.width / 2, y - iconEntity.height / 2)
        iconEntity.matrix.mapPoints(iconEntity.destPoints, iconEntity.srcPoints)
    }

    fun setBorderPaint(@NonNull borderPaint: Paint) {
        this.borderPaint = borderPaint
    }

    fun setClosePaint(@NonNull closePaint: Paint) {
        this.closePaint = closePaint
    }

    fun setIconBackground(iconBackground: Paint) {
        this.iconBackgroundPaint = iconBackground
    }

    protected abstract fun drawContent(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?)
    protected abstract fun drawRealContent(@NonNull canvas: Canvas, @Nullable drawingPaint: Paint?)
    abstract val width: Int
    abstract val height: Int
    abstract fun clone(): MotionEntity
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