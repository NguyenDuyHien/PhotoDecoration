package com.hmman.photodecoration.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hmman.photodecoration.R
import com.hmman.photodecoration.adapter.StickerAdapter
import com.hmman.photodecoration.util.AnimUtil
import kotlinx.android.synthetic.main.dialog_sticker.*

class DialogSticker (
    context: Context,
    val mOnStickerSelected: StickerAdapter.onStickerSelected
) : Dialog (context){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_sticker)

//        setCancelable(false)

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            attributes.windowAnimations = AnimUtil.slideRightLeftDialog()
            setGravity(Gravity.BOTTOM)
        }

        btnBack.setOnClickListener({
            this.hide()
        })

        showStickerList()
    }

    private fun showStickerList() {
        val adapter = StickerAdapter(context, mOnStickerSelected)
        val layoutManager = GridLayoutManager(context, 2, LinearLayoutManager.HORIZONTAL,false)
        rvStickers.layoutManager = layoutManager
        rvStickers.adapter = adapter
    }
}