package com.khrd.pingapp.firebase.authentication

import com.khrd.pingapp.homescreen.changeEmail.ChangeEmailState
import com.khrd.pingapp.homescreen.changePassword.UpdatePasswordState
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountState
import com.khrd.pingapp.login.LoginState
import com.khrd.pingapp.registration.IRegistrationState
import com.khrd.pingapp.registration.verification.logout.ILogoutState
import com.khrd.pingapp.registration.verification.sendEmail.ISendConfirmationEmailState

interface FirebaseAuthAPI {
    suspend fun login(email: String, password: String): LoginState
    suspend fun register(email: String, password: String):IRegistrationState
    fun logout(callback: (ILogoutState) -> Unit)
    fun sendConfirmationEmail(callback: (ISendConfirmationEmailState) -> Unit)
    fun isEmailVerified(): Boolean
    fun currentUserId(): String?
    fun currentUserEmail(): String?
    fun reauthenticate(password: String, callback: (ReauthenticationState) -> Unit)
    fun changePassword(password: String, callback: (UpdatePasswordState) -> Unit)
    fun updateEmail(email: String, callback: (ChangeEmailState) -> Unit)
    fun deleteAccount(callback: (DeleteAccountState) -> Unit)
    fun reload(callback: (ReloadState) -> Unit)
}