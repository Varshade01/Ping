package com.khrd.pingapp.registration.verification.isEmailVerified

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.authentication.ReloadStateFailure
import com.khrd.pingapp.firebase.authentication.ReloadStateSuccess
import javax.inject.Inject

class IsEmailVerifiedUseCaseImpl @Inject constructor(
    val firebaseAuth: FirebaseAuthAPI
) : IsEmailVerifiedUseCase {

    override fun isEmailVerified(): LiveData<Boolean> {
        val mutableStateLiveData = MutableLiveData<Boolean>()
        firebaseAuth.reload {
            when(it){
                ReloadStateSuccess -> mutableStateLiveData.value = firebaseAuth.isEmailVerified()
                is ReloadStateFailure ->{}
            }
        }
        return mutableStateLiveData
    }
}