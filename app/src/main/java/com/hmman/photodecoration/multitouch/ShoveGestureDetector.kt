package com.hmman.photodecoration.multitouch

import android.content.Context
import android.view.MotionEvent

class ShoveGestureDetector(
    context: Context?,
    private val mListener: OnShoveGestureListener
) : TwoFingerGestureDetector(context!!) {

    private var mPrevAverageY = 0f
    private var mCurrAverageY = 0f
    private var mSloppyGesture = false

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_DOWN -> {
                resetState()
                mPrevEvent = MotionEvent.obtain(event)
                mTimeDelta = 0
                updateStateByEvent(event!!)
                mSloppyGesture = isSloppyGesture(event)
                if (!mSloppyGesture) {
                    mGestureInProgress = mListener.onShoveBegin(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mSloppyGesture) {
                    return
                }
                mSloppyGesture = isSloppyGesture(event!!)
                if (!mSloppyGesture) {
                    mGestureInProgress = mListener.onShoveBegin(this)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> if (!mSloppyGesture) {
                return
            }
        }
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent?) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_UP -> {
                updateStateByEvent(event!!)
                if (!mSloppyGesture) {
                    mListener.onShoveEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (!mSloppyGesture) {
                    mListener.onShoveEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event!!)
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD
                    && Math.abs(shovePixelsDelta) > 0.5f
                ) {
                    val updatePrevious = mListener.onShove(this)
                    if (updatePrevious) {
                        mPrevEvent!!.recycle()
                        mPrevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }
    }

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)
        val prev = mPrevEvent!!
        val py0 = prev.getY(0)
        val py1 = prev.getY(1)
        mPrevAverageY = (py0 + py1) / 2.0f
        val cy0 = curr.getY(0)
        val cy1 = curr.getY(1)
        mCurrAverageY = (cy0 + cy1) / 2.0f
    }

    override fun isSloppyGesture(event: MotionEvent): Boolean {
        val sloppy = super.isSloppyGesture(event)
        if (sloppy) return true
        val angle =
            Math.abs(Math.atan2(mCurrFingerDiffY.toDouble(), mCurrFingerDiffX.toDouble()))
        return !(0.0f < angle && angle < 0.35f
                || 2.79f < angle && angle < Math.PI)
    }

    val shovePixelsDelta: Float
        get() = mCurrAverageY - mPrevAverageY

    override fun resetState() {
        super.resetState()
        mSloppyGesture = false
        mPrevAverageY = 0.0f
        mCurrAverageY = 0.0f
    }

    interface OnShoveGestureListener {
        fun onShove(detector: ShoveGestureDetector?): Boolean
        fun onShoveBegin(detector: ShoveGestureDetector?): Boolean
        fun onShoveEnd(detector: ShoveGestureDetector?)
    }

    class SimpleOnShoveGestureListener : OnShoveGestureListener {
        override fun onShove(detector: ShoveGestureDetector?): Boolean {
            return false
        }

        override fun onShoveBegin(detector: ShoveGestureDetector?): Boolean {
            return true
        }

        override fun onShoveEnd(detector: ShoveGestureDetector?) { // Do nothing, overridden implementation may be used
        }
    }

}
