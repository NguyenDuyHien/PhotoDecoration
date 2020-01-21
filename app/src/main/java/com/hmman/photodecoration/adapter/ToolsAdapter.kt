package com.hmman.photodecoration.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hmman.photodecoration.R
import kotlinx.android.synthetic.main.item_tool.view.*
import java.util.*

class ToolsAdapter(
    private val mOnItemSelected: OnItemSelected
) : RecyclerView.Adapter<ToolsAdapter.ViewHolder>() {

    private val mToolList: MutableList<ToolModel> = ArrayList()
    var isEnable = true

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType)
    }

    internal inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        var mtoolType: ToolType
    )

    enum class ToolType {
        TEXT, STICKER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToolsAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tool, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList.get(position)
        holder.txtToolName.text = item.mToolName
        holder.imgIcon.setImageResource(item.mToolIcon)

        when (isEnable){
            true -> {
                holder.txtToolName.setTextColor(Color.BLACK)
                holder.imgIcon.setColorFilter(Color.BLACK)
            }
            else -> {
                holder.txtToolName.setTextColor(Color.GRAY)
                holder.imgIcon.setColorFilter(Color.GRAY)
            }
        }
    }

    override fun getItemCount(): Int {
        return mToolList.size
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgIcon = itemView.imgIcon
        var txtToolName = itemView.txtToolName

        init {
            if (isEnable){
                itemView.setOnClickListener {
                    mOnItemSelected.onToolSelected(mToolList.get(adapterPosition).mtoolType)
                }
            }
        }
    }

    init {
        mToolList.add(ToolModel("Text", R.drawable.text, ToolType.TEXT))
        mToolList.add(ToolModel("Sticker", R.drawable.sticker, ToolType.STICKER))
    }
}
