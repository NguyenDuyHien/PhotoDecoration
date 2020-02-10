package com.hmman.photodecoration.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hmman.photodecoration.R
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

        when (position){
            selectedItem -> {
//                view.setTextColor(Color.BLUE)
//                view.setPaintFlags(view.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)
                view.setBackgroundColor(ContextCompat.getColor(context, R.color.selectedFont))
                view.setTextColor(Color.WHITE)
            }
            else -> {
//                view.setTextColor(Color.BLACK)
//                view.setPaintFlags(0)
                view.setBackgroundColor(Color.WHITE)
                view.setTextColor(Color.BLACK)
            }
        }

        return view
    }

    fun setSelection (position: Int){
        selectedItem = position
        notifyDataSetChanged()
    }
}
