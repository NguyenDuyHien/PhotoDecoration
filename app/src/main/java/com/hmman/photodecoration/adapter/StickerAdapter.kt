package com.hmman.photodecoration.adapter

import android.content.Context
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.hmman.photodecoration.R
import kotlinx.android.synthetic.main.item_sticker.view.*


class StickerAdapter(context: Context, val mOnStickerSelected: OnStickerSelected) :
    RecyclerView.Adapter<StickerAdapter.ItemHolder>() {

    private var stickerList: MutableList<Int> = mutableListOf()
    private val context: Context = context

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sticker: AppCompatImageView = itemView.sticker

        init {
            itemView.setOnClickListener {
                mOnStickerSelected.onStickerSelected(stickerList[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_sticker, parent, false)
        return ItemHolder(view)
    }

    override fun getItemCount(): Int {
        return stickerList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val sticker = stickerList[position]
        holder.sticker.setImageResource(sticker)
    }

    init {
        initStickerList()
    }

    interface OnStickerSelected {
        fun onStickerSelected(sticker: Int)
    }

    private fun initStickerList() {
        val list: TypedArray = context.resources.obtainTypedArray(R.array.list)
        for (i in 0 until list.length()) {
            stickerList.add(list.getResourceId(i, -1))
        }
    }
}