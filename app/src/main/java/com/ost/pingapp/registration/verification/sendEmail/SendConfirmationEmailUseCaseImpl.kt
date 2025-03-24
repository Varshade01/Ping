package com.khrd.pingapp.registration.verification.sendEmail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import javax.inject.Inject

class SendConfirmationEmailUseCaseImpl @Inject constructor(
    val firebaseAuth: FirebaseAuthAPI
) : SendConfirmationEmailUseCase {

    override fun sendConfirmationEmail(): LiveData<ISendConfirmationEmailState?> {
        val mutableStateLiveData = MutableLiveData<ISendConfirmationEmailState?>()
        firebaseAuth.sendConfirmationEmail {
            mutableStateLiveData.value = it
        }
        return mutableStateLiveData
    }
}