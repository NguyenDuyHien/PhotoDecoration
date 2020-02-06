package com.hmman.photodecoration.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.hmman.photodecoration.R
import kotlinx.android.synthetic.main.colorsliderbar.*

class DialogColor(
    context: Context,
    val mOnColorSelected: EditDialogFragment
) : Dialog(context) {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.colorsliderbar)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
        }
        color_slider.setSelectorColor(Color.TRANSPARENT)
        color_slider.setListener(mListener)
    }
    private val mListener: ColorSlider.OnColorSelectedListener =
        object : ColorSlider.OnColorSelectedListener {
            override fun onColorChanged(position: Int, color: Int) {
                mOnColorSelected.onColorSelected(color)

            }
        }
    interface onColorSelected {
        fun onColorSelected(color: Int){
        }
    }

}
