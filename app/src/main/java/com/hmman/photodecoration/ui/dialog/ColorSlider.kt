package com.hmman.photodecoration.ui.dialog

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.hmman.photodecoration.R


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
    private var selectedItem = 0
    @Nullable
    private var mListener: OnColorSelectedListener? =
        null
    var isLockMode = false
    private var radius: Float = 0.toFloat()
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
        setOnTouchListener { _, event -> processTouch(event) }
        var selectorColor = 0
        if (attrs != null) {
            val a =
                context.theme.obtainStyledAttributes(attrs, R.styleable.ColorSlider, 0, 0)
            try {
                selectorColor = a.getColor(R.styleable.ColorSlider_cs_selector_color, 0)
                val id = a.getResourceId(R.styleable.ColorSlider_cs_colors, 0)
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
        if (mColorRects.isNotEmpty()) {
            drawSlider(canvas)
        }
    }

    private fun drawSlider(canvas: Canvas) {
        mPaint?.let {mPaint ->
            for (i in mColorRects.indices) {
//                mColorRects[i]?.let { canvas.drawRect(it, this.mPaint!!) }
                if (i == selectedItem && i != 0 && i != this.mColorRects.size - 1) {
                    mPaint.color = mColors[i]
                    canvas.drawRect(mColorRects[i]!!, mPaint)
                    if (mSelectorPaint != null) {
//                        canvas.drawRect(mColorFullRects[i]!!, mSelectorPaint!!)
                        canvas.drawCircle(
                            ((this.mColorFullRects[i]!!.left + this.mColorFullRects[i + 1]!!.left) / 2).toFloat(),
                            radius,
                            radius *0.9f,
                            this.mPaint!!
                        )
                    }
                } else {
                    when (i) {
                        0 -> {
                            mPaint.color = mColors[i]
                            canvas.drawArc(RectF(mColorRects[i]!!),  90F, 180F, true,  mPaint)
                            canvas.drawRect((mColorRects[i]!!.left +(mColorRects[i]!!.right - mColorRects[i]!!.left)/2).toFloat(),
                                mColorRects[i]!!.top.toFloat(),
                                mColorRects[i]!!.right.toFloat(),
                                mColorRects[i]!!.bottom.toFloat(), mPaint
                            )
                        }
                        this.mColorRects.size - 1 -> {
                            // Draw gradient and half circle
                            val colors = IntArray(mColors.size - 1)
                            System.arraycopy(mColors, 0, colors, 0, mColors.size - 1)
                            mPaint.shader = drawRectWithGradient(mColorRects[i]!!.width(), mColorRects[i]!!.height(), colors)
                            canvas.drawArc(RectF(mColorRects[i]!!),  270F, 180F, true,  mPaint)
                            canvas.drawRect( mColorRects[i]!!.left.toFloat(),
                                mColorRects[i]!!.top.toFloat(),
                                (mColorRects[i]!!.left +(mColorRects[i]!!.right - mColorRects[i]!!.left)/2).toFloat(),
                                mColorRects[i]!!.bottom.toFloat(), mPaint)
                            mPaint!!.shader = null
                        }
                        else -> {
                            mPaint.color = mColors[i]
                            canvas.drawRect(mColorRects[i]!!, mPaint)
                        }
                    }

                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private fun drawRectWithGradient(
        width: Int,
        height: Int,
        colors: IntArray
    ): Shader? {
        val gradientDrawable = GradientDrawable()
        val shader: Shader
            gradientDrawable.setColors(colors)
            gradientDrawable.cornerRadii = floatArrayOf(
                0f,
                0f,
                0f,
                0f,
                0f,
                0f,
                0f,
                0f
            )
            val mutableBitmap =
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val _canvas = Canvas(mutableBitmap)
            gradientDrawable.setBounds(0, 0, width, height)
            gradientDrawable.draw(_canvas)
            shader = BitmapShader(mutableBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            return shader
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
        val margin = height * 0.70f
        radius = height * 0.70f / 2
        for (i in mColors.indices) {
            mColorRects[i] = Rect(
                (itemWidth * i).toInt(),
                margin.toInt(),
                (itemWidth * (i + 1)).toInt(),
                (height - (margin *0.2) ).toInt()
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