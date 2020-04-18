package com.hien.photodecoration.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.hien.photodecoration.R
import kotlinx.android.synthetic.main.test.*

class TestActivity : AppCompatActivity() {

    var isDecor = true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)

        cropOverlay.setOnTouchListener { v, event ->
            false
        }

        motionViewTest.setOnTouchListener { v, event ->
            if (isDecor)
            motionViewTest.setBackgroundColor(Color.argb(60f, 0f, 0f, 0f))
            isDecor
        }

//        crop_image.setOnTouchListener { v, event ->
////            crop_image.setBackgroundColor(Color.WHITE)
//            false
//        }
    }
}