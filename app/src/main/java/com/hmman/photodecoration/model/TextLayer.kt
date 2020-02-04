package com.hmman.photodecoration.model

class TextLayer : Layer() {
    var text: String? = null
    var font: Font? = null
    private var initialScale = 0f

    override fun reset() {
        super.reset()
        text = ""
        font = Font()
    }

    override fun getMaxScale(): Float {
        return Limits.MAX_SCALE
    }

    override fun getMinScale(): Float {
        return Limits.MIN_SCALE
    }

    override fun initialScale(): Float {
        return initialScale
    }

    fun setInitialScale(initialScale: Float) {
        this.initialScale = initialScale
    }

    interface Limits {
        companion object {
            const val MAX_SCALE = 3.0f
            const val MIN_SCALE = 0.2f
            const val MIN_BITMAP_HEIGHT = 0.13f
            const val FONT_SIZE_STEP = 0.008f
            const val INITIAL_FONT_SIZE = 0.075f
            const val INITIAL_FONT_COLOR = 0x1000000
            const val INITIAL_SCALE = 0.8f // set the same to avoid text scaling
        }
    }
}