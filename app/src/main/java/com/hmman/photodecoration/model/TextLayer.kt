package com.hmman.photodecoration.model

class TextLayer(
    var text: String? = null,
    var font: Font? = null
) : Layer() {
    private var initialScale = 0f
    var dynamicMinScale = Limits.MIN_SCALE
    var dynamicMaxScale = Limits.MAX_SCALE
    override fun reset() {
        super.reset()
        text = ""
        font = Font()
    }

    override fun getMaxScale(): Float {
        return dynamicMinScale
    }

    override fun getMinScale(): Float {
        return dynamicMaxScale
    }

    override fun postScale(scaleDiff: Float) {
        val newVal = scale + scaleDiff
        if (newVal in dynamicMinScale..Limits.MAX_SCALE) {
            scale = newVal
        }
    }

    override fun initialScale(): Float {
        return initialScale
//        return Limits.MIN_SCALE
    }

    fun setInitialScale(initialScale: Float) {
        this.initialScale = initialScale
    }

    interface Limits {
        companion object {
            const val MAX_SCALE = 2f
            const val MIN_SCALE = 0.2f
            const val MIN_BITMAP_HEIGHT = 0.13f
            const val FONT_SIZE_STEP = 0.008f
            const val INITIAL_FONT_SIZE = 0.07f
            const val INITIAL_FONT_COLOR = 0x1000000
            const val INITIAL_SCALE = 0.8f // set the same to avoid text scaling
        }
    }

    fun cloneTextLayer(): TextLayer {
        return TextLayer(text, font!!.clone())
    }
}