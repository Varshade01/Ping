package com.khrd.pingapp.resetpassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import javax.inject.Inject

class ResetPasswordUseCaseImpl @Inject constructor() : ResetPasswordUseCase {

    override fun resetPassword(email: String): LiveData<ResetPasswordState?> {
        val mutableStateLiveData = MutableLiveData<ResetPasswordState?>()
        FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim()).addOnCompleteListener { task ->
            if(task.isSuccessful)
                mutableStateLiveData.value = ResetPasswordState.ResetPasswordSuccess
            else {
                if(task.exception is FirebaseNetworkException){
                    mutableStateLiveData.value = ResetPasswordState.NetworkFailure
                }
                else if(task.exception is FirebaseAuthInvalidUserException){
                    mutableStateLiveData.value = ResetPasswordState.InvalidEmail
                }
                else if(task.exception is FirebaseTooManyRequestsException) {
                    mutableStateLiveData.value = ResetPasswordState.TooManyRequests
                }
                else {
                    mutableStateLiveData.value = ResetPasswordState.UnknownFailure
                }
            }
        }
        return mutableStateLiveData
    }
}