package com.hmman.photodecoration.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hmman.photodecoration.R
import kotlinx.android.synthetic.main.item_sticker.view.*

class StickerAdapter (context: Context, val mOnStickerSelected: onStickerSelected) : RecyclerView.Adapter<StickerAdapter.ItemHolder> () {

    private lateinit var stickerList: MutableList<Int>

    init {
        initStickerList()
    }

    interface onStickerSelected {
        fun onStickerSelected(sticker: Int)
    }

    private fun initStickerList(){
        stickerList = mutableListOf()

        stickerList.add(R.drawable.abra)
        stickerList.add(R.drawable.bellsprout)
        stickerList.add(R.drawable.bracelet)
        stickerList.add(R.drawable.bullbasaur)
        stickerList.add(R.drawable.candy)
        stickerList.add(R.drawable.caterpie)
        stickerList.add(R.drawable.charmander)
        stickerList.add(R.drawable.mankey)
        stickerList.add(R.drawable.map)
        stickerList.add(R.drawable.mega_ball)
        stickerList.add(R.drawable.meowth)
        stickerList.add(R.drawable.pawprints)
        stickerList.add(R.drawable.pidgey)
        stickerList.add(R.drawable.pikachu)
        stickerList.add(R.drawable.pikachu_1)
        stickerList.add(R.drawable.pikachu_2)
        stickerList.add(R.drawable.player)
        stickerList.add(R.drawable.pointer)
        stickerList.add(R.drawable.pokebag)
        stickerList.add(R.drawable.pokeball)
        stickerList.add(R.drawable.pokeballs)
        stickerList.add(R.drawable.pokecoin)
        stickerList.add(R.drawable.pokedex)
        stickerList.add(R.drawable.potion)
        stickerList.add(R.drawable.psyduck)
        stickerList.add(R.drawable.rattata)
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sticker = itemView.sticker
        init {
            itemView.setOnClickListener({
                mOnStickerSelected.onStickerSelected(stickerList[adapterPosition])
            })
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
        val sticker = stickerList.get(position)
        holder.sticker.setImageResource(sticker)
    }

}