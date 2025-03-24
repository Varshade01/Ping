package com.khrd.pingapp.homescreen.changeEmail.usecases

import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.authentication.ReauthenticationError
import com.khrd.pingapp.firebase.authentication.ReauthenticationFailure
import com.khrd.pingapp.firebase.authentication.ReauthenticationSuccess
import com.khrd.pingapp.homescreen.changeEmail.*
import com.khrd.pingapp.registration.verification.sendEmail.SendConfirmationEmailNetworkFailure
import com.khrd.pingapp.registration.verification.sendEmail.SendConfirmationEmailSuccess
import com.khrd.pingapp.registration.verification.sendEmail.SendConfirmationEmailTooManyRequests
import com.khrd.pingapp.registration.verification.sendEmail.SendConfirmationEmailUnknownFailure
import com.khrd.pingapp.repository.users.UsersRepository
import javax.inject.Inject

class ChangeEmailUseCaseImpl @Inject constructor(
    private val usersRepository: UsersRepository,
    private val firebaseAuth: FirebaseAuthAPI
) : ChangeEmailUseCase {
    override fun changeEmail(newEmail: String, userId: String, password: String, callback: (ChangeEmailState) -> Unit) {
        firebaseAuth.reauthenticate(password) {
            when (it) {
                is ReauthenticationFailure -> {
                    when (it.error) {
                        ReauthenticationError.InvalidCredentials -> callback(ChangeEmailFailure(ChangeEmailError.INVALID_PASSWORD))
                        ReauthenticationError.NetworkFailure -> callback(ChangeEmailFailure(ChangeEmailError.NETWORK_ERROR))
                        ReauthenticationError.TooManyRequests -> callback(ChangeEmailFailure(ChangeEmailError.TOO_MANY_REQUESTS))
                        ReauthenticationError.UnknownFailure -> callback(ChangeEmailFailure(ChangeEmailError.UNKNOWN_ERROR))
                    }
                }
                ReauthenticationSuccess -> {
                    firebaseAuth.updateEmail(email = newEmail) {
                        when (it) {
                            ChangeEmailSuccess -> {
                                firebaseAuth.sendConfirmationEmail {
                                    when(it) {
                                        is SendConfirmationEmailNetworkFailure -> callback(ChangeEmailFailure(ChangeEmailError.NETWORK_ERROR))
                                        is SendConfirmationEmailTooManyRequests -> callback(ChangeEmailFailure(ChangeEmailError.TOO_MANY_REQUESTS))
                                        is SendConfirmationEmailUnknownFailure -> callback(ChangeEmailFailure(ChangeEmailError.UNKNOWN_ERROR))
                                        is SendConfirmationEmailSuccess -> {
                                            usersRepository.updateEmail(id = userId, email = newEmail) {
                                                when (it) {
                                                    UserRequestState.UserRequestFail -> {
                                                        callback(ChangeEmailFailure(ChangeEmailError.UNKNOWN_ERROR))
                                                    }
                                                    is UserRequestState.UserRequestSuccess -> callback(ChangeEmailSuccess)
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                            is ChangeEmailFailure -> callback(it)
                            is ChangeEmailValidationSuccess -> callback(ChangeEmailFailure(ChangeEmailError.UNKNOWN_ERROR))
                        }
                    }
                }
            }
        }
    }
}