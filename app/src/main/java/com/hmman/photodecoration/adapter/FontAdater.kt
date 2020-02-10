package com.hmman.photodecoration.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.hmman.photodecoration.util.FontProvider


class FontAdater (
    context: Context,
    resource: Int,
    val fontList: List<String>
) : ArrayAdapter<String?>(context, resource, fontList) {

    private var fontProvider: FontProvider
    private var selectedItem = -1

    init {
        fontProvider = FontProvider(context.resources)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.setTypeface(fontProvider.getTypeface(fontList.get(position)))

        if (position == selectedItem){
            view.setTextColor(Color.BLUE)
            view.setPaintFlags(view.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
        }
        else {
            view.setTextColor(Color.BLACK)
            view.setPaintFlags(0)
        }

        return view
    }

    fun setSelection (position: Int){
        selectedItem = position
        notifyDataSetChanged()
    }
}
