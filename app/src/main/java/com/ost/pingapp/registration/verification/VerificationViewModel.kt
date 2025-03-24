package com.khrd.pingapp.registration.verification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.khrd.pingapp.registration.verification.isEmailVerified.IsEmailVerifiedUseCase
import com.khrd.pingapp.registration.verification.logout.ILogoutState
import com.khrd.pingapp.registration.verification.logout.LogoutUseCase
import com.khrd.pingapp.registration.verification.sendEmail.ISendConfirmationEmailState
import com.khrd.pingapp.registration.verification.sendEmail.SendConfirmationEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val isEmailVerifiedUseCase: IsEmailVerifiedUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sendConfirmationEmailUseCase: SendConfirmationEmailUseCase,
) : ViewModel() {
    private val loginIsClicked = MutableLiveData<Boolean>()
    private val logoutIsClicked = MutableLiveData<Boolean>()
    private val sendConfirmEmail = MutableLiveData<Boolean>()

    val isEmailVerifiedLiveData: LiveData<Boolean>
        get() = Transformations.switchMap(loginIsClicked) {
            isEmailVerifiedUseCase.isEmailVerified()
        }

    val logoutStateLiveData: LiveData<ILogoutState?>
        get() = Transformations.switchMap(logoutIsClicked) {
            logoutUseCase.logout()
        }

    val sendConfirmationEmailLiveData: LiveData<ISendConfirmationEmailState>
        get() = Transformations.switchMap(sendConfirmEmail) {
            sendConfirmationEmailUseCase.sendConfirmationEmail()
        }

    fun login() {
        loginIsClicked.value = true
    }

    fun logout() {
        logoutIsClicked.value = true
    }

    fun sendConfirmationEmail() {
        sendConfirmEmail.value = true
    }

}