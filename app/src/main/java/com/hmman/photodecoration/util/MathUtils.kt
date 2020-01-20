package com.hmman.photodecoration.util

import android.graphics.PointF
import androidx.annotation.NonNull

object MathUtils {

    fun pointInTriangle(
        @NonNull pt: PointF, @NonNull v1: PointF,
        @NonNull v2: PointF, @NonNull v3: PointF
    ): Boolean {
        val b1 = crossProduct(pt, v1, v2) < 0.0f
        val b2 = crossProduct(pt, v2, v3) < 0.0f
        val b3 = crossProduct(pt, v3, v1) < 0.0f
        return b1 == b2 && b2 == b3
    }

    private fun crossProduct(
        @NonNull a: PointF, @NonNull b: PointF,
        @NonNull c: PointF
    ): Float {
        return (a.x - c.x) * (b.y - c.y) - (b.x - c.x) * (a.y - c.y)
    }
    
}