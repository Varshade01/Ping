package com.khrd.pingapp.utils

import android.content.Context

class DialogUtilsImpl() : DialogUtils {
    override fun showSuccessDialog(context: Context, title: String, message: String, onBackPressed: () -> Unit) {
        SuccessMessageDialog.show(
            context = context,
            title = title,
            message = message,
            onOKClickListener = onBackPressed
        )
    }

    override fun showWarningDialog(
        context: Context,
        title: String,
        message: String,
        confirmButtonText: String,
        showWarningIcon: Boolean,
        onCancelClickListener: () -> Unit,
        onConfirmClickListener: () -> Unit
    ) {
        WarningDialog.show(
            context = context,
            title = title,
            message = message,
            confirmButtonText = confirmButtonText,
            showWarningIcon = showWarningIcon,
            onCancelClickListener = onCancelClickListener,
            onConfirmClickListener = onConfirmClickListener
        )
    }
}