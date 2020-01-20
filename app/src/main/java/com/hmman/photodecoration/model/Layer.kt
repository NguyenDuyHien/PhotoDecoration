package com.hmman.photodecoration.model

import androidx.annotation.FloatRange

open class Layer {
    @FloatRange(from = 0.0, to = 360.0)
    var rotationInDegrees = 0f
    var scale = 0f
    var x = 0f
    var y = 0f
    var isFlipped = false

    fun Layer() {
        reset()
    }

    open fun reset() {
        rotationInDegrees = 0.0f
        scale = 1.0f
        isFlipped = false
        x = 0.0f
        y = 0.0f
    }

    fun postScale(scaleDiff: Float) {
        val newVal = scale + scaleDiff
        if (newVal >= getMinScale() && newVal <= getMaxScale()) {
            scale = newVal
        }
    }

    open fun getMaxScale(): Float {
        return Layer.Limits.MAX_SCALE
    }

    open fun getMinScale(): Float {
        return Layer.Limits.MIN_SCALE
    }

    fun postRotate(rotationInDegreesDiff: Float) {
        rotationInDegrees += rotationInDegreesDiff
        rotationInDegrees %= 360.0f
    }

    fun postTranslate(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun flip() {
        isFlipped = !isFlipped
    }

    open fun initialScale(): Float {
        return Layer.Limits.INITIAL_ENTITY_SCALE
    }

    internal interface Limits {
        companion object {
            const val MIN_SCALE = 0.06f
            const val MAX_SCALE = 4.0f
            const val INITIAL_ENTITY_SCALE = 0.4f
        }
    }
}