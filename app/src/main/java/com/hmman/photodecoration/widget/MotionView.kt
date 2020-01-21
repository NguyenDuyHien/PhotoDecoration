package com.hmman.photodecoration.widget

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.hmman.photodecoration.R
import com.hmman.photodecoration.multitouch.MoveGestureDetector
import com.hmman.photodecoration.multitouch.RotateGestureDetector
import com.hmman.photodecoration.util.PhotoUtils
import com.hmman.photodecoration.widget.entity.MotionEntity
import kotlinx.android.synthetic.main.dialog_sticker.view.*
import java.io.IOException
import java.lang.Exception
import java.util.*

class MotionView : FrameLayout {
    interface Constants {
        companion object {
            const val SELECTED_LAYER_ALPHA = 0.15f
        }
    }

    interface MotionViewCallback {
        fun onEntitySelected(@Nullable entity: MotionEntity?)
        fun onEntityDoubleTap(@NonNull entity: MotionEntity?)
        fun onEntityUnselected()
    }

    // layers
    private val entities: MutableList<MotionEntity> =
        ArrayList()
    private val undoEntities: Stack<MotionEntity> = Stack()
//    private val resetEntities: MutableList<MotionEntity> = ArrayList<MotionEntity>()

    @Nullable
    var selectedEntity: MotionEntity? = null
        private set
    private var selectedLayerPaint: Paint? = null
    // callback
    @Nullable
    private var motionViewCallback: MotionViewCallback? = null
    // gesture detection
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var rotateGestureDetector: RotateGestureDetector? = null
    private var moveGestureDetector: MoveGestureDetector? = null
    private var gestureDetectorCompat: GestureDetectorCompat? = null
    private var motionEventTouch: MotionEvent? = null

    // constructors
    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context)
    }

    private fun init(@NonNull context: Context) { // I fucking love Android
        setWillNotDraw(false)
        selectedLayerPaint = Paint()
        selectedLayerPaint!!.alpha = (255 * Constants.SELECTED_LAYER_ALPHA).toInt()
        selectedLayerPaint!!.isAntiAlias = true
        selectedLayerPaint!!.color = Color.BLUE
        // init listeners
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        rotateGestureDetector = RotateGestureDetector(context, RotateListener())
        moveGestureDetector = MoveGestureDetector(context, MoveListener())
        gestureDetectorCompat = GestureDetectorCompat(context, TapsListener())
        setOnTouchListener(onTouchListener)
        updateUI()
    }

    fun getEntities(): List<MotionEntity> {
        return entities
    }

    fun setMotionViewCallback(@Nullable callback: MotionViewCallback?) {
        motionViewCallback = callback
    }

    fun addEntity(@Nullable entity: MotionEntity?) {
        if (entity != null) {
            entities.add(entity)
            selectEntity(entity, false)
        }
    }

    fun addEntityAndPosition(@Nullable entity: MotionEntity?) {
        if (entity != null) {
            initEntityBorder(entity)
            initEntityClose(entity)
            initialTranslateAndScale(entity)
            entities.add(entity)
            selectEntity(entity, true)
        }
    }

    private fun initEntityBorder(@NonNull entity: MotionEntity) { // init stroke
        val strokeSize = resources.getDimensionPixelSize(R.dimen.stroke_size)
        val borderPaint = Paint()
        borderPaint.strokeWidth = strokeSize.toFloat()
        borderPaint.isAntiAlias = true
        borderPaint.color = ContextCompat.getColor(context, R.color.stroke_color)
        entity.setBorderPaint(borderPaint)
    }
    private fun initEntityClose(@NonNull entity: MotionEntity) { // init stroke
        val strokeSize = resources.getDimensionPixelSize(R.dimen.stroke_size)
        val borderPaint = Paint()
        borderPaint.strokeWidth = strokeSize.toFloat()
        borderPaint.isAntiAlias = true
        borderPaint.color = ContextCompat.getColor(context, R.color.white)
        entity.setClosePaint(borderPaint)
//        }

    }
    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (selectedEntity != null) {
            selectedEntity!!.draw(canvas, selectedLayerPaint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawAllEntities(canvas)
        super.onDraw(canvas)
    }

    private fun drawAllEntities(canvas: Canvas) {
        for (i in entities.indices) {
            println(i)
            entities[i].draw(canvas, null)
        }
    }

    private fun drawAllRealEntities(canvas: Canvas) {
        for (i in entities.indices) {
            entities[i].drawReal(canvas, null)
        }
    }

    fun getFinalBitmap () : Bitmap? {
        selectEntity(null, false)

        try {
            val inputStream = context.contentResolver.openInputStream(PhotoUtils.getInstance(null).photoUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val preventRotateBitmap = PhotoUtils.getInstance(null).rotateImageIfRequired(bitmap, PhotoUtils.getInstance(null).photoUri)
            return if (preventRotateBitmap != null) {
                val finalBitmap = preventRotateBitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(finalBitmap)
                drawAllRealEntities(canvas)
                finalBitmap
            } else {
                val finalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(finalBitmap)
                drawAllRealEntities(canvas)
                finalBitmap
            }
        } catch (e: Exception) {

        }
        return null
    }

    private fun updateUI() {
        invalidate()
    }

    private fun handleTranslate(delta: PointF) {
        if (selectedEntity != null) {
            val newCenterX = selectedEntity!!.absoluteCenterX() + delta.x
            val newCenterY = selectedEntity!!.absoluteCenterY() + delta.y
            // limit entity center to screen bounds
            var needUpdateUI = false
            if (newCenterX >= 0 && newCenterX <= width) {
                selectedEntity!!.layer.postTranslate(delta.x / width, 0.0f)
                needUpdateUI = true
            }
            if (newCenterY >= 0 && newCenterY <= height) {
                selectedEntity!!.layer.postTranslate(0.0f, delta.y / height)
                needUpdateUI = true
            }
            if (needUpdateUI) {
                updateUI()
            }
        }
    }

    private fun initialTranslateAndScale(@NonNull entity: MotionEntity) {
        entity.moveToCanvasCenter()
        entity.layer.scale = entity.layer.initialScale()
    }

    private fun selectEntity(@Nullable entity: MotionEntity?, updateCallback: Boolean) {
        if (selectedEntity != null) {
            selectedEntity!!.setIsSelected(false)
        }
        entity?.setIsSelected(true)
        selectedEntity = entity
        invalidate()
        if (updateCallback && motionViewCallback != null) {
            motionViewCallback!!.onEntitySelected(entity)
        }
    }

    fun unselectEntity() {
        if (selectedEntity != null) {
            selectEntity(null, true)
            motionViewCallback!!.onEntityUnselected()
        }
    }

    @Nullable
    private fun findEntityAtPoint(x: Float, y: Float): MotionEntity? {
        var selected: MotionEntity? = null
        val p = PointF(x, y)
        for (i in entities.indices.reversed()) {
            if (entities[i].pointInLayerRect(p)) {
                selected = entities[i]
                break
            }
        }
        return selected
    }
    private fun closeSelectionOnTap(e: MotionEvent): Boolean {
        val p = PointF(e.x, e.y)
        if (selectedEntity != null  && selectedEntity!!.pointClose(p)) {
            deletedSelectedEntity()
            return true
        }
        return false

    }
    private fun updateSelectionOnTap(e: MotionEvent): Boolean {
        val entity = findEntityAtPoint(e.x, e.y)
        return if (entity != null) {
            selectEntity(entity, true)
            true
        } else {
            if(selectedEntity !=null){
                val p = PointF(e.x, e.y)
                if(selectedEntity!!.pointClose(p)){
                    deletedSelectedEntity()
                }
            }
            unselectEntity()
            false
        }
    }

    private fun updateOnLongPress(e: MotionEvent) {
        if (selectedEntity != null) {
            val p = PointF(e.x, e.y)
            if (selectedEntity!!.pointInLayerRect(p)) {
                bringLayerToFront(selectedEntity!!)
            }
        }
    }

    fun bringLayerToFront(@NonNull entity: MotionEntity) {
        if (entities.remove(entity)) {
            entities.add(entity)
            invalidate()
        }
    }

    fun moveEntityToBack(@Nullable entity: MotionEntity?) {
        if (entity == null) {
            return
        }
        if (entities.remove(entity)) {
            entities.add(0, entity)
            invalidate()
        }
    }

    fun flipSelectedEntity() {
        if (selectedEntity == null) {
            return
        }
        selectedEntity!!.layer.flip()
        invalidate()
    }

    fun moveSelectedBack() {
        moveEntityToBack(selectedEntity)
    }

    fun deletedSelectedEntity() {
        if (selectedEntity == null) {
            return
        }
        if (entities.remove(selectedEntity!!)) {
            selectedEntity!!.release()
            selectedEntity = null
            motionViewCallback!!.onEntityUnselected()
            invalidate()
        }
    }

    fun release() {
        for (entity in entities) {
            entity.release()
        }
    }

    fun redo() {
        if (undoEntities.size > 0) {
            entities.add(undoEntities.pop())
            updateUI()
        } else {
            Toast.makeText(this.context, "Nothing to Redo", Toast.LENGTH_SHORT).show()
        }
    }

    fun undo() {
        val lastItemPosition = entities.size - 1
        val listSize = entities.size
//        when {
//            isReseted -> {
//                entities.addAll(resetEntities)
//                resetEntities.clear()
//                updateUI()
//                isReseted = false
//            }
//            listSize > 0 -> {
//                undoEntities.push(entities.removeAt(lastItemPosition))
//                selectEntity(null, false)
//                updateUI()
//            }
//            else -> {
//                Toast.makeText(this.context, "Nothing to Undo", Toast.LENGTH_SHORT).show();
//            }
//        }
        when {
            listSize > 0 -> {
                undoEntities.push(entities.removeAt(lastItemPosition))
                selectEntity(null, false)
                updateUI()
            }
            else -> {
                Toast.makeText(this.context, "Nothing to Undo", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    private var isReseted = false
    fun reset() {
//        resetEntities.addAll(entities)
        entities.clear()
        selectEntity(null, false)
        updateUI()
        motionViewCallback!!.onEntityUnselected()
//        isReseted = true
    }

    // gesture detectors
    private val onTouchListener = OnTouchListener { _, event ->
        if (scaleGestureDetector != null) {
            scaleGestureDetector!!.onTouchEvent(event)
            rotateGestureDetector!!.onTouchEvent(event)
            gestureDetectorCompat!!.onTouchEvent(event)
            moveGestureDetector!!.onTouchEvent(event)
        }

        true
    }

    private inner class TapsListener : SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (motionViewCallback != null && selectedEntity != null) {
                motionViewCallback!!.onEntityDoubleTap(selectedEntity)
            }
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            updateOnLongPress(e)
        }


        override fun onSingleTapUp(e: MotionEvent): Boolean {
            closeSelectionOnTap(e)
            updateSelectionOnTap(e)
            return true
        }

    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (selectedEntity != null) {
                val scaleFactorDiff = detector.scaleFactor
                selectedEntity!!.layer.postScale(scaleFactorDiff - 1.0f)
                updateUI()
            }
            return true
        }
    }

    private inner class RotateListener : RotateGestureDetector.SimpleOnRotateGestureListener() {
        override fun onRotate(detector: RotateGestureDetector?): Boolean {
            if (selectedEntity != null) {
                selectedEntity!!.layer.postRotate(-detector!!.rotationDegreesDelta)
                updateUI()
            }
            return true
        }
    }

    private inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            handleTranslate(detector.getFocusDelta())
            return true
        }
    }

    companion object {
        private val TAG = MotionView::class.java.simpleName
    }
}
