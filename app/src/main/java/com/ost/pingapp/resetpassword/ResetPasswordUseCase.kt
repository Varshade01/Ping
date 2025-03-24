package com.khrd.pingapp.resetpassword

import androidx.lifecycle.LiveData

interface ResetPasswordUseCase {
    fun resetPassword(email: String): LiveData<ResetPasswordState?>
}