package com.hien.photodecoration.model

data class Font(
    var color: Int? = null,
    var typeface: String? = null,
    var size: Float = 0f
) {

    fun getColor(): Int {
        return color!!
    }

    fun getTextSize(): Float {
        return size
    }

    fun increaseSize(diff: Float) {
        size += diff
    }

    fun decreaseSize(diff: Float) {
        if (size - diff >= Limits.MIN_FONT_SIZE) {
            size -= diff
        }
    }

    private interface Limits {
        companion object {
            const val MIN_FONT_SIZE = 0.01f
        }
    }

    open fun clone(): Font {
        return Font(color, typeface, size)
    }
}