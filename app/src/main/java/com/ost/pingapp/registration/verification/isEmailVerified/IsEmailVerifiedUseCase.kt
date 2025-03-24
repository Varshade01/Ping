package com.khrd.pingapp.registration.verification.isEmailVerified

import androidx.lifecycle.LiveData

interface IsEmailVerifiedUseCase {
    fun isEmailVerified(): LiveData<Boolean>
}