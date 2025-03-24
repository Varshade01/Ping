package com.khrd.pingapp.firebase.authentication

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.FirebaseAuth
import com.khrd.pingapp.homescreen.changeEmail.ChangeEmailError
import com.khrd.pingapp.homescreen.changeEmail.ChangeEmailFailure
import com.khrd.pingapp.homescreen.changeEmail.ChangeEmailState
import com.khrd.pingapp.homescreen.changeEmail.ChangeEmailSuccess
import com.khrd.pingapp.homescreen.changePassword.UpdatePasswordErrors
import com.khrd.pingapp.homescreen.changePassword.UpdatePasswordFailure
import com.khrd.pingapp.homescreen.changePassword.UpdatePasswordState
import com.khrd.pingapp.homescreen.changePassword.UpdatePasswordSuccess
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountError
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountFailure
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountState
import com.khrd.pingapp.homescreen.deleteAccount.DeleteAccountSuccess
import com.khrd.pingapp.login.LoginState
import com.khrd.pingapp.registration.*
import com.khrd.pingapp.registration.verification.logout.ILogoutState
import com.khrd.pingapp.registration.verification.logout.LogoutNetworkFailure
import com.khrd.pingapp.registration.verification.logout.LogoutSuccess
import com.khrd.pingapp.registration.verification.logout.LogoutUnknownFailure
import com.khrd.pingapp.registration.verification.sendEmail.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseAuth : FirebaseAuthAPI {
    private val firebaseAuth = FirebaseAuth.getInstance()

    override suspend fun login(email: String, password: String): LoginState {
        return suspendCoroutine { cont ->
            val callback = OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    if (firebaseAuth.currentUser?.isEmailVerified == true) {
                        cont.resume(LoginState.LoginSuccess)
                    } else {
                        cont.resume(LoginState.EmailNotVerified)
                    }
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            cont.resume(LoginState.InvalidCredentials)
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            cont.resume(LoginState.InvalidCredentials)
                        }
                        is FirebaseNetworkException -> {
                            cont.resume(LoginState.NetworkFailure)
                        }
                        is FirebaseTooManyRequestsException -> {
                            cont.resume(LoginState.TooManyRequests)
                        }
                        else -> {
                            cont.resume(LoginState.UnknownFailure)
                        }
                    }
                }
            }
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(callback)
        }
    }

    override suspend fun register(email: String, password: String): IRegistrationState {
        return suspendCoroutine { cont ->
            val callback = OnCompleteListener<AuthResult> { task ->
                if (task.isSuccessful) {
                    firebaseAuth.currentUser?.sendEmailVerification()
                    cont.resume(RegistrationSuccess())
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> {
                            cont.resume(RegistrationUserExistFailure())
                        }
                        is FirebaseAuthUserCollisionException -> {
                            cont.resume(RegistrationEmailAlreadyInUseFailure())
                        }
                        is FirebaseNetworkException -> {
                            cont.resume(RegistrationNetworkFailure())
                        }
                        else -> {
                            cont.resume(RegistrationUnknownFailure())
                        }
                    }
                }
            }
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(callback)
        }
    }

    override fun logout(callback: (ILogoutState) -> Unit) {
        try {
            firebaseAuth.signOut()
            callback(LogoutSuccess())
        } catch (e: Exception) {
            e.message?.let { it1 -> Log.e("*****", it1) }
            when (e) {
                is FirebaseNetworkException -> {
                    callback(LogoutNetworkFailure())
                }
                else -> {
                    callback(LogoutUnknownFailure())
                }
            }
        }
    }

    override fun sendConfirmationEmail(callback: (ISendConfirmationEmailState) -> Unit) {
        firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
                callback(SendConfirmationEmailSuccess())
            } else {
                when (it.exception) {
                    is FirebaseNetworkException -> {
                        callback(SendConfirmationEmailNetworkFailure())
                    }
                    is FirebaseTooManyRequestsException -> {
                        callback(SendConfirmationEmailTooManyRequests())
                    }
                    else -> {
                        callback(SendConfirmationEmailUnknownFailure())
                    }
                }
            }
        }
    }


    override fun isEmailVerified(): Boolean {
        val user = firebaseAuth.currentUser
        val infos: List<UserInfo?> = user!!.providerData
        for (userinfo in infos) {
            if (userinfo?.providerId == GoogleAuthProvider.PROVIDER_ID || userinfo?.providerId == FacebookAuthProvider.PROVIDER_ID ||
                firebaseAuth.currentUser!!.isEmailVerified) {
                return true
            }
        }
       return false
    }

    override fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun currentUserEmail(): String? = firebaseAuth.currentUser?.email

    override fun reauthenticate(password: String, callback: (ReauthenticationState) -> Unit) {
        val user = firebaseAuth.currentUser
        val credentials = EmailAuthProvider.getCredential(user?.email!!, password)
        user.reauthenticate(credentials).addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    callback(ReauthenticationSuccess)
                }
                task.exception is FirebaseNetworkException -> {
                    callback(ReauthenticationFailure(ReauthenticationError.NetworkFailure))
                }
                task.exception is FirebaseTooManyRequestsException -> {
                    callback(ReauthenticationFailure(ReauthenticationError.TooManyRequests))
                }
                task.exception is FirebaseAuthInvalidCredentialsException -> {
                    callback(ReauthenticationFailure(ReauthenticationError.InvalidCredentials))
                }
                else -> {
                    callback(ReauthenticationFailure(ReauthenticationError.UnknownFailure))
                }
            }
        }
    }

    override fun changePassword(password: String, callback: (UpdatePasswordState) -> Unit) {
        val user = firebaseAuth.currentUser
        user?.updatePassword(password)?.addOnCompleteListener {
            when {
                it.isSuccessful -> {
                    callback(UpdatePasswordSuccess())
                }
                it.exception is FirebaseNetworkException -> {
                    callback(UpdatePasswordFailure(UpdatePasswordErrors.NO_INTERNET_CONNECTION))
                }
                it.exception is FirebaseTooManyRequestsException -> {
                    callback(UpdatePasswordFailure(UpdatePasswordErrors.TOO_MANY_REQUESTS))
                }
                else -> {
                    callback(UpdatePasswordFailure(UpdatePasswordErrors.UNKNOWN_ERROR))
                }
            }
        }
    }

    override fun updateEmail(email: String, callback: (ChangeEmailState) -> Unit) {
        firebaseAuth.currentUser?.updateEmail(email)?.addOnCompleteListener { task ->
            when {
                task.isSuccessful -> {
                    callback(ChangeEmailSuccess)
                }
                task.exception is FirebaseAuthUserCollisionException -> {
                    callback(ChangeEmailFailure(ChangeEmailError.EMAIL_ALREADY_REGISTERED))
                }
                task.exception is FirebaseNetworkException -> {
                    callback(ChangeEmailFailure(ChangeEmailError.NETWORK_ERROR))
                }
                else -> {
                    callback(ChangeEmailFailure(ChangeEmailError.UNKNOWN_ERROR))
                }
            }
        }
    }

    override fun deleteAccount(callback: (DeleteAccountState) -> Unit) {
        firebaseAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i("***", "User account deleted.")
                callback(DeleteAccountSuccess)
            } else {
                Log.i("***", "User account deletion error.")
                callback(DeleteAccountFailure(DeleteAccountError.CONNECTION_ERROR))
            }
        }
    }

    override fun reload(callback: (ReloadState) -> Unit) {
        firebaseAuth.currentUser?.reload()?.addOnCompleteListener() {
            if (it.isSuccessful) {
                callback(ReloadStateSuccess)
            } else {
                if (it.exception is FirebaseNetworkException) {
                    callback(ReloadStateFailure(ReloadError.NetworkFailure))
                } else if (it.exception is FirebaseAuthInvalidUserException || it.exception is FirebaseAuthException) {
                    callback(ReloadStateFailure(ReloadError.InvalidUserFailure))
                } else {
                    callback(ReloadStateFailure(ReloadError.UnknownFailure))
                }
            }
        }
    }
}
