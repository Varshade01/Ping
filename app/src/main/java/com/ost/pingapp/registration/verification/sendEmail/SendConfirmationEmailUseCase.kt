package com.khrd.pingapp.registration.verification.sendEmail

import androidx.lifecycle.LiveData

interface SendConfirmationEmailUseCase {
    fun sendConfirmationEmail(): LiveData<ISendConfirmationEmailState?>
}