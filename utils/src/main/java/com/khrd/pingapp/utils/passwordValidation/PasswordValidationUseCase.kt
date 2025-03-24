package com.khrd.pingapp.utils.passwordValidation

interface PasswordValidationUseCase {
    fun validatePassword(password: String?): ValidationState
}