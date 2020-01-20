package com.hmman.photodecoration.multitouch

import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent

class MoveGestureDetector(
    context: Context,
    listener: OnMoveGestureListener
) : BaseGestureDetector(context) {

    private val mListener: OnMoveGestureListener
    private var mCurrFocusInternal: PointF? = null
    private var mPrevFocusInternal: PointF? = null
    private val mFocusExternal = PointF()
    private var mFocusDeltaExternal = PointF()

    init {
        mListener = listener
    }

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_DOWN -> {
                resetState()

                mPrevEvent = MotionEvent.obtain(event)
                mTimeDelta = 0

                updateStateByEvent(event!!)
            }
            MotionEvent.ACTION_MOVE -> mGestureInProgress = mListener.onMoveBegin(this)
        }
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_UP -> {
                mListener.onMoveEnd(this)
                resetState()
            }
            MotionEvent.ACTION_CANCEL -> {
                mListener.onMoveEnd(this)
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event!!)
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                    val updatePrevious = mListener.onMove(this)
                    if (updatePrevious) {
                        mPrevEvent!!.recycle()
                        mPrevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }
    }

    private fun determineFocalPoint(event: MotionEvent): PointF {
        val pCount = event.pointerCount
        var x = 0f
        var y = 0f

        for (i in 0 until pCount) {
            x += event.getX(i)
            y += event.getY(i)
        }

        return PointF(x / pCount, y / pCount)
    }

    fun getFocusX(): Float {
        return mFocusExternal.x
    }

    fun getFocusY(): Float {
        return mFocusExternal.y
    }

    fun getFocusDelta(): PointF {
        return mFocusDeltaExternal
    }

    interface OnMoveGestureListener {
        fun onMove(detector: MoveGestureDetector): Boolean
        fun onMoveBegin(detector: MoveGestureDetector): Boolean
        fun onMoveEnd(detector: MoveGestureDetector)
    }

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)

        val prev = mPrevEvent

        mCurrFocusInternal = determineFocalPoint(curr)
        mPrevFocusInternal = determineFocalPoint(prev!!)

        val mSkipNextMoveEvent = prev.pointerCount != curr.pointerCount
        mFocusDeltaExternal =
            if (mSkipNextMoveEvent)
                FOCUS_DELTA_ZERO
            else PointF(
                mCurrFocusInternal!!.x - mPrevFocusInternal!!.x,
                mCurrFocusInternal!!.y - mPrevFocusInternal!!.y
            )
    }

    companion object {
        val FOCUS_DELTA_ZERO = PointF()
        val TAG = this::class.java.simpleName
    }

    open class SimpleOnMoveGestureListener : OnMoveGestureListener {
        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
            return true
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {

        }

    }
}