package com.hmman.photodecoration.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.hmman.photodecoration.R
import com.hmman.photodecoration.adapter.FontAdater
import com.hmman.photodecoration.util.Constants
import com.hmman.photodecoration.util.FontProvider
import kotlinx.android.synthetic.main.edit_dialog.*
import java.util.regex.Pattern

class EditDialogFragment : DialogFragment(), DialogColor.onColorSelected  {

    private lateinit var colorDialog: DialogColor
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.edit_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.apply {
                setLayout(width, height)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    private var mTextEditor: TextEditor? = null
    private var mContent: String? = null
    private var mFontName: String? = null
    private var mInputMethodManager: InputMethodManager? = null
    var mColorCode: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mInputMethodManager =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        edtContent.requestFocus()

        val fontProvider = FontProvider(context!!.resources)

        mInputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        mColorCode = arguments!!.getInt(Constants.COLOR_CODE)
        mContent = arguments!!.getString(Constants.TEXT_CONTENT)
        mFontName = arguments!!.getString(Constants.FONT_NAME)
        edtContent.setTextColor(mColorCode!!)
        edtContent.setText(mContent)
        edtContent.typeface = fontProvider.getTypeface(mFontName)
        edtContent.setSelection(edtContent.text!!.length)
//        btnColor.setBackgroundColor(mColorCode!!)

        color_slider.setSelectorColor(Color.TRANSPARENT)
        color_slider.setListener(mListener)
        mColorCode?.let {
            color_slider.setLastSelectedColor(it)
        }

        val fontList = fontProvider.getFontNames()
        val fontAdapter = FontAdater(context!!, android.R.layout.simple_spinner_dropdown_item, fontList)
        val fontPosition = fontList.indexOf(mFontName)
        spnFont.adapter = fontAdapter
        spnFont.setSelection(fontPosition)

        btnShow.setOnClickListener{
            spnFont.performClick()
        }

        spnFont.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                mFontName = fontList[i]
                edtContent.typeface = fontProvider.getTypeface(mFontName)
                fontAdapter.setSelection(i)
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        }

        txtDone.setOnClickListener {
            dismiss()
            mInputMethodManager!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            mContent = edtContent.text.toString()
            if (!TextUtils.isEmpty(mContent)) {
                mContent = fitString(edtContent, edtContent.text.toString())
                mTextEditor!!.onDone(mContent!!, mColorCode!!, mFontName!!)
            }
        }
    }

    private val mListener: ColorSlider.OnColorSelectedListener =
        object : ColorSlider.OnColorSelectedListener {
            override fun onColorChanged(position: Int, color: Int) {
                onColorSelected(color)
            }
        }

    val metrics = DisplayMetrics()
    val density = metrics.density

    private fun fitString(edtContent: AppCompatEditText, mContent: String) : String {
        val finalText = StringBuilder()
        return if (isTooLarge(edtContent, mContent)) {
            val lineList: List<String> = mContent.split("\n")
            if (lineList.isNotEmpty()) {
                for (i in lineList.indices) {
                    if (lineList[i].isNotEmpty()) {
                        if (isTooLarge(edtContent, lineList[i])) {
                            val wordList: List<String> = lineList[i].split(" ")
                            if (wordList.isNotEmpty()) {
                                val temp = java.lang.StringBuilder()
                                var lastWord: String? = ""
                                for (j in wordList.indices) {
                                    if (wordList[j].isNotEmpty()) {
                                        if (isTooLarge(edtContent, wordList[j])) {
                                            val newString =
                                                fitCharacter(edtContent, wordList[j])
                                            if (j == wordList.size - 1 && i == lineList.size - 1) {
                                                finalText.append(newString)
                                            } else {
                                                finalText.append(newString + "\n")
                                            }
                                        } else {
                                            lastWord = if (j == 0) {
                                                wordList[j]
                                            } else {
                                                " " + wordList[j]
                                            }
                                            temp.append(lastWord)
                                            if (isTooLarge(edtContent, temp.toString())) {
                                                temp.setLength(0) // clear String Builder,  new StringBuilder()
                                                temp.append(lastWord)
                                                if (j == wordList.size - 1 && i != lineList.size - 1) {
                                                    finalText.append("\n" + lastWord.trim { it <= ' ' } + "\n")
                                                } else {
                                                    finalText.append("\n" + lastWord.trim { it <= ' ' })
                                                }
                                            } else {
                                                if (j == wordList.size - 1 && i != lineList.size - 1) {
                                                    finalText.append(lastWord + "\n")
                                                } else {
                                                    finalText.append(lastWord)
                                                }
                                            }
                                        }
                                    } else {
                                        finalText.append(" ")
                                    }
                                }
                            } else {
                                Log.e(TAG, "fitString: wordList is Null or Empty.")
                            }
                        } else {
                            if (i == lineList.size - 1) {
                                finalText.append(lineList[i])
                            } else {
                                finalText.append(lineList[i] + "\n")
                            }
                        }
                    } else {
                        finalText.append(lineList[i] + "\n")
                    }
                }
            } else {
                finalText.append("")
            }
            finalText.toString()
        } else {
            mContent
        }
    }

    private fun fitCharacter(editText: EditText?, message: String): String? {
        val finalWord = StringBuilder()
        var startIndex = 0
        var endIndex = 1
        while (true) {
            val tempSplitWord = message.substring(startIndex, endIndex)
            if (!isTooLarge(editText, tempSplitWord)) { // isTooLarge
                if (endIndex < message.length) {
                    endIndex += 1
                } else {
                    val result = finalWord.append(tempSplitWord).toString()
                    return result
                }
            } else {
                endIndex -= 1
                val splitWord = message.substring(startIndex, endIndex)
                val isTooLarge = isTooLarge(editText, splitWord)
                if (!isTooLarge) {
                    finalWord.append(splitWord + "\n")
                }
                startIndex = endIndex
                endIndex += 1
            }
        }
    }

    private fun isTooLarge(editText: EditText?, newText: String): Boolean {
        return if (editText != null && editText.paint != null) {
            val textWidth = editText.paint.measureText(newText)
            textWidth >= editText.measuredWidth - 12 * density // editText.getMeasuredWidth();
        } else {
            false
        }
    }

    private fun changeTextEntityColor(initialColor: Int) {
        ColorPickerDialogBuilder
            .with(context)
            .setTitle(Constants.TITLE_CHANGE_COLOR)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(17)
            .setPositiveButton(Constants.DEFAULT_OK_BUTTON)
            { _, lastSelectedColor, _ ->
                mColorCode = lastSelectedColor
                edtContent.setTextColor(lastSelectedColor)
            }
            .setNegativeButton(Constants.DEFAULT_CANCEL_BUTTON) { _, _ ->
            }
            .build()
            .show()
    }

    override fun onColorSelected(color: Int) {
        if(color == 0){
            changeTextEntityColor(color)
        }
        else {
            mColorCode = color
            edtContent.setTextColor(color)
        }
    }

    fun setOnDoneListener(textEditor: TextEditor) {
        mTextEditor = textEditor
    }

    interface TextEditor {
        fun onDone(text: String, colorCode: Int, fontName: String)
    }

    companion object {
        private val TAG = EditDialogFragment::class.java.simpleName

        fun show(
            @NonNull appcompatActivity: AppCompatActivity,
            @NonNull inputText: String,
            @ColorInt colorCode: Int,
            @NonNull fontName: String
        ): EditDialogFragment {
            val args = Bundle()
            args.putString(Constants.TEXT_CONTENT, inputText)
            args.putInt(Constants.COLOR_CODE, colorCode)
            args.putString(Constants.FONT_NAME, fontName)
            val fragment = EditDialogFragment()
            fragment.arguments = args
            fragment.show(appcompatActivity.supportFragmentManager, TAG)
            return fragment
        }

        fun show(@NonNull appcompatActivity: AppCompatActivity): EditDialogFragment {
            return show(appcompatActivity, Constants.DEFAULT_TEXT, Color.WHITE, FontProvider.DEFAULT_FONT_NAME)
        }
    }
}