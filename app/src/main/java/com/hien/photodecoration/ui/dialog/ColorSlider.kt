package com.hien.photodecoration.ui.dialog

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.NonNull

class ColorSlider @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mColors = intArrayOf()
    private var mColorBlockRects = arrayOf<RectF>()
    private var mSelectorRects = arrayOf<RectF>()
    private lateinit var mPaint: Paint
    private var selectedItem = 0
    private var mListener: OnColorSelectedListener? = null
    private var isLockMode = false
    // isUp = true -> draw small circle, isUp = false -> draw big circle
    private var isUp = true
    private var bigSelectorRadius = 0f
    private var smallSelectorRadius = 0f

    fun setListener(listener: OnColorSelectedListener?) {
        mListener = listener
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?
    ) {
        mPaint = Paint()
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.isAntiAlias = true
        setOnTouchListener { _, event -> processTouch(event) }
        if (attrs != null) {
            val a =
                context.theme.obtainStyledAttributes(attrs, com.hien.photodecoration.R.styleable.ColorSlider, 0, 0)
            try {
                val id = a.getResourceId(com.hien.photodecoration.R.styleable.ColorSlider_cs_colors, 0)
                if (id != 0) {
                    val ids = resources.getIntArray(id)
                    if (ids.isNotEmpty()) {
                        mColors = IntArray(ids.size)
                        System.arraycopy(ids, 0, mColors, 0, ids.size)
                    }
                }
            } catch (e: Exception) {
                Log.d("ColorSlider", "init: " + e.localizedMessage)
            } finally {
                a.recycle()
            }
        }
        if (mColors.isEmpty()) {
            initDefaultColors()
        }
        mColorBlockRects = Array(mColors.size) { RectF() }
        mSelectorRects = Array(mColors.size) { RectF() }
    }

    private fun initDefaultColors() {
        mColors = intArrayOf(
            Color.parseColor("#FFFFFF"),
            Color.parseColor("#000000"),
            Color.parseColor("#666666"),
            Color.parseColor("#808080"),
            Color.parseColor("#CCCCCC"),
            Color.parseColor("#D33115"),
            Color.parseColor("#F44E3B"),
            Color.parseColor("#F46F3B"),
            Color.parseColor("#FE9200"),
            Color.parseColor("#FCDC00"),
            Color.parseColor("#A4DD00"),
            Color.parseColor("#BAD279"),
            Color.parseColor("#16A55F"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#73D8FF"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#1976D2"),
            Color.parseColor("#8079D2"),
            Color.parseColor("#8A40BF"),
            Color.parseColor("#EB144C"),
            0
        )
    }

    fun setLastSelectedColor(@ColorInt color: Int) {
        for (i in mColors.indices) {
            if (color == mColors[i] || i == mColors.size - 1) {
                selectedItem = i
                this.invalidate()
                break
            }
        }
    }

    private fun processTouch(event: MotionEvent): Boolean {
        when {
            event.action == MotionEvent.ACTION_DOWN -> {
                for (i in mColorBlockRects.indices) {
                    val rect = mColorBlockRects[i]
                    if (isTouchInRange(rect, event.x, event.y)) {
                        isUp = false
                        updateView(event.x, event.y)
                        return true
                    }
                }
                return false
            }
            event.action == MotionEvent.ACTION_MOVE -> {
                isUp = false
                updateView(event.x, event.y)
                return true
            }
            event.action == MotionEvent.ACTION_UP -> {
                isUp = true
                updateView(event.x, event.y)
                return true
            }
            else -> return true
        }
    }

    private fun updateView(x: Float, y: Float) {
        var changed = false
        for (i in mSelectorRects.indices) {
            val rect = mSelectorRects[i]
            if (isInRange(rect, x, y) && i != selectedItem) {
                selectedItem = i
                changed = true
                break
            }
        }

        if (selectedItem != mSelectorRects.size - 1) {
            if (isUp) {
                invalidate()
            } else if (!isUp && changed) {
                invalidate()
                notifyChanged()
            }
        } else {
            if (isUp) {
                invalidate()
                notifyChanged()
            } else {
                invalidate()
            }
        }
    }

    private fun isInRange(@NonNull rect: RectF, x: Float, y: Float): Boolean {
        return if (isLockMode) {
            rect.contains(x, y)
        } else {
            rect.left <= x && rect.right >= x
        }
    }

    private fun isTouchInRange(@NonNull rect: RectF, x: Float, y: Float): Boolean {
        return rect.contains(x, y)
    }

    private fun notifyChanged() {
        mListener?.onColorChanged(selectedItem, mColors[selectedItem])
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mColorBlockRects.isNotEmpty()) {
            drawSlider(canvas)
        }
    }

    private fun drawSlider(canvas: Canvas) {
        val colors = resources.getIntArray(com.hien.photodecoration.R.array.gradient_colors)
        val firstColorBlockPath = Path()
        val lastColorBlockPath = Path()
        val colorBlockHeight = mColorBlockRects[0].height()
        val firstColorBlockCorners = floatArrayOf(
            colorBlockHeight / 2, colorBlockHeight / 2, // Top left radius in px
            0f, 0f, // Top right radius in px
            0f, 0f, // Bottom right radius in px
            colorBlockHeight / 2, colorBlockHeight / 2           // Bottom left radius in px
        )
        val lastColorBlockCorners = floatArrayOf(
            0f, 0f,
            colorBlockHeight / 2, colorBlockHeight / 2,
            colorBlockHeight / 2, colorBlockHeight / 2,
            0f, 0f
        )

        for (i in mColorBlockRects.indices) {
            if (i == selectedItem) {
                if (i == 0) {
                    mPaint.color = mColors[i]
                    firstColorBlockPath.addRoundRect(mColorBlockRects[i], firstColorBlockCorners, Path.Direction.CW)
                    canvas.drawPath(firstColorBlockPath, mPaint)

                    if (!isUp) {
                        canvas.drawCircle(
                            (mSelectorRects[i].left + (mSelectorRects[i].right - mSelectorRects[i].left) / 2),
                            bigSelectorRadius,
                            bigSelectorRadius,
                            mPaint
                        )
                    } else {
                        canvas.drawCircle(
                            (mSelectorRects[i].left + (mSelectorRects[i].right - mSelectorRects[i].left) / 2),
                            mSelectorRects[i].bottom - smallSelectorRadius,
                            smallSelectorRadius,
                            mPaint
                        )
                    }
                } else if (i == (mColorBlockRects.size - 1)) {
                    mPaint.shader = getVerticalLinearGradient(mColorBlockRects[i], colors)
                    lastColorBlockPath.addRoundRect(mColorBlockRects[i], lastColorBlockCorners, Path.Direction.CW)
                    canvas.drawPath(lastColorBlockPath, mPaint)
                    mPaint.shader = null

                    if (!isUp) {
                        val centerX = mSelectorRects[i].left + (mSelectorRects[i].right - mSelectorRects[i].left) / 2
                        val centerY = bigSelectorRadius
                        mPaint.shader = getHorizontalLinearGradient(
                            RectF(
                                centerX - bigSelectorRadius,
                                centerY - bigSelectorRadius,
                                centerX + bigSelectorRadius,
                                centerY + bigSelectorRadius
                            ),
                            colors
                        )
                        canvas.drawCircle(centerX, centerY, bigSelectorRadius, mPaint)
                        mPaint.shader = null
                    } else {
                        val centerX = mSelectorRects[i].left + (mSelectorRects[i].right - mSelectorRects[i].left) / 2
                        val centerY = mSelectorRects[i].bottom - smallSelectorRadius
                        mPaint.shader = getHorizontalLinearGradient(
                            RectF(
                                centerX - smallSelectorRadius,
                                centerY - smallSelectorRadius,
                                centerX + smallSelectorRadius,
                                centerY + smallSelectorRadius
                            ),
                            colors
                        )
                        canvas.drawCircle(
                            (mSelectorRects[i].left + (mSelectorRects[i].right - mSelectorRects[i].left) / 2),
                            mSelectorRects[i].bottom - smallSelectorRadius,
                            smallSelectorRadius,
                            mPaint
                        )
                        mPaint.shader = null
                    }

                } else {
                    mPaint.color = mColors[i]
                    canvas.drawRect(mColorBlockRects[i], mPaint)

                    if (!isUp) {
                        canvas.drawCircle(
                            (mSelectorRects[i].left + (mSelectorRects[i].right - mSelectorRects[i].left) / 2),
                            bigSelectorRadius,
                            bigSelectorRadius,
                            mPaint
                        )
                    } else {
                        canvas.drawCircle(
                            (mSelectorRects[i].left + (mSelectorRects[i].right - mSelectorRects[i].left) / 2),
                            mSelectorRects[i].bottom - smallSelectorRadius,
                            smallSelectorRadius,
                            mPaint
                        )
                    }
                }
            } else {
                when (i) {
                    0 -> {
                        mPaint.color = mColors[i]
                        firstColorBlockPath.addRoundRect(mColorBlockRects[i], firstColorBlockCorners, Path.Direction.CW)
                        canvas.drawPath(firstColorBlockPath, mPaint)
                    }
                    (mColorBlockRects.size - 1) -> {
                        mPaint.shader = getVerticalLinearGradient(mColorBlockRects[i], colors)
                        lastColorBlockPath.addRoundRect(mColorBlockRects[i], lastColorBlockCorners, Path.Direction.CW)
                        canvas.drawPath(lastColorBlockPath, mPaint)
                        mPaint.shader = null
                    }
                    else -> {
                        mPaint.color = mColors[i]
                        canvas.drawRect(mColorBlockRects[i], mPaint)
                    }
                }
            }

        }
    }

    private fun getHorizontalLinearGradient(rectF: RectF, colors: IntArray): Shader {
        return LinearGradient(
            rectF.left,
            rectF.top + (rectF.height() / 2),
            rectF.right,
            rectF.bottom - (rectF.height() / 2),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
    }

    private fun getVerticalLinearGradient(rectF: RectF, colors: IntArray): Shader {
        return LinearGradient(
            rectF.left + (rectF.width() / 2),
            rectF.top,
            rectF.right - (rectF.width() / 2),
            rectF.bottom,
            colors,
            null,
            Shader.TileMode.CLAMP
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        calculateRectangles()
    }

    private fun calculateRectangles() {
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val colorBlockWidth = width / (mColors.size + PADDING_LEFT + PADDING_RIGHT)
        val colorBlockHeight = height * COLOR_BLOCK_HEIGHT_RATIO
        val paddingBottom = height * PADDING_BOTTOM_RATIO
        bigSelectorRadius = height * BIG_SELECTOR_RADIUS_RATIO
        smallSelectorRadius = bigSelectorRadius * 0.1f
        for (i in mColors.indices) {
            mColorBlockRects[i] = RectF(
                (colorBlockWidth * i) + (colorBlockWidth * PADDING_LEFT),
                (height - (colorBlockHeight + paddingBottom)),
                (colorBlockWidth * (i + 1)) + (colorBlockWidth * PADDING_LEFT),
                (height - paddingBottom)
            )
            mSelectorRects[i] = RectF(
                (colorBlockWidth * i) + (colorBlockWidth * PADDING_LEFT),
                0f,
                (colorBlockWidth * (i + 1)) + (colorBlockWidth * PADDING_LEFT),
                bigSelectorRadius * 2
            )
        }
    }

    interface OnColorSelectedListener {
        fun onColorChanged(position: Int, @ColorInt color: Int)
    }

    init {
        init(context, attrs)
    }

    companion object {
        private const val PADDING_BOTTOM_RATIO = 0.07f // 7% of ColorSliderView height
        private const val PADDING_LEFT =
            2 // Padding between left of ColorSliderView and left of first color block = 2x color block width
        private const val PADDING_RIGHT =
            2 // Padding between right of ColorSliderView and right of last color block = 2x color block width
        private const val COLOR_BLOCK_HEIGHT_RATIO = 0.2f // 20% of ColorSliderView height
        private const val BIG_SELECTOR_RADIUS_RATIO =
            0.35f // 35% of ColorSliderView height => big selector height = 70% of ColorSliderView height
    }
}

/* ColorSliderView height = 100%
   Big selector radius = 35%
   Margin between selector and color block = 3%
   Color block height = 20%
   Margin bottom = 7%*/