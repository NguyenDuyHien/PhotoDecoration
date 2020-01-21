package com.hmman.photodecoration.util

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.hmman.photodecoration.R

class AnimUtil {
    companion object {
        fun slideRightLeftDialog () = R.style.DialogRightLeft
        fun slideUp (context: Context) = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        fun slideDown (context: Context) = AnimationUtils.loadAnimation(context, R.anim.slide_down)
    }
}