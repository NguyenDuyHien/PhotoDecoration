package com.hmman.photodecoration.adapter

import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.hmman.photodecoration.R
import com.hmman.photodecoration.util.StringUtils
import kotlinx.android.synthetic.main.item_tool.view.*
import java.util.*

class ToolAdapter(
    private val mOnItemSelected: OnItemSelected
) : RecyclerView.Adapter<ToolAdapter.ViewHolder>() {

    private val mToolList: MutableList<ToolModel> = ArrayList()
    var isEnable = true

    internal inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        var mToolType: ToolType
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tool, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        var color: Int = Color.GRAY
        holder.txtToolName.text = StringUtils.capitalize(item.mToolName)
        holder.imgIcon.setImageResource(item.mToolIcon)
        when (isEnable) {
            true -> color = Color.BLACK
        }
        holder.txtToolName.setTextColor(color)
        holder.imgIcon.setColorFilter(color)
    }

    override fun getItemCount(): Int {
        return mToolList.size
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgIcon: AppCompatImageView = itemView.imgIcon
        var txtToolName: AppCompatTextView = itemView.txtToolName

        init {
            if (isEnable) {
                itemView.setOnClickListener {
                    mOnItemSelected.onToolSelected(mToolList[adapterPosition].mToolType)
                }
            }
        }
    }

    init {
        mToolList.add(ToolModel(ToolType.TEXT.name, R.drawable.text, ToolType.TEXT))
        mToolList.add(ToolModel(ToolType.STICKER.name, R.drawable.sticker, ToolType.STICKER))
    }

    enum class ToolType {
        TEXT, STICKER
    }

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType)
    }
}
