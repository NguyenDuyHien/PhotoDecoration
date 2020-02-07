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
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.hmman.photodecoration.R
import com.hmman.photodecoration.util.Constants
import kotlinx.android.synthetic.main.edit_dialog.*

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
    private var mInputMethodManager: InputMethodManager? = null
    var mColorCode: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mInputMethodManager =
            activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        edtContent.requestFocus()

        mInputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        mColorCode = arguments!!.getInt(Constants.COLOR_CODE)
        mContent = arguments!!.getString(Constants.TEXT_CONTENT)
        edtContent.setTextColor(mColorCode!!)
        edtContent.setText(mContent)
        edtContent.setSelection(edtContent.getText()!!.length)
//        btnColor.setBackgroundColor(mColorCode!!)

        color_slider.setSelectorColor(Color.TRANSPARENT)
        color_slider.setListener(mListener)


        txtDone.setOnClickListener {
            dismiss()
            mInputMethodManager!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            mContent = edtContent.text.toString()
            if (!TextUtils.isEmpty(mContent)) {
                mContent = fitString(edtContent, edtContent.text.toString())
                mTextEditor!!.onDone(mContent!!, mColorCode!!)
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
//    activity!!.windowManager.defaultDisplay.getMetrics(metrics)
    val density = metrics.density

    private fun fitString(edtContent: AppCompatEditText, mContent: String) : String {
        val finalText = StringBuilder()
        return if (isTooLarge(edtContent, mContent)) {
            Log.i(TAG, "fitString: isTooLarge 1 : " + true)
            val lineList: List<String> = mContent.split("\n")
            Log.i(TAG, "fitString: stringList$lineList")
            if (lineList != null && lineList.size > 0) {
                for (i in lineList.indices) {
                    if (lineList[i] != null && !lineList[i].isEmpty()) {
                        if (isTooLarge(edtContent, lineList[i])) {
                            Log.i(
                                TAG,
                                "fitString: isTooLarge 2 : " + lineList[i] + " == " + true
                            )
                            val wordList: List<String> = lineList[i].split(" ")
                            Log.i(TAG, "fitString: wordList$wordList")
                            if (wordList != null && wordList.size > 0) {
                                Log.i(TAG, "fitString: wordList : " + wordList.size)
                                val temp = java.lang.StringBuilder()
                                var lastWord: String? = ""
                                for (j in wordList.indices) {
                                    if (wordList[j] != null && !wordList[j].isEmpty()) {
                                        if (isTooLarge(edtContent, wordList[j])) {
                                            Log.i(
                                                TAG,
                                                "fitString: isTooLarge 3 : " + wordList[j] + " == " + true
                                            )
                                            val newString =
                                                fitCharacter(edtContent, wordList[j])
                                            Log.i(
                                                TAG,
                                                "fitString: fitCharacter == $newString"
                                            )
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
                                            Log.i(TAG, "fitString: temp : $temp")
                                            Log.i(
                                                TAG,
                                                "fitString: lastWord : $lastWord"
                                            )
                                            if (isTooLarge(edtContent, temp.toString())) {
                                                temp.setLength(0) // clear String Builder,  new StringBuilder()
                                                temp.append(lastWord)
                                                if (j == wordList.size - 1 && i != lineList.size - 1) {
                                                    Log.i(TAG, "fitString: ###### 1")
                                                    finalText.append("\n" + lastWord!!.trim { it <= ' ' } + "\n")
                                                } else {
                                                    Log.i(TAG, "fitString: ###### 2")
                                                    finalText.append("\n" + lastWord!!.trim { it <= ' ' })
                                                }
                                            } else {
                                                if (j == wordList.size - 1 && i != lineList.size - 1) {
                                                    Log.i(TAG, "fitString: ###### 3")
                                                    finalText.append(lastWord + "\n")
                                                } else {
                                                    Log.i(TAG, "fitString: ###### 4")
                                                    finalText.append(lastWord)
                                                }
                                            }
                                            Log.i(
                                                TAG,
                                                "fitString: finalMessage : $finalText"
                                            )
                                        }
                                    } else {
                                        Log.e(TAG, "fitString: Word is Null or Empty.")
                                        finalText.append(" ")
                                    }
                                }
                            } else {
                                Log.e(TAG, "fitString: wordList is Null or Empty.")
                            }
                        } else {
                            Log.i(
                                TAG,
                                "fitString: isTooLarge 2 : " + lineList[i] + " == " + false
                            )
                            if (i == lineList.size - 1) {
                                finalText.append(lineList[i])
                            } else {
                                finalText.append(lineList[i].toString() + "\n")
                            }
                        }
                    } else {
                        Log.e(TAG, "fitString: Line is Null or Empty.")
                        finalText.append(lineList[i].toString() + "\n")
                    }
                }
            } else {
                Log.e(TAG, "fitString: stringList is Null or Empty.")
                finalText.append("")
            }
            finalText.toString()
        } else {
            Log.i(TAG, "fitString: isTooLarge : " + false)
            mContent
        }
    }

    fun fitCharacter(editText: EditText?, message: String): String? {
        Log.i(TAG, "fitCharacter2: Default Word : $message")
        val finalWord = StringBuilder()
        var startIndex = 0
        var endIndex = 1
        while (true) {
            val tempSplitWord = message.substring(startIndex, endIndex)
            Log.i(
                TAG,
                "fitCharacter2: startIndex : $startIndex endIndex : $endIndex tempSplitWord : $tempSplitWord"
            )
            if (!isTooLarge(editText, tempSplitWord)) { // isTooLarge
                if (endIndex < message.length) {
                    endIndex = endIndex + 1
                    Log.i(
                        TAG,
                        "IF fitCharacter2: endIndex < message.length() " + endIndex + " < " + message.length
                    )
                } else {
                    val result = finalWord.append(tempSplitWord).toString()
                    Log.i(TAG, "IF RETURN RESULT : $result")
                    return result
                }
            } else {
                endIndex = endIndex - 1
                val splitWord = message.substring(startIndex, endIndex)
                Log.i(
                    TAG,
                    "ELSE fitCharacter2: startIndex : $startIndex endIndex : $endIndex splitWord : $splitWord"
                )
                val isTooLarge = isTooLarge(editText, splitWord)
                if (!isTooLarge) {
                    finalWord.append(splitWord + "\n")
                }
                startIndex = endIndex
                endIndex = endIndex + 1
                Log.i(
                    TAG,
                    "ELSE fitCharacter2: startIndex : $startIndex endIndex : $endIndex"
                )
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

    private fun changeTextEntityColor() {
        ColorPickerDialogBuilder
            .with(context)
            .setTitle(Constants.TITLE_CHANGE_COLOR)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(17)
            .setPositiveButton(Constants.DEFAULT_OK_BUTTON)
            { _, lastSelectedColor, _ ->
                mColorCode = lastSelectedColor
                edtContent.setTextColor(lastSelectedColor)
//                btnColor.setBackgroundColor(lastSelectedColor)
            }
            .setNegativeButton(Constants.DEFAULT_CANCEL_BUTTON) { _, _ ->
            }

            .build()
            .show()
    }

    override fun onColorSelected(color: Int) {
        if( color == 0){
                changeTextEntityColor()
        }
        else {
            mColorCode= color
            edtContent.setTextColor(color)
//            btnColor.setBackgroundColor(color)
        }
    }

    fun setOnDoneListener(textEditor: TextEditor) {
        mTextEditor = textEditor
    }

    interface TextEditor {
        fun onDone(text: String, colorCode: Int)
    }

    companion object {
        private val TAG = EditDialogFragment::class.java.simpleName

        fun show(
            @NonNull appcompatActivity: AppCompatActivity,
            @NonNull inputText: String,
            @ColorInt colorCode: Int
        ): EditDialogFragment {
            val args = Bundle()
            args.putString(Constants.TEXT_CONTENT, inputText)
            args.putInt(Constants.COLOR_CODE, colorCode)
            val fragment = EditDialogFragment()
            fragment.arguments = args
            fragment.show(appcompatActivity.supportFragmentManager, TAG)
            return fragment
        }

        //show dialog with empty text
        fun show(@NonNull appcompatActivity: AppCompatActivity): EditDialogFragment {
            return show(appcompatActivity, Constants.DEFAULT_TEXT, Color.WHITE)
        }
    }
}