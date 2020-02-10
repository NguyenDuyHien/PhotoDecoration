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
import kotlin.math.round


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
    var isUp = true
    var isFirstTime = true
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
        if (event.action == MotionEvent.ACTION_DOWN) {
            for (i in mColorRects.indices) {
                val rect = mColorRects[i]
                if (rect != null) {
                    if (isTouchInRange(rect, event.x.toInt(), event.y.toInt())) {
                        updateView(event.x, event.y)
                        return true
                    }
                }
            }
            return false
        } else if (event.action == MotionEvent.ACTION_MOVE) {
            updateView(event.x, event.y)
            return true
        } else if (event.action == MotionEvent.ACTION_UP) {
            isUp = true
            updateView(event.x, event.y)
            return true
        }
        return true
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

        if  (selectedItem != mColorFullRects.size - 1) {
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

    private fun isInRange(@NonNull rect: Rect, x: Int, y: Int): Boolean {
        return if (isLockMode) {
            rect.contains(x, y)
        } else {
            rect.left <= x && rect.right >= x
        }
    }

    private fun isTouchInRange(@NonNull rect: Rect, x: Int, y: Int): Boolean {
        return rect.contains(x, y)
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
        val colors = resources.getIntArray(R.array.gradient_colors)

        mPaint?.let {mPaint ->
            for (i in mColorRects.indices) {
                if (i == selectedItem && !isFirstTime) {
                    if (mSelectorPaint != null && i == 0) {
                        mPaint.color = mColors[i]
                        canvas.drawArc(RectF(mColorRects[i]!!),  90F, 180F, true,  mPaint)
                        canvas.drawRect((mColorRects[i]!!.left +(mColorRects[i]!!.right - mColorRects[i]!!.left)/2).toFloat(),
                            mColorRects[i]!!.top.toFloat(),
                            mColorRects[i]!!.right.toFloat(),
                            mColorRects[i]!!.bottom.toFloat(), mPaint)

                        if (!isUp) {
                            canvas.drawCircle(
                                (this.mColorFullRects[i]!!.left + (this.mColorFullRects[i]!!.right - this.mColorFullRects[i]!!.left) / 2).toFloat(),
                                radius,
                                radius * 0.9f,
                                mPaint
                            )
                        } else {
                            canvas.drawCircle((this.mColorFullRects[i]!!.left +(this.mColorFullRects[i]!!.right - this.mColorFullRects[i]!!.left) / 2).toFloat(),
                                radius + (radius *0.8f),
                                radius *0.1f,
                                mPaint)
                            isUp = false
                        }
                    } else if (mSelectorPaint != null && i == mColorRects.size - 1){
                        mPaint.shader = drawRectWithGradient(mColorFullRects[i]!!.width(), mColorFullRects[i]!!.height(), colors)
                        canvas.drawArc(RectF(mColorRects[i]!!),  270F, 180F, true,  mPaint)
                        canvas.drawRect( mColorRects[i]!!.left.toFloat(),
                            mColorRects[i]!!.top.toFloat(),
                            (mColorRects[i]!!.left +(mColorRects[i]!!.right - mColorRects[i]!!.left)/2).toFloat(),
                            mColorRects[i]!!.bottom.toFloat(), mPaint)

                        if (!isUp) {
                            canvas.drawCircle(
                                (this.mColorFullRects[i]!!.left + (this.mColorFullRects[i]!!.right - this.mColorFullRects[i]!!.left) / 2).toFloat(),
                                radius,
                                radius * 0.9f,
                                mPaint)
                        } else {
                            canvas.drawCircle((this.mColorFullRects[i]!!.left +(this.mColorFullRects[i]!!.right - this.mColorFullRects[i]!!.left) / 2).toFloat(),
                                radius + (radius *0.8f),
                                radius *0.1f,
                                mPaint)
                            isUp = false
                        }
                        mPaint.shader = null
                    } else {
                        mPaint.color = mColors[i]
                        canvas.drawRect(mColorRects[i]!!, mPaint)

                        if (!isUp) {
                            canvas.drawCircle(
                                (this.mColorFullRects[i]!!.left + (this.mColorFullRects[i]!!.right - this.mColorFullRects[i]!!.left) / 2).toFloat(),
                                radius,
                                radius * 0.9f,
                                mPaint)
                        } else {
                            canvas.drawCircle((this.mColorFullRects[i]!!.left +(this.mColorFullRects[i]!!.right - this.mColorFullRects[i]!!.left) / 2).toFloat(),
                                radius + (radius *0.8f),
                                radius *0.1f,
                                mPaint)
                            isUp = false
                        }
                    }
                } else {
                    when (i) {
                        0 -> {
                            mPaint.color = mColors[i]
                            canvas.drawArc(RectF(mColorRects[i]!!),  90F, 180F, true,  mPaint)
                            canvas.drawRect((mColorRects[i]!!.left +(mColorRects[i]!!.right - mColorRects[i]!!.left)/2).toFloat(),
                                mColorRects[i]!!.top.toFloat(),
                                mColorRects[i]!!.right.toFloat(),
                                mColorRects[i]!!.bottom.toFloat(), mPaint)
                        }
                        this.mColorRects.size - 1 -> {
                            mPaint.shader = drawRectWithGradient(mColorRects[i]!!.width(), mColorRects[i]!!.height(), colors)
                            canvas.drawArc(RectF(mColorRects[i]!!),  270F, 180F, true,  mPaint)
                            canvas.drawRect( mColorRects[i]!!.left.toFloat(),
                                mColorRects[i]!!.top.toFloat(),
                                (mColorRects[i]!!.left +(mColorRects[i]!!.right - mColorRects[i]!!.left)/2).toFloat(),
                                mColorRects[i]!!.bottom.toFloat(), mPaint)
                            mPaint.shader = null
                        }
                        else -> {
                            mPaint.color = mColors[i]
                            canvas.drawRect(mColorRects[i]!!, mPaint)
                        }
                    }
                }
            }
        }
        isFirstTime = false
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private fun drawRectWithGradient(
        width: Int,
        height: Int,
        colors: IntArray
    ): Shader? {
        val gradientDrawable = GradientDrawable()
        val shader: Shader
        gradientDrawable.colors = colors
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
        val itemWidth = width / (mColors.size.toFloat()+4)
        mColorRects = arrayOfNulls(mColors.size)
        mColorFullRects = arrayOfNulls(mColors.size)
        val margin =  itemWidth *4f
//        val margin = height * 0.70f
//        radius = height * 0.70f / 2
        radius = itemWidth *2f
        var length:Int = round(height* 0.70f / 2 ).toInt()
        for (i in mColors.indices) {
            mColorRects[i] = Rect(
                (itemWidth * i).toInt() +(itemWidth *2).toInt(),
                margin.toInt(),
                (itemWidth * (i + 1)).toInt()+(itemWidth *2).toInt(),
                (height - (margin *0.15) ).toInt()
            )
            mColorFullRects[i] = Rect(
                (itemWidth * i).toInt() +(itemWidth *2).toInt(),
                0,
                (itemWidth * (i + 1)).toInt()+(itemWidth *2).toInt(),
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