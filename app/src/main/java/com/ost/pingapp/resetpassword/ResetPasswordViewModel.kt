package com.khrd.pingapp.resetpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {
    private val _emailIsValid = MutableLiveData<Boolean>()
    private val emailLiveData = MutableLiveData<String>()

    val resetPasswordStateLiveData: LiveData<ResetPasswordState?> get() =
        Transformations.switchMap(emailLiveData) {
            resetPasswordUseCase.resetPassword(it)
        }
    val emailIsValid: LiveData<Boolean> get() = _emailIsValid

    fun onEditTextEmailAddressTextChange(email: CharSequence) {
        _emailIsValid.value = email.trim().isValid()
    }

    fun onResetPasswordButtonClick(email: CharSequence) {
        emailLiveData.value = email.toString()
    }

    private fun CharSequence.isValid(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}