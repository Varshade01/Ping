package com.khrd.pingapp.homescreen.deleteAccount

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khrd.pingapp.homescreen.deleteAccount.usecases.IDeleteAccountUseCase
import com.khrd.pingapp.homescreen.usecases.pings.ClearReceivedPingsCacheUseCase
import com.khrd.pingapp.utils.passwordValidation.PasswordValidationUseCase
import com.khrd.pingapp.utils.passwordValidation.ValidationState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeleteAccountViewModel @Inject constructor(
    private val passwordValidationUseCase: PasswordValidationUseCase,
    private val deleteAccountUseCase: IDeleteAccountUseCase,
    private val clearReceivedPingsCacheUseCase: ClearReceivedPingsCacheUseCase
) : ViewModel() {

    private val _deleteAccountStateLiveData = MutableLiveData<DeleteAccountState>()
    val deleteAccountStateLiveData: LiveData<DeleteAccountState> get() = _deleteAccountStateLiveData

    fun deleteAccount(password: String) {
        when (passwordValidationUseCase.validatePassword(password)) {
            ValidationState.PASSWORD_IS_SHORT -> _deleteAccountStateLiveData.value =
                DeleteAccountFailure(DeleteAccountError.PASSWORD_IS_TOO_SHORT)
            ValidationState.PASSWORD_IS_BLANK -> _deleteAccountStateLiveData.value =
                DeleteAccountFailure(DeleteAccountError.EMPTY_FIELD)
            else -> password.let {
                deleteAccountUseCase.deleteAccount(password) {
                    if (it == DeleteAccountSuccess) {
                        clearPingsCache()
                    }
                    _deleteAccountStateLiveData.value = it
                }
            }
        }
    }

    fun clearPingsCache() {
        clearReceivedPingsCacheUseCase.clearCache()
    }
}