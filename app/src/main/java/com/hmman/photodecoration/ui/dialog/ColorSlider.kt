package com.hmman.photodecoration.ui.dialog

import com.hmman.photodecoration.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat

class ColorSlider @JvmOverloads constructor(
    context: Context, @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mColors = intArrayOf()
    private var mColorRects =
        arrayOf<Rect?>()
    private var mColorFullRects =
        arrayOf<Rect?>()
    @Nullable
    private var mPaint: Paint? = null
    @Nullable
    private var mSelectorPaint: Paint? = null
    var selectedItem = 0
        private set
    @Nullable
    private var mListener: OnColorSelectedListener? =
        null

    private var barCornerRadius: Float = 8f
    var isLockMode = false

    fun setSelectorColor(@ColorInt color: Int) {
        if (mSelectorPaint != null) {
            mSelectorPaint!!.color = color
            this.invalidate()
        }
    }

    fun setListener(listener: OnColorSelectedListener?) {
        mListener = listener
    }

    private fun init(
        context: Context,
        attrs: AttributeSet?
    ) {


        mPaint = Paint()
        mPaint!!.style = Paint.Style.FILL_AND_STROKE
        mSelectorPaint = Paint()
        mSelectorPaint!!.style = Paint.Style.FILL
        mSelectorPaint!!.color = ContextCompat.getColor(
            context,
            R.color.editDialogBackground
        )
        mSelectorPaint!!.strokeWidth = 2f
        setOnTouchListener { v, event -> processTouch(event) }
        var selectorColor = 0
        if (attrs != null) {
            val a =
                context.theme.obtainStyledAttributes(attrs, R.styleable.ColorSlider, 0, 0)
            try {
                selectorColor = a.getColor(R.styleable.ColorSlider_cs_selector_color, 0)
                val id = a.getResourceId(R.styleable.ColorSlider_cs_colors, 0)
                val hexId = a.getResourceId(R.styleable.ColorSlider_cs_hex_colors, 0)

                barCornerRadius = a.getDimension(R.styleable.ColorSlider_cornerRadius, 30f)

                if (id != 0) {
                    val ids = resources.getIntArray(id)
                    if (ids.size > 0) {
                        mColors = IntArray(ids.size)
                        System.arraycopy(ids, 0, mColors, 0, ids.size)
                    }
                } else if (hexId != 0) {
                    val hex = resources.getStringArray(hexId)
                    if (hex.size > 0) {
                        convertToColors(hex)
                    }
                }
                if (mColors.size == 0) {
                    val fromColor = a.getColor(R.styleable.ColorSlider_cs_from_color, 0)
                    val toColor = a.getColor(R.styleable.ColorSlider_cs_to_color, 0)
                    val steps = a.getInt(R.styleable.ColorSlider_cs_steps, 21)
                    if (fromColor != 0 && toColor != 0 && steps != 0) {
                        calculateColors(fromColor, toColor, steps)
                    }
                }
            } catch (e: Exception) {
                Log.d("ColorSlider", "init: " + e.localizedMessage)
            } finally {
                a.recycle()
            }
        }
        if (mColors.size == 0) {
            initDefaultColors()
        }
        mColorRects = arrayOfNulls(mColors.size)
        mColorFullRects = arrayOfNulls(mColors.size)
        if (selectorColor != 0 && mSelectorPaint != null) {
            mSelectorPaint!!.color = selectorColor
        }
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

    private fun convertToColors(hex: Array<String>) {
        mColors = IntArray(hex.size)
        for (i in hex.indices) {
            mColors[i] = Color.parseColor(hex[i])
        }
    }

   /* private fun calculateColors(@ColorInt colors: IntArray, steps: Int) {
        val numOfColors = colors.size
        val stepsPerBlock = steps / (numOfColors - 1)
        var leftSteps = 0
        if (steps % stepsPerBlock != 0) {
            leftSteps = steps % stepsPerBlock
        }
        mColors = IntArray(steps)
        for (i in 1 until numOfColors) {
            val fromColor = colors[i - 1]
            val toColor = colors[i]
            val startBlockPosition = (i - 1) * stepsPerBlock
            var endBlockPosition = i * stepsPerBlock
            if (i == numOfColors - 1) endBlockPosition += leftSteps
            val blockSteps = endBlockPosition - startBlockPosition
            val a1 = Color.alpha(fromColor).toFloat()
            val r1 = Color.red(fromColor).toFloat()
            val g1 = Color.green(fromColor).toFloat()
            val b1 = Color.blue(fromColor).toFloat()
            val a2 = Color.alpha(toColor).toFloat()
            val r2 = Color.red(toColor).toFloat()
            val g2 = Color.green(toColor).toFloat()
            val b2 = Color.blue(toColor).toFloat()
            val alphaStep = (a2 - a1) / blockSteps.toFloat()
            val redStep = (r2 - r1) / blockSteps.toFloat()
            val greenStep = (g2 - g1) / blockSteps.toFloat()
            val blueStep = (b2 - b1) / blockSteps.toFloat()
            var k = 0
            for (j in startBlockPosition until endBlockPosition) {
                mColors[j] = Color.argb(
                    (a1 + alphaStep * k).toInt(), (r1 + redStep * k).toInt(),
                    (g1 + greenStep * k).toInt(), (b1 + blueStep * k).toInt()
                )
                k++
            }
        }
    }*/

    private fun calculateColors(@ColorInt fromColor: Int, @ColorInt toColor: Int, steps: Int) {
        val a1 = Color.alpha(fromColor).toFloat()
        val r1 = Color.red(fromColor).toFloat()
        val g1 = Color.green(fromColor).toFloat()
        val b1 = Color.blue(fromColor).toFloat()
        val a2 = Color.alpha(toColor).toFloat()
        val r2 = Color.red(toColor).toFloat()
        val g2 = Color.green(toColor).toFloat()
        val b2 = Color.blue(toColor).toFloat()
        val alphaStep = (a2 - a1) / steps.toFloat()
        val redStep = (r2 - r1) / steps.toFloat()
        val greenStep = (g2 - g1) / steps.toFloat()
        val blueStep = (b2 - b1) / steps.toFloat()
        mColors = IntArray(steps)
        for (i in 0 until steps) {
            mColors[i] = Color.argb(
                (a1 + alphaStep * i).toInt(), (r1 + redStep * i).toInt(),
                (g1 + greenStep * i).toInt(), (b1 + blueStep * i).toInt()
            )
        }
    }

    private fun processTouch(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) return true else if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_UP) {
            updateView(event.x, event.y)
            return true
        }
        return false
    }

    private fun updateView(x: Float, y: Float) {
        var changed = false
        for (i in mColorFullRects.indices) {
            val rect = mColorFullRects[i]
            if (rect != null) {
                if (isInRange(rect, x.toInt(), y.toInt()) && i != selectedItem) {
                    selectedItem = i
                    changed = true
                    break
                }
            }
        }
        if (changed) {
            invalidate()
            notifyChanged()
        }
    }

    private fun isInRange(@NonNull rect: Rect, x: Int, y: Int): Boolean {
        return if (isLockMode) {
            rect.contains(x, y)
        } else {
            rect.left <= x && rect.right >= x
        }
    }

    private fun notifyChanged() {
        if (mListener != null) {
            mListener!!.onColorChanged(selectedItem, mColors[selectedItem])
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mColorRects.size > 0) {
            drawSlider(canvas)
        }
    }

    private fun drawSlider(canvas: Canvas) {
        if (mPaint != null) {
            for (i in mColorRects.indices) {
                mPaint!!.color = mColors[i]
                if (i == selectedItem) {
                    canvas.drawRect(mColorFullRects[i]!!, mPaint!!)
                    if (mSelectorPaint != null) {
                        canvas.drawRect(mColorFullRects[i]!!, mSelectorPaint!!)
                    }
                } else {
                    canvas.drawRect(mColorRects[i]!!, mPaint!!)
                }
            }
        }
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
        val itemWidth = width / mColors.size.toFloat()
        mColorRects = arrayOfNulls(mColors.size)
        mColorFullRects = arrayOfNulls(mColors.size)
        val margin = height * 0.1f
        for (i in mColors.indices) {
            mColorRects[i] = Rect(
                (itemWidth * i).toInt(),
                margin.toInt(),
                (itemWidth * (i + 1)).toInt(),
                (height - margin).toInt()
            )
            mColorFullRects[i] = Rect(
                (itemWidth * i).toInt(),
                0,
                (itemWidth * (i + 1)).toInt(),
                height.toInt()
            )
        }
    }

    interface OnColorSelectedListener {
        fun onColorChanged(position: Int, @ColorInt color: Int)
    }

    init {
        init(context, attrs)
    }
}