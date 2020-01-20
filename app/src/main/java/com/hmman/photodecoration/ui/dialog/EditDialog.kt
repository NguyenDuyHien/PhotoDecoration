package com.hmman.photodecoration.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import com.hmman.photodecoration.R
import com.hmman.photodecoration.util.Constants

class EditDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var view: View = inflater.inflate(R.layout.edit_dialog, container, false)
        val content = arguments?.getString(Constants.TEXT_CONTENT)
        val edtContent = view.findViewById<AppCompatEditText>(R.id.edtContent)
        edtContent.setText(content)
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
        return dialog
    }
}