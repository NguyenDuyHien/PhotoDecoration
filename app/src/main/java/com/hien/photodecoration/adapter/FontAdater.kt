package com.hien.photodecoration.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.hien.photodecoration.R
import com.hien.photodecoration.util.FontProvider


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

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.setTypeface(fontProvider.getTypeface(fontList.get(position)))

        when (position){
            selectedItem -> {
                view.apply {
                    setBackgroundColor(ContextCompat.getColor(context, R.color.selectedFont))
                    setTextColor(Color.WHITE)
                }
            }
            else -> {
                view.apply {
                    setBackgroundColor(Color.WHITE)
                    setTextColor(Color.BLACK)
                }
            }
        }

        return view
    }

    fun setSelection (position: Int){
        selectedItem = position
        notifyDataSetChanged()
    }
}
