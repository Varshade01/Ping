package com.khrd.pingapp.homescreen.changeEmail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.changeEmail.usecases.ChangeEmailUseCase
import com.khrd.pingapp.utils.passwordValidation.PasswordValidationUseCase
import com.khrd.pingapp.utils.passwordValidation.ValidationState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangeEmailViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuthAPI,
    private val changeEmailUseCase: ChangeEmailUseCase,
    private val passwordValidationUseCase: PasswordValidationUseCase
) : ViewModel() {
    private val _changeEmailStateLiveData = MutableLiveData<ChangeEmailState>()
    val changeEmailStateLiveData: LiveData<ChangeEmailState> get() = _changeEmailStateLiveData
    private var email: String? = null

    fun onEmailChanged(email: String?) {
        this.email = email?.lowercase()
    }

    fun changeEmail(password: String) {
        val userId = firebaseAuth.currentUserId()
        userId?.let {
            changeEmailUseCase.changeEmail(email!!, it, password) { state ->
                when (state) {
                    ChangeEmailSuccess -> {
                        _changeEmailStateLiveData.value = state
                    }
                    is ChangeEmailFailure -> {
                        _changeEmailStateLiveData.value = state
                    }
                    is ChangeEmailValidationSuccess -> _changeEmailStateLiveData.value =
                        ChangeEmailFailure(ChangeEmailError.SAME_EMAIL_ERROR)
                }
            }
        }
    }

    private fun validatePassword(password: String): Boolean {
        when (passwordValidationUseCase.validatePassword(password)) {
            ValidationState.VALID -> return true
            ValidationState.PASSWORD_IS_BLANK -> _changeEmailStateLiveData.value = ChangeEmailFailure(ChangeEmailError.EMPTY_PASSWORD_FIELD)
            else -> _changeEmailStateLiveData.value = ChangeEmailFailure(ChangeEmailError.INVALID_PASSWORD)
        }
        return false
    }

    private fun validateEmail(): Boolean {
        if (email.isNullOrBlank()) {
            _changeEmailStateLiveData.value = ChangeEmailFailure(ChangeEmailError.EMPTY_EMAIL_FIELD)
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _changeEmailStateLiveData.value = ChangeEmailFailure(ChangeEmailError.INVALID_EMAIL)
        } else if (email == firebaseAuth.currentUserEmail()!!.lowercase()) {
            _changeEmailStateLiveData.value = ChangeEmailFailure(ChangeEmailError.SAME_EMAIL_ERROR)
        } else {
            return true
        }
        return false
    }

    fun validateCredentials(password: String) {
        if (validateEmail() && validatePassword(password)) {
            _changeEmailStateLiveData.value = ChangeEmailValidationSuccess(email!!)
        }
    }
}