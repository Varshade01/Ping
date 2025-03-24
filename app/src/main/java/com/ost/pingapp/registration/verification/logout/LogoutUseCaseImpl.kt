package com.khrd.pingapp.registration.verification.logout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.usecases.RemoveFcmTokenUseCase
import com.khrd.pingapp.utils.OnlineManager
import javax.inject.Inject

class LogoutUseCaseImpl @Inject constructor(
    val firebaseAuth: FirebaseAuthAPI,
    val removeFcmTokenUseCase: RemoveFcmTokenUseCase,
    val onlineManager: OnlineManager
) : LogoutUseCase {

    override fun logout(): LiveData<ILogoutState?> {
        val mutableStateLiveData = MutableLiveData<ILogoutState?>()
        val currentUserId = firebaseAuth.currentUserId()
        firebaseAuth.logout {
            mutableStateLiveData.value = it
            if (it is LogoutSuccess) {
                onlineManager.stop(currentUserId)
                removeFcmTokenUseCase.removeFcmToken(currentUserId)
            }
        }
        Firebase.auth.signOut()
        return mutableStateLiveData
    }
}