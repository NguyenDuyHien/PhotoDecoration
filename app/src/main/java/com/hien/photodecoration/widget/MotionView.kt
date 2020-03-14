package com.hien.photodecoration.widget

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.hien.photodecoration.R
import com.hien.photodecoration.model.TextLayer
import com.hien.photodecoration.multitouch.MoveGestureDetector
import com.hien.photodecoration.multitouch.RotateGestureDetector
import com.hien.photodecoration.util.BorderUtil
import com.hien.photodecoration.util.FontProvider
import com.hien.photodecoration.util.PhotoUtils
import com.hien.photodecoration.widget.entity.IconEntity
import com.hien.photodecoration.widget.entity.MotionEntity
import com.hien.photodecoration.widget.entity.TextEntity
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

    private val entities: MutableList<MotionEntity> = ArrayList()
    private val undoEntities: Stack<MotionEntity> = Stack()
    val moveUndoEntities: Stack<MotionEntity> = Stack()
    private val moveRedoEntities: Stack<MotionEntity> = Stack()
    val undoActionEntities: Stack<String> = Stack()
    val redoActionEntities = Stack<String>()
    private val indexUndoRemoveEntities = Stack<Int>()
    private val indexRedoRemoveEntities = Stack<Int>()
    private val removeEntities: Stack<MotionEntity> = Stack()
    private var checkListernerPerAction: Boolean = false
    @Nullable
    var selectedEntity: MotionEntity? = null
        private set
    private var selectedLayerPaint: Paint? = null

    // Icon
    private val icons: MutableList<IconEntity> = ArrayList()
    // callback
    @Nullable
    private var motionViewCallback: MotionViewCallback? = null
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var rotateGestureDetector: RotateGestureDetector? = null
    private var moveGestureDetector: MoveGestureDetector? = null
    private var gestureDetectorCompat: GestureDetectorCompat? = null

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
        configDefaultIcons()
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

    private fun configDefaultIcons() {
        val deleteIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_delete)
        val deleteIconEntity = IconEntity(deleteIcon, IconEntity.RIGHT_TOP)
        icons.clear()
        icons.add(deleteIconEntity)
    }

    private fun selectIconEntity(iconEntity: IconEntity?) {
        when (iconEntity?.gravity) {
            IconEntity.RIGHT_TOP -> {
                deletedSelectedEntity()
            }
//            IconEntity.RIGHT_BOTTOM->{
//                setOnTouchListener(onTouchListener)
//            }
        }
    }

    fun getEntities(): List<MotionEntity> {
        return entities
    }

    fun setMotionViewCallback(@Nullable callback: MotionViewCallback?) {
        motionViewCallback = callback
    }

    fun addEntityAndPosition(@Nullable entity: MotionEntity?) {
        if (entity != null) {
            initEntityBorderAndIconBackground(entity)
            initialTranslateAndScale(entity)
            entities.add(entity)
            undoActionEntities.push("ADD")
            redoActionEntities.clear()
            selectEntity(entity, true)
        }
    }

    private fun initEntityClose(@NonNull entity: MotionEntity) { // init stroke
        val strokeSize = resources.getDimensionPixelSize(R.dimen.stroke_size)
        val borderPaint = Paint()
        borderPaint.strokeWidth = strokeSize.toFloat()
        borderPaint.isAntiAlias = true
        borderPaint.color = ContextCompat.getColor(context, R.color.white)
        entity.setClosePaint(borderPaint)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (selectedEntity != null) {
            selectedEntity!!.draw(canvas, selectedLayerPaint, icons)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawAllEntities(canvas)
        super.onDraw(canvas)
    }

    private fun drawAllEntities(canvas: Canvas) {
        for (i in entities.indices) {
            entities[i].draw(canvas, null, icons)
        }
    }

    private fun drawAllRealEntities(canvas: Canvas) {
        for (i in entities.indices) {
            entities[i].drawReal(canvas, null)
        }
    }

    fun getFinalBitmap(): Bitmap? {
        selectEntity(null, false)

        try {
            val inputStream =
                context.contentResolver.openInputStream(PhotoUtils.getInstance(null).photoUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            Log.d("long", "${PhotoUtils.getInstance(null).photoUri}")
            val preventRotateBitmap = PhotoUtils.getInstance(null)
                .rotateImageIfRequired(bitmap, PhotoUtils.getInstance(null).photoUri)

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

    fun unSelectEntity() {
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
        if (selectedEntity != null && selectedEntity!!.pointClose(p)) {
//            deletedSelectedEntity()
            return true
        }
        return false

    }

    private fun updateSelectionOnTap(e: MotionEvent) {
        val iconEntity: IconEntity? = findIconAtPoint(e.x, e.y)
        selectIconEntity(iconEntity)
        val entity: MotionEntity? = findEntityAtPoint(e.x, e.y)
        when (entity) {
            null -> unSelectEntity()
            else -> {
                selectEntity(entity, true)
                bringLayerToFront(entity)
            }
        }
    }

    private fun findIconAtPoint(x: Float, y: Float): IconEntity? {
        var selected: IconEntity? = null
        if (selectedEntity != null) {
            for (i in icons.indices.reversed()) {
                if (selectedEntity!!.pointInLayerRectIcon(PointF(x, y), icons[i])) {
                    selected = icons[i]
                    break
                }
            }
        }
        return selected
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

    private fun moveEntityToBack(@Nullable entity: MotionEntity?) {
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
        /**
         * Find position of selected Entity
         * */
        val pos = entities.indexOf(selectedEntity!!)

        if (entities.remove(selectedEntity!!)) {
            removeEntities.push(selectedEntity)
            undoActionEntities.push("REMOVE")
//            selectedEntity = null
            indexUndoRemoveEntities.push(pos)
            unSelectEntity()
            invalidate()
        }
    }

    fun release() {
        for (entity in entities) {
            entity.release()
        }
    }

    fun redo() {
        val listSize = redoActionEntities.size
        when {
            listSize > 0 -> {
                when {
                    redoActionEntities[redoActionEntities.size - 1] == "ADD" -> {
                        entities.add(undoEntities.pop())
                    }
                    redoActionEntities[redoActionEntities.size - 1] == "MOVE" -> {
                        var indexOfEntity = -1
                        entities.forEachIndexed { index, motionEntity ->
                            if (motionEntity.name == moveRedoEntities[moveRedoEntities.size - 1].name) {
                                indexOfEntity = index
                            }
                        }
                        if (indexOfEntity != -1) {
                            moveUndoEntities.push(entities[indexOfEntity])
                            entities.removeAt(indexOfEntity)
                            entities.add(moveRedoEntities.pop())
                            unSelectEntity()
                        }
                    }
                    else -> {
                        if (indexRedoRemoveEntities.size > 0 && entities.size > 0) {
                            undoEntities.push(entities.removeAt(indexRedoRemoveEntities[indexRedoRemoveEntities.size - 1]))
                            unSelectEntity()
                            indexUndoRemoveEntities.push(indexRedoRemoveEntities.pop())
                        }
                    }
                }
                undoActionEntities.push(redoActionEntities.pop())
                updateUI()
            }
            else -> {
                Toast.makeText(this.context, "Nothing to Redo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun undo() {
        val lastItemPosition = entities.size - 1
        val listSize = undoActionEntities.size
        when {
            listSize > 0 -> {
                when {
                    undoActionEntities[undoActionEntities.size - 1] == "ADD" && lastItemPosition != -1 -> {
                        undoEntities.push(entities.removeAt(lastItemPosition))
                        unSelectEntity()
                    }
                    undoActionEntities[undoActionEntities.size - 1] == "MOVE" -> {
                        undoActionEntities.forEach { println(it) }
                        var lastIndexOf = -1
                        entities.forEachIndexed { index, motionEntity ->
                            if (motionEntity.name == moveUndoEntities[moveUndoEntities.size - 1].name) {
                                lastIndexOf = index
                            }
                        }

                        if (lastIndexOf != -1) {
                            if (moveUndoEntities[moveUndoEntities.size - 1].layer is TextLayer) {
                                val textLayer =
                                    moveUndoEntities[moveUndoEntities.size - 1].layer as TextLayer
                            }

                            moveRedoEntities.push(entities[lastIndexOf])
                            entities.removeAt(lastIndexOf)
                            entities.add(moveUndoEntities.pop())
                            unSelectEntity()
                        }
                    }
                    else -> {
                        if (removeEntities.size > 0) {
                            val entity = removeEntities.pop()

                            entities.add(
                                indexUndoRemoveEntities[indexUndoRemoveEntities.size - 1],
                                entity
                            )
                            indexRedoRemoveEntities.push(indexUndoRemoveEntities.pop())
                            selectEntity(entity, true)
                        }
                    }
                }
                redoActionEntities.push(undoActionEntities.pop())
                updateUI()
            }
            else -> {
                Toast.makeText(this.context, "Nothing to Undo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun reset() {
        entities.clear()
        selectEntity(null, false)
        undoEntities.clear()
        redoActionEntities.clear()
        undoActionEntities.clear()
        updateUI()
        motionViewCallback!!.onEntityUnselected()
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
//            closeSelectionOnTap(e)
            updateSelectionOnTap(e)
            return true
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun redrawTextEntityOnScaleEnd() {
        val fontProvider = FontProvider(resources)
        val textLayer = TextLayer()
        val font = (selectedEntity as TextEntity).getLayer().font
        font!!.size =
            TextLayer.Limits.INITIAL_FONT_SIZE * selectedEntity!!.layer.scale / TextLayer.Limits.MIN_SCALE
        val currentText = (selectedEntity as TextEntity).getLayer().text
        textLayer.font = font
        textLayer.text = currentText
        textLayer.scale = selectedEntity!!.layer.scale
        textLayer.rotationInDegrees = selectedEntity!!.layer.rotationInDegrees
        textLayer.x = selectedEntity!!.layer.x
        textLayer.y = selectedEntity!!.layer.y
        textLayer.isFlipped = selectedEntity!!.layer.isFlipped
        val textEntity =
            TextEntity(
                textLayer,
                this.width,
                this.height,
                fontProvider,
                currentText!!,
                this.context
            )
        initEntityBorderAndIconBackground(textEntity)
        entities.remove(selectedEntity!!)
        entities.add(textEntity)
        selectEntity(textEntity, true)

        updateUI()
    }

    private fun initEntityBorderAndIconBackground(entity: MotionEntity) {
        BorderUtil.initEntityBorder(entity, context)
        BorderUtil.initEntityIconBackground(entity, context)
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        var entity: MotionEntity? = null
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (selectedEntity != null) {
                val scaleFactorDiff = detector.scaleFactor
                selectedEntity!!.layer.postScale(scaleFactorDiff - 1.0f)
                updateUI()
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            if (entities.indexOf(selectedEntity) != -1) {
                entity = entities[entities.indexOf(selectedEntity!!)].clone()
            }
            return true
        }


        @RequiresApi(Build.VERSION_CODES.M)
        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)
            if (entity != null) {
                var checkDuplicate = false
                moveUndoEntities.forEach {
                    if (checkDuplication(it, entity!!)) checkDuplicate = true
                }
                if (!checkDuplicate) {
                    moveUndoEntities.add(entity)
                    undoActionEntities.push("MOVE")
                    redoActionEntities.clear()
                    entity = null
                }
            }
//            if (selectedEntity is TextEntity) {
//                redrawTextEntityOnScaleEnd()
//            }
        }
    }

    private inner class RotateListener : RotateGestureDetector.SimpleOnRotateGestureListener() {
        var entity: MotionEntity? = null
        override fun onRotate(detector: RotateGestureDetector?): Boolean {
            checkListernerPerAction = true
            if (selectedEntity != null) {
                selectedEntity!!.layer.postRotate(-detector!!.rotationDegreesDelta)
                updateUI()
            }
            return true
        }

        override fun onRotateBegin(detector: RotateGestureDetector?): Boolean {
            if (entities.indexOf(selectedEntity) != -1) {
                entity = selectedEntity!!.clone()
            }
            return true
        }

        override fun onRotateEnd(detector: RotateGestureDetector?) {
            if (entity != null) {
                var checkDuplicate = false
                moveUndoEntities.forEach {
                    if (checkDuplication(it, entity!!)) checkDuplicate = true
                }
                if (!checkDuplicate) {
                    moveUndoEntities.add(entity)
                    undoActionEntities.push("MOVE")
                    redoActionEntities.clear()
                    entity = null
                }
            }
        }
    }

    private inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        var entity: MotionEntity? = null
        override fun onMove(detector: MoveGestureDetector): Boolean {
            if (selectedEntity != null) {
                handleTranslate(detector.getFocusDelta())
            }
            return true
        }

        override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
            if (entities.indexOf(selectedEntity) != -1) {
                entity = entities[entities.indexOf(selectedEntity)].clone()
            }
            return true
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            if (entity != null) {
                var checkDuplicate = false
                moveUndoEntities.forEach {
                    if (checkDuplication(it, entity!!)) checkDuplicate = true
                }
                if (!checkDuplicate) {
                    moveUndoEntities.add(entity)
                    undoActionEntities.push("MOVE")
                    redoActionEntities.clear()
                    entity = null
                }

            }
        }
    }

    private fun checkDuplication(entity: MotionEntity, preparedEntity: MotionEntity): Boolean {
        if (entity.layer.x == preparedEntity.layer.x && entity.layer.y == preparedEntity.layer.y
            && entity.layer.scale == preparedEntity.layer.scale
            && entity.layer.rotationInDegrees == preparedEntity.layer.rotationInDegrees
        ) {
            if (entity.layer is TextLayer && preparedEntity.layer is TextLayer) {
                val preparedLayer = preparedEntity.layer as TextLayer
                val textLayer = entity.layer as TextLayer
                if (textLayer.font!!.color != preparedLayer.font!!.color
                    || textLayer.font!!.typeface != preparedLayer.font!!.typeface
                    || textLayer.text != preparedLayer.text
                ) {
                    return false
                }
            }
            return true
        }

        return false
    }

    companion object {
        private val TAG = MotionView::class.java.simpleName
    }
}
