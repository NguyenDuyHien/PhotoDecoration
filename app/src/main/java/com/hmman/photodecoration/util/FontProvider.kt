package com.hmman.photodecoration.util

import android.content.res.Resources
import android.graphics.Typeface
import android.text.TextUtils
import androidx.annotation.Nullable

class FontProvider(private val resources: Resources) {
    private val typefaces: MutableMap<String?, Typeface>
    private val fontNameToTypefaceFile: MutableMap<String, String>
    private val fontNames: List<String>

    fun getTypeface(@Nullable typefaceName: String?): Typeface? {
        return if (TextUtils.isEmpty(typefaceName)) {
            Typeface.DEFAULT
        } else {
            if (typefaces[typefaceName] == null) {
                typefaces[typefaceName] = Typeface.createFromAsset(
                    resources.assets,
                    "fonts/" + fontNameToTypefaceFile[typefaceName]
                )
            }
            typefaces[typefaceName]
        }
    }

    fun getFontNames(): List<String> {
        return fontNames
    }

    fun getDefaultFontName(): String {
        return DEFAULT_FONT_NAME
    }

    companion object {
        private const val DEFAULT_FONT_NAME = "Helvetica"
    }

    init {
        typefaces = HashMap()
        fontNameToTypefaceFile = HashMap()
        fontNameToTypefaceFile["Arial"] = "Arial.ttf"
        fontNameToTypefaceFile["Eutemia"] = "Eutemia.ttf"
        fontNameToTypefaceFile["GREENPIL"] = "GREENPIL.ttf"
        fontNameToTypefaceFile["Grinched"] = "Grinched.ttf"
        fontNameToTypefaceFile["Helvetica"] = "Helvetica.ttf"
        fontNameToTypefaceFile["Libertango"] = "Libertango.ttf"
        fontNameToTypefaceFile["Metal Macabre"] = "MetalMacabre.ttf"
        fontNameToTypefaceFile["Parry Hotter"] = "ParryHotter.ttf"
        fontNameToTypefaceFile["SCRIPTIN"] = "SCRIPTIN.ttf"
        fontNameToTypefaceFile["The Godfather v2"] = "TheGodfather_v2.ttf"
        fontNameToTypefaceFile["Aka Dora"] = "akaDora.ttf"
        fontNameToTypefaceFile["Waltograph"] = "waltograph42.ttf"
        fontNames = ArrayList(fontNameToTypefaceFile.keys)
    }
}