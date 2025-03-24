package com.khrd.pingapp.utils.passwordValidation

import com.khrd.pingapp.utils.containsDigit
import com.khrd.pingapp.utils.containsLowerLetter
import com.khrd.pingapp.utils.containsUpperLetter

class PasswordValidationUseCaseImpl : PasswordValidationUseCase {
    override fun validatePassword(password: String?): ValidationState {
        return if (password.isNullOrBlank()) {
            ValidationState.PASSWORD_IS_BLANK
        } else if (password.length < 8) {
            ValidationState.PASSWORD_IS_SHORT
        } else if (password.length > 32) {
            ValidationState.PASSWORD_IS_LONG
        } else if (!password.containsDigit()) {
            ValidationState.PASSWORD_WITHOUT_DIGIT
        } else if (!password.containsUpperLetter()) {
            ValidationState.PASSWORD_WITHOUT_UPPER_LETTER
        } else if (!password.containsLowerLetter()) {
            ValidationState.PASSWORD_WITHOUT_LOWER_LETTER
        } else {
            ValidationState.VALID
        }
    }
}