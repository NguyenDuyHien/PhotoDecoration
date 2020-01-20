package com.hmman.photodecoration.model

import android.graphics.Color

data class Font(
    var color: Int? = null,
    var typeface: String? = null,
    var size: Float = 0f
) {

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
}