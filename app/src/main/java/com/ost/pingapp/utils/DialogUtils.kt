package com.khrd.pingapp.utils

import android.content.Context

interface DialogUtils {
    fun showSuccessDialog(context: Context, title: String, message: String, onBackPressed: () -> Unit)
    fun showWarningDialog(
        context: Context,
        title: String,
        message: String,
        confirmButtonText: String,
        showWarningIcon: Boolean,
        onCancelClickListener: () -> Unit,
        onConfirmClickListener: () -> Unit
    )
}