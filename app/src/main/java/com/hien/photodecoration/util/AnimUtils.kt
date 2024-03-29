package com.hien.photodecoration.util

import android.content.Context
import android.view.animation.AnimationUtils
import com.hien.photodecoration.R

class AnimUtils {
    companion object {
        fun slideRightLeftDialog () = R.style.DialogRightLeft
        fun slideUp (context: Context) = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        fun slideDown (context: Context) = AnimationUtils.loadAnimation(context, R.anim.slide_down)
    }
}