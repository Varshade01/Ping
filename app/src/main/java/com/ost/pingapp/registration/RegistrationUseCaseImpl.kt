package com.khrd.pingapp.registration

import com.khrd.pingapp.utils.passwordValidation.PasswordValidationUseCase
import com.khrd.pingapp.utils.passwordValidation.ValidationState
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.utils.validateLetters
import javax.inject.Inject

class RegistrationUseCaseImpl @Inject constructor(
    val firebaseAuth: FirebaseAuthAPI,
    val passwordValidationUseCase: PasswordValidationUseCase
) : RegistrationUseCase {

    override suspend fun registerUser(fields: RegistrationFields): IRegistrationState {
        val validationErrors = mutableSetOf<ValidationState>()
        validationErrors.add(checkNameValidation(fields.name))
        validationErrors.add(checkEmailValidation(fields.email))
        validationErrors.add(passwordValidationUseCase.validatePassword(fields.password))
        validationErrors.add(checkConsent(fields.consent))
        validationErrors.remove(ValidationState.VALID)
        return if (validationErrors.isEmpty()) {
            firebaseAuth.register(fields.email, fields.password)
        } else {
            RegistrationValidationFailure(validationErrors)
        }
    }

    private fun checkNameValidation(name: String): ValidationState {
        return if (name.isEmpty()) {
            ValidationState.NAME_IS_BLANK
        } else if (!validateLetters(name)) {
            ValidationState.NAME_LETTERS_FAILURE
        } else if (name.length >= 32) {
            ValidationState.NAME_LENGTH_FAILURE
        } else {
            ValidationState.VALID
        }
    }

    private fun checkEmailValidation(email: String): ValidationState {
        return if (email.isEmpty()) {
            ValidationState.EMAIL_IS_BLANK
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ValidationState.EMAIL_IS_INVALID
        } else {
            ValidationState.VALID
        }
    }

    private fun checkConsent(consent: Boolean): ValidationState = if (consent) ValidationState.VALID else ValidationState.CONSENT_NOT_GIVEN
}