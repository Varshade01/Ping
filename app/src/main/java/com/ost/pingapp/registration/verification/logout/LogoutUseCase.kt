package com.khrd.pingapp.registration.verification.logout

import androidx.lifecycle.LiveData

interface LogoutUseCase {
    fun logout(): LiveData<ILogoutState?>
}