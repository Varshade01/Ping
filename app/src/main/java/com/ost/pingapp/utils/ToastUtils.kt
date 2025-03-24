package com.khrd.pingapp.utils

import android.content.Context
import android.widget.Toast.LENGTH_LONG
import androidx.annotation.StringRes

interface ToastUtils {
    fun showToast(message: String, duration: Int = LENGTH_LONG)
    fun showToast(@StringRes messageResId: Int, duration: Int = LENGTH_LONG)
    fun showShortToast(message: String)
    fun showShortToast(@StringRes messageResId: Int)
    fun showNetworkErrorToast()
    fun showUnknownErrorToast()
    fun showTooManyRequestToast()
    fun showInvalidCredentialsToast()
    fun showNoGoogleAccountsFoundToast()
    fun showFailedToSignInViaGoogleToast()
    fun showReceivedPingToast(pingMessage: String, senderName: String?)
    fun setContext(context: Context)
    fun removeContext()
}