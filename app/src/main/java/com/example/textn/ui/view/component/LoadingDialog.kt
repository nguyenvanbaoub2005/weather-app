package com.example.textn.ui.view.component

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.textn.R

class LoadingDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.dialog_loading, null)

        val builder = AlertDialog.Builder(requireContext(), R.style.LoadingDialogTheme)
        builder.setView(view)
        isCancelable = false
        return builder.create()
    }
}