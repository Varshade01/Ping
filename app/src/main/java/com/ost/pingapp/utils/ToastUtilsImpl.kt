package com.khrd.pingapp.utils

import android.content.Context
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.khrd.pingapp.R

class ToastUtilsImpl : ToastUtils {

    private var context: Context? = null

    override fun removeContext() {
        context = null
    }

    override fun setContext(context: Context) {
        this.context = context
    }

    override fun showToast(message: String, duration: Int) {
        context?.let {
            Toast.makeText(it, message, duration).show()
        }
    }

    override fun showToast(messageResId: Int, duration: Int) {
        context?.let {
            Toast.makeText(it, it.getString(messageResId), duration).show()
        }
    }

    override fun showShortToast(message: String) {
        showToast(message, LENGTH_SHORT)
    }

    override fun showShortToast(messageResId: Int) {
        showToast(messageResId, LENGTH_SHORT)
    }

    override fun showNetworkErrorToast() {
        showToast(NO_INTERNET_CONNECTION_MESSAGE)
    }

    override fun showUnknownErrorToast() {
        showToast(UNKNOWN_ERROR_MESSAGE)
    }

    override fun showTooManyRequestToast() {
        showToast(TOO_MANY_REQUESTS_MESSAGE)
    }

    override fun showInvalidCredentialsToast() {
        showToast(INVALID_CREDENTIALS_MESSAGE)
    }

    override fun showNoGoogleAccountsFoundToast() {
        showToast(NO_GOOGLE_ACCOUNTS_FOUND_MESSAGE)
    }

    override fun showFailedToSignInViaGoogleToast() {
        showToast(FAILED_TO_SIGN_VIA_GOOGLE_MESSAGE)
    }

    override fun showReceivedPingToast(pingMessage: String, senderName: String?) {
        if (pingMessage.isBlank()) {
            showShortToast("${context?.getString(R.string.new_ping_from)} $senderName")
        } else {
            showShortToast("${senderName}, $pingMessage")
        }
    }

    companion object {
        const val NO_INTERNET_CONNECTION_MESSAGE = R.string.no_internet_connection
        const val UNKNOWN_ERROR_MESSAGE = R.string.unknown_error
        const val TOO_MANY_REQUESTS_MESSAGE = R.string.too_many_requests
        const val INVALID_CREDENTIALS_MESSAGE = R.string.invalid_login_credentials
        const val NO_GOOGLE_ACCOUNTS_FOUND_MESSAGE = R.string.no_google_account_found_error
        const val FAILED_TO_SIGN_VIA_GOOGLE_MESSAGE = R.string.failed_to_sign_in_via_google
    }
}