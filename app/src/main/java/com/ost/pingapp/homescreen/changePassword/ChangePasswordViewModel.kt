package com.khrd.pingapp.homescreen.changePassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khrd.pingapp.utils.passwordValidation.PasswordValidationUseCase
import com.khrd.pingapp.utils.passwordValidation.ValidationState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val validationUseCaseImpl: PasswordValidationUseCase
) : ViewModel() {
    private val _changePasswordStateLiveData = MutableLiveData<ChangePasswordState>()
    val changePasswordStateLiveData: LiveData<ChangePasswordState> get() = _changePasswordStateLiveData
    private val _currentPasswordStateLiveData = MutableLiveData<ValidationState>()
    val currentPasswordStateLiveData: LiveData<ValidationState> get() = _currentPasswordStateLiveData
    private val _newPasswordStateLiveData = MutableLiveData<ValidationState>()
    val newPasswordStateLiveData: LiveData<ValidationState> get() = _newPasswordStateLiveData

    fun changePassword(currentPassword: String, newPassword: String) {
        val errors = mutableSetOf<ValidationState>()
        errors.add(validationUseCaseImpl.validatePassword(currentPassword))
        errors.add(validationUseCaseImpl.validatePassword(newPassword))
        errors.remove(ValidationState.VALID)
        if (errors.isEmpty()) {
            changePasswordUseCase.changePassword(currentPassword, newPassword) {
                when (it) {
                    is UpdatePasswordSuccess -> {
                        _changePasswordStateLiveData.value = ChangePasswordSuccess()
                    }
                    is UpdatePasswordFailure -> {
                        _changePasswordStateLiveData.value = ChangePasswordFailure(it.error)
                    }
                }
            }
        } else {
            _currentPasswordStateLiveData.value = validationUseCaseImpl.validatePassword(currentPassword)
            _newPasswordStateLiveData.value = validationUseCaseImpl.validatePassword(newPassword)
        }
    }
}