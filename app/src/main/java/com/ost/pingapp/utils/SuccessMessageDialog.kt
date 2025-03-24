package com.khrd.pingapp.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.khrd.pingapp.databinding.DialogSuccessMessageBinding

object SuccessMessageDialog {
    fun show(context: Context, title: String, message: String, onOKClickListener: (() -> Unit)? = null) {
        val view = DialogSuccessMessageBinding.inflate(LayoutInflater.from(context))
        view.tvMessageDialogTitle.text = title
        view.tvMessageDialogMessage.text = message

        val alertDialog = AlertDialog.Builder(context).create()

        alertDialog.setView(view.root)

        alertDialog.setOnDismissListener {
            if (onOKClickListener != null) onOKClickListener()
        }

        view.btnMessageDialogOk.setOnClickListener {
            alertDialog.dismiss()
            if (onOKClickListener != null) onOKClickListener()
        }

        alertDialog.show()
    }
}