package com.khrd.pingapp.login

import android.util.Patterns
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.usecases.UpdateFcmTokenUseCase
import com.khrd.pingapp.utils.OnlineManager
import javax.inject.Inject

class LoginUseCaseImpl @Inject constructor(
    val firebaseAuth: FirebaseAuthAPI, var updateFcmTokenUseCase: UpdateFcmTokenUseCase, val onlineManager: OnlineManager
) : LoginUseCase {

    override suspend fun login(email: String, password: String): LoginState {
        if (email.isEmpty()) {
            return LoginState.EmptyEmail
        } else if (password.isEmpty()) {
            return LoginState.EmptyPassword
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return LoginState.InvalidEmail
        }
        val state = firebaseAuth.login(email, password)
        if (state == LoginState.LoginSuccess) {
            updateFcmTokenUseCase.updateFcmToken()
            onlineManager.start()
        }
        return state
    }
}