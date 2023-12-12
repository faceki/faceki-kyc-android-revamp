package com.faceki.android.util

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.faceki.android.R


/**
 * A simple [Fragment] subclass.
 * Use the [AnalyzingProgressDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnalyzingProgressDialogFragment : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isCancelable = false

        return inflater.inflate(R.layout.fragment_analyzing_progress_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.setOnKeyListener { _, keyCode, _ ->
            return@setOnKeyListener keyCode == KeyEvent.KEYCODE_BACK
        }
    }


    companion object {
        fun newInstance(): AnalyzingProgressDialogFragment {
            return AnalyzingProgressDialogFragment()
        }
    }
}