package com.khrd.pingapp.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.khrd.pingapp.databinding.DialogWarningBinding

object WarningDialog {
    fun show(
        context: Context,
        title: CharSequence,
        message: CharSequence,
        confirmButtonText: CharSequence,
        showWarningIcon: Boolean = true,
        onCancelClickListener: (() -> Unit)? = null,
        onConfirmClickListener: (() -> Unit)? = null
    ) {
        val view = DialogWarningBinding.inflate(LayoutInflater.from(context))
        view.tvWarningDialogTitle.text = title
        view.tvWarningDialogMessage.text = message
        view.btnWarningDialogConfirm.text = confirmButtonText

        if (showWarningIcon) {
            view.ivWarningDialogWarning.visibility = View.VISIBLE
        } else {
            view.ivWarningDialogWarning.visibility = View.GONE
        }


        val alertDialog = AlertDialog.Builder(context).create()

        alertDialog.setView(view.root)

        alertDialog.setOnDismissListener {
            if (onCancelClickListener != null) onCancelClickListener()
        }

        view.btnWarningDialogCancel.setOnClickListener {
            alertDialog.dismiss()
            if (onCancelClickListener != null) onCancelClickListener()
        }

        view.btnWarningDialogConfirm.setOnClickListener {
            alertDialog.dismiss()
            if (onConfirmClickListener != null) onConfirmClickListener()
        }
        alertDialog.show()
    }
}