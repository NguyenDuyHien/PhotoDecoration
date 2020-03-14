package com.hien.photodecoration.util

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
        return fontNames.sorted()
    }

    fun getDefaultFontName(): String {
        return DEFAULT_FONT_NAME
    }

    companion object {
        const val DEFAULT_FONT_NAME = "Alata - Regular"
    }

    fun clone(): FontProvider {
        return FontProvider(resources)
    }

    init {
        typefaces = HashMap()
        fontNameToTypefaceFile = HashMap()
        fontNameToTypefaceFile["Arial"] = "Arial.ttf"
        fontNameToTypefaceFile["Alata - Regular"] = "Alata-Regular.ttf"

        fontNameToTypefaceFile["Baloo Bhai - Regular"] = "BalooBhai-Regular.ttf"
        fontNameToTypefaceFile["Comfortaa VariableFont"] = "Comfortaa-VariableFont.ttf"
        fontNameToTypefaceFile["Didact Gothic - Regular"] = "DidactGothic-Regular.ttf"
        fontNameToTypefaceFile["Dosis VariableFont"] = "Dosis-VariableFont.ttf"
        fontNameToTypefaceFile["Fredoka One - Regular"] = "FredokaOne-Regular.ttf"
        fontNameToTypefaceFile["Gochihand - Regular"] = "GochiHand-Regular.ttf"

        fontNameToTypefaceFile["JosefinSans - BoldItalic"] = "JosefinSans-BoldItalic.ttf"
        fontNameToTypefaceFile["JosefinSans - Italic"] = "JosefinSans-Italic.ttf"
        fontNameToTypefaceFile["JosefinSans - Light"] = "JosefinSans-Light.ttf"

        fontNameToTypefaceFile["KulimPark - BoldItalic"] = "KulimPark-BoldItalic.ttf"
        fontNameToTypefaceFile["KulimPark - ExtraLight"] = "KulimPark-ExtraLight.ttf"
        fontNameToTypefaceFile["KulimPark - Regualar"] = "KulimPark-Regular.ttf"

        fontNameToTypefaceFile["Lato - Regular"] = "Lato-Regular.ttf"
        fontNameToTypefaceFile["Lato - Thin"] = "Lato-Thin.ttf"
        fontNameToTypefaceFile["Lato - LightItalic"] = "Lato-LightItalic.ttf"
        fontNameToTypefaceFile["Lato - Bold"] = "Lato-Bold.ttf"

        fontNameToTypefaceFile["Lobster - Regular"] = "Lobster-Regular.ttf"
        fontNameToTypefaceFile["LobsterTwo - Italic"] = "LobsterTwo-Italic.ttf"
        fontNameToTypefaceFile["LobsterTwo - Bold"] = "LobsterTwo-Bold.ttf"

        fontNameToTypefaceFile["Mitr - Regular"] = "Mitr-Regular.ttf"
        fontNameToTypefaceFile["Mitr - Light"] = "Mitr-Light.ttf"
        fontNameToTypefaceFile["Mitr - Bold"] = "Mitr-Bold.ttf"

        fontNameToTypefaceFile["Montserrat - Regular"] = "Montserrat-Regular.ttf"
        fontNameToTypefaceFile["Montserrat - Italic"] = "Montserrat-Italic.ttf"
        fontNameToTypefaceFile["Montserrat - Light"] = "Montserrat-Light.ttf"
        fontNameToTypefaceFile["Montserrat - Bold"] = "Montserrat-Bold.ttf"

        fontNameToTypefaceFile["PatrickHand - Regular"] = "PatrickHand-Regular.ttf"
        fontNameToTypefaceFile["PaytoneOne - Regular"] = "PaytoneOne-Regular.ttf"
//        fontNameToTypefaceFile["Yellowtail - Regular"] = "Yellowtail-Regular.ttf"

        fontNameToTypefaceFile["Helvetica"] = "Helvetica.ttf"
        fontNames = ArrayList(fontNameToTypefaceFile.keys)
    }
}