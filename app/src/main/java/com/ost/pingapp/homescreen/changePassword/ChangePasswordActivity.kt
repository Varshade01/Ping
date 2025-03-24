package com.khrd.pingapp.homescreen.changePassword

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khrd.pingapp.utils.passwordValidation.ValidationState
import com.khrd.pingapp.PingAppBaseActivity
import com.khrd.pingapp.R
import com.khrd.pingapp.utils.SuccessMessageDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordActivity : PingAppBaseActivity() {

    private val viewModel: ChangePasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChangePasswordScreen(modifier = Modifier)
            HandleSuccessDialog()
        }
    }

    @Composable
    fun ChangePasswordScreen(modifier: Modifier) {
        Surface(color = colorResource(id = R.color.intouch_background), modifier = modifier.fillMaxHeight()) {
            Column {
                SetToolbar()
                SetPasswordChangeInstruction(modifier)
                Spacer(modifier = modifier.height(8.dp))
                ChangePasswordBlock(viewModel, modifier)
                Spacer(modifier = modifier.weight(1f))
                CancelButton(modifier)
            }
        }
    }

    @Composable
    private fun HandleSuccessDialog() {
        val changePasswordState by viewModel.changePasswordStateLiveData.observeAsState()
        when (changePasswordState) {
            is ChangePasswordSuccess -> ShowSuccessDialog()
            is ChangePasswordFailure -> handleChangePasswordErrors(changePasswordState as ChangePasswordFailure)
        }
    }

    @Composable
    private fun ShowSuccessDialog() {
        SuccessMessageDialog.show(
            context = this,
            title = getString(R.string.success),
            message = getString(R.string.password_changed_successful)
        ) { onBackPressed() }
    }

    @Composable
    private fun SetToolbar() {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.change_password)) },
            navigationIcon = { ToolbarIcon() },
            backgroundColor = colorResource(id = R.color.intouch_background),
            contentColor = colorResource(id = R.color.intouch_primary_01),
            elevation = 0.dp
        )
    }

    @Composable
    private fun ToolbarIcon() {
        IconButton(onClick = { onBackPressed() }) {
            Icon(painter = painterResource(id = R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.toolbar_icon_content_description))
        }
    }

    @Composable
    private fun SetPasswordChangeInstruction(modifier: Modifier) {
        Text(
            text = stringResource(R.string.password_instruction),
            modifier = modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontStyle = FontStyle(R.font.poppins),
            color = colorResource(id = R.color.intouch_text)
        )
    }

    @Composable
    private fun ChangePasswordBlock(viewModel: ChangePasswordViewModel, modifier: Modifier) {
        var currentPassword by rememberSaveable { mutableStateOf("") }
        var newPassword by rememberSaveable { mutableStateOf("") }

        var currentPasswordVisibility by rememberSaveable { mutableStateOf(false) }
        var newPasswordVisibility by rememberSaveable { mutableStateOf(false) }

        val currentPasswordValidationState by viewModel.currentPasswordStateLiveData.observeAsState()
        val newPasswordValidationState by viewModel.newPasswordStateLiveData.observeAsState()
        val changePasswordValidationState by viewModel.changePasswordStateLiveData.observeAsState()

        Column {
            PasswordInputField(
                pass = currentPassword,
                passVisibility = currentPasswordVisibility,
                labelText = R.string.current_password,
                placeholderText = R.string.current_pass_placeholder_message,
                isError = isErrorCheck(currentPasswordValidationState, changePasswordValidationState),
                onIconClicked = { currentPasswordVisibility = !currentPasswordVisibility },
                onPassChanged = { currentPassword = it },
                modifier = modifier,
            )

            DisplayValidationText(currentPasswordValidationState, changePasswordValidationState, isCurrentPass = true, modifier)

            Spacer(modifier = modifier.height(8.dp))

            PasswordInputField(
                pass = newPassword,
                passVisibility = newPasswordVisibility,
                labelText = R.string.new_password,
                placeholderText = R.string.new_pass_placeholder_message,
                isError = isErrorCheck(newPasswordValidationState, changePasswordValidationState),
                onIconClicked = { newPasswordVisibility = !newPasswordVisibility },
                onPassChanged = { newPassword = it },
                modifier = modifier,
            )

            DisplayValidationText(newPasswordValidationState, changePasswordValidationState, isCurrentPass = false, modifier)

            Spacer(modifier = modifier.height(8.dp))

            ChangePasswordButton(currentPassword, newPassword, modifier)
        }
    }

    @Composable
    private fun isErrorCheck(passValidationState: ValidationState?, changePassState: ChangePasswordState?): Boolean {
        val validationMessage = handleCurrentPasswordState(passValidationState)
        val isAuthenticateError = isAuthenticateError(changePassState)
        val isSamePassError = isSamePassError(changePassState)

        return validationMessage.isNotBlank() || isAuthenticateError || isSamePassError
    }

    @Composable
    private fun DisplayValidationText(
        passValidationState: ValidationState?,
        changePassState: ChangePasswordState?,
        isCurrentPass: Boolean,
        modifier: Modifier,
    ) {
        val validationMessage =
            if (isCurrentPass) handleCurrentPasswordState(passValidationState) else handleNewPasswordState(passValidationState)
        val isAuthenticateError = isAuthenticateError(changePassState) && isCurrentPass
        val isSamePassError = isSamePassError(changePassState) && !isCurrentPass

        when {
            validationMessage.isNotBlank() -> ValidationText(validationMessage, modifier = modifier)
            isAuthenticateError -> ValidationText(getString(R.string.current_password_doesnt_match), modifier = modifier)
            isSamePassError -> ValidationText(message = getString(R.string.new_password_same_as_current), modifier = modifier)
            else -> {}
        }
    }

    @Composable
    private fun isSamePassError(changePasswordValidationState: ChangePasswordState?) =
        changePasswordValidationState is ChangePasswordFailure && changePasswordValidationState.error == UpdatePasswordErrors.PASSWORDS_ARE_SAME

    @Composable
    private fun isAuthenticateError(changePasswordValidationState: ChangePasswordState?) =
        changePasswordValidationState is ChangePasswordFailure && changePasswordValidationState.error == UpdatePasswordErrors.AUTHENTICATE_FAILURE

    @Composable
    private fun providePassIconColor(passwordVisibility: Boolean) =
        if (passwordVisibility) R.color.intouch_primary_01 else R.color.intouch_neutral_02

    @Composable
    private fun PasswordInputField(
        pass: String,
        passVisibility: Boolean,
        @StringRes labelText: Int,
        @StringRes placeholderText: Int,
        isError: Boolean,
        onIconClicked: () -> Unit,
        onPassChanged: (String) -> Unit,
        modifier: Modifier,
    ) {
        OutlinedTextField(
            value = pass,
            label = { Text(text = stringResource(id = labelText)) },
            singleLine = true,
            placeholder = { Text(text = stringResource(placeholderText)) },
            trailingIcon = { TrailingIcon(onIconClicked, passVisibility) },
            modifier = modifier.padding(8.dp).fillMaxWidth(),
            isError = isError,
            colors = textFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = setPassVisibility(passVisibility),
            onValueChange = onPassChanged
        )
    }

    @Composable
    private fun TrailingIcon(onIconClicked: () -> Unit, passVisibility: Boolean) {
        IconButton(onClick = onIconClicked) {
            Icon(
                painter = painterResource(id = R.drawable.ic_password_visibility),
                tint = colorResource(id = providePassIconColor(passVisibility)),
                contentDescription = stringResource(R.string.password_visibility_content_description)
            )
        }
    }

    @Composable
    private fun setPassVisibility(passVisibility: Boolean) =
        if (passVisibility) VisualTransformation.None else PasswordVisualTransformation()

    @Composable
    private fun ChangePasswordButton(currentPass: String = "", newPass: String = "", modifier: Modifier) {
        Button(
            onClick = { handleChangePasswordButton(currentPass, newPass) },
            modifier = modifier
                .padding(start = 32.dp, top = 28.dp, end = 32.dp)
                .fillMaxWidth(),
            colors = ButtonDefaults.textButtonColors(backgroundColor = colorResource(id = R.color.intouch_accent_active))
        ) {
            Text(
                text = stringResource(id = R.string.change_password).uppercase(),
                color = colorResource(id = R.color.intouch_button_text),
                fontStyle = FontStyle(R.font.poppins_semibold),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
            )
        }
    }

    @Composable
    private fun CancelButton(modifier: Modifier) {
        TextButton(
            onClick = { onBackPressed() },
            modifier = modifier
                .padding(start = 32.dp, end = 32.dp, bottom = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(id = R.string.cancel).uppercase(),
                color = colorResource(id = R.color.intouch_primary_01),
                fontStyle = FontStyle(R.font.poppins_medium),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun ValidationText(message: String, isVisible: Boolean = true, modifier: Modifier) {
        if (isVisible) {
            Text(
                text = message,
                modifier = modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                textAlign = TextAlign.Start,
                fontSize = 10.sp,
                fontStyle = FontStyle(R.font.poppins),
                color = colorResource(id = R.color.intouch_error),
            )
        }
    }

    @Composable
    private fun textFieldColors(): TextFieldColors {
        return TextFieldDefaults.outlinedTextFieldColors(
            textColor = colorResource(id = R.color.intouch_text),
            cursorColor = colorResource(id = R.color.intouch_primary_01),
            placeholderColor = colorResource(id = R.color.intouch_neutral_01),
            //label colors
            focusedLabelColor = colorResource(id = R.color.intouch_primary_01),
            unfocusedLabelColor = colorResource(id = R.color.intouch_neutral_01),
            errorLabelColor = colorResource(id = R.color.intouch_error),
            //border colors
            focusedBorderColor = colorResource(id = R.color.intouch_primary_01),
            unfocusedBorderColor = colorResource(id = R.color.intouch_neutral_01),
            errorBorderColor = colorResource(id = R.color.intouch_error)
        )
    }

    private fun handleChangePasswordButton(currentPass: String = "", newPass: String = "") {
        viewModel.changePassword(currentPass, newPass)
    }

    private fun handleCurrentPasswordState(state: ValidationState?): String {
        return when (state) {
            ValidationState.PASSWORD_IS_BLANK -> getString(R.string.fillout_the_field)
            ValidationState.VALID -> ""
            ValidationState.PASSWORD_IS_SHORT,
            ValidationState.PASSWORD_IS_LONG,
            ValidationState.PASSWORD_WITHOUT_DIGIT,
            ValidationState.PASSWORD_WITHOUT_UPPER_LETTER,
            ValidationState.PASSWORD_WITHOUT_LOWER_LETTER -> getString(R.string.please_provide_a_valid_password)
            else -> ""
        }
    }

    private fun handleNewPasswordState(state: ValidationState?): String {
        return when (state) {
            ValidationState.PASSWORD_IS_BLANK -> getString(R.string.fillout_the_field)
            ValidationState.PASSWORD_IS_SHORT -> getString(R.string.password_too_short)
            ValidationState.PASSWORD_IS_LONG -> getString(R.string.password_too_long)
            ValidationState.PASSWORD_WITHOUT_DIGIT -> getString(R.string.password_digit_validation_message)
            ValidationState.PASSWORD_WITHOUT_UPPER_LETTER -> getString(R.string.password_capital_letter_validation_message)
            ValidationState.PASSWORD_WITHOUT_LOWER_LETTER -> getString(R.string.password_small_letter_validation_message)
            ValidationState.PASSWORDS_ARE_SAME -> getString(R.string.passwords_are_same)
            else -> ""
        }
    }

    private fun handleChangePasswordErrors(changePasswordState: ChangePasswordFailure) {
        when (changePasswordState.error) {
            UpdatePasswordErrors.NO_INTERNET_CONNECTION -> toastUtils.showNetworkErrorToast()
            UpdatePasswordErrors.TOO_MANY_REQUESTS -> toastUtils.showTooManyRequestToast()
            UpdatePasswordErrors.UNKNOWN_ERROR -> toastUtils.showUnknownErrorToast()
            else -> {}
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}