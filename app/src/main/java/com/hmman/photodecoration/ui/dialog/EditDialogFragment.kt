package com.hmman.photodecoration.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.hmman.photodecoration.R
import com.hmman.photodecoration.util.Constants
import kotlinx.android.synthetic.main.edit_dialog.*

class EditDialogFragment : DialogFragment() {

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

    var mTextEditor: TextEditor? = null
    var mColorCode: Int? = null
    var mContent: String? = null
    var mInputMethodManager: InputMethodManager? = null

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
        btnColor.setBackgroundColor(mColorCode!!)

        btnColor.setOnClickListener({
            changeTextEntityColor(mColorCode!!)
        })

        btnDone.setOnClickListener({
            dismiss()
            mInputMethodManager!!.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
            mContent = edtContent.text.toString()
            if (!TextUtils.isEmpty(mContent)){
                mTextEditor!!.onDone(mContent!!, mColorCode!!)
            }
        })
    }

    fun changeTextEntityColor(initialColor: Int){
        ColorPickerDialogBuilder
            .with(context)
            .setTitle("Select Color")
            .initialColor(initialColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .density(17)
            .setPositiveButton("OK", object : ColorPickerClickListener {
                override fun onClick(d: DialogInterface?, lastSelectedColor: Int, allColors: Array<out Int>?) {
                    mColorCode = lastSelectedColor
                    edtContent.setTextColor(lastSelectedColor)
                    btnColor.setBackgroundColor(lastSelectedColor)
                }
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener{ _, _ ->  })
            .build()
            .show()
    }


    fun setOnDoneListener (textEditor: TextEditor){
        mTextEditor = textEditor
    }

    interface TextEditor {
        fun onDone (text: String, colorCode: Int)
    }

    companion object {
        val TAG = EditDialogFragment::class.java.simpleName

        fun show (@NonNull appcompatActivity: AppCompatActivity,
                  @NonNull inputText: String,
                  @ColorInt colorCode: Int): EditDialogFragment{
            val args = Bundle()
            args.putString(Constants.TEXT_CONTENT, inputText)
            args.putInt(Constants.COLOR_CODE, colorCode)
            val fragment = EditDialogFragment()
            fragment.setArguments(args)
            fragment.show(appcompatActivity.supportFragmentManager, TAG)
            return fragment
        }

        //show dialog with empty text
        fun show (@NonNull appcompatActivity: AppCompatActivity) : EditDialogFragment{
            return show(appcompatActivity, "", Color.WHITE)
        }
    }
}