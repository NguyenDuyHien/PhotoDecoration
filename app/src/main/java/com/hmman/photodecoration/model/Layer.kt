package com.hmman.photodecoration.model

open class Layer(
    var rotationInDegrees: Float = 0f,
    var scale: Float = 0f,
    var x: Float = 0f,
    var y: Float = 0f,
    var isFlipped: Boolean = false
) {

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
        return Limits.MAX_SCALE
    }

    open fun getMinScale(): Float {
        return Limits.MIN_SCALE
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
        return Limits.INITIAL_ENTITY_SCALE
    }

    internal interface Limits {
        companion object {
            const val MIN_SCALE = 0.1f
            const val MAX_SCALE = 1.5f
            const val INITIAL_ENTITY_SCALE = 0.4f
        }
    }

    open fun clone(): Layer {
        return Layer(rotationInDegrees, scale, x, y, isFlipped)
    }
}