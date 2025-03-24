package com.khrd.pingapp.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.StringRes
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.khrd.pingapp.R
import com.khrd.pingapp.SocialAccountActivity
import com.khrd.pingapp.constants.Constants.GROUP_ID_FROM_DEEP_LINKING
import com.khrd.pingapp.databinding.ActivityRegistrationBinding
import com.khrd.pingapp.login.LoginActivity
import com.khrd.pingapp.registration.verification.VerificationActivity
import com.khrd.pingapp.utils.passwordValidation.ValidationState.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationActivity : SocialAccountActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private val viewModel by viewModels<RegistrationViewModel>()

    private lateinit var auth: FirebaseAuth
    private lateinit var facebookCallbackManager: CallbackManager

    private var showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        initFBCallbackManager()
        initListeners()
        initObservers()
        setContentView(binding.root)
    }

    private fun initListeners() {
        initRegisterBtnClick()
        initAlreadyRegisterBtnClick()
        handleLoginViaGoogle()
        handleLoginViaFacebook()
    }

    private fun initRegisterBtnClick() {
        binding.buttonRegister.setOnClickListener {
            val name = binding.personName.editText?.text.toString().trim()
            val email = binding.emailAddress.editText?.text.toString().trim()
            val password = binding.password.editText?.text.toString().trim()
            val consent = binding.checkBox.isChecked
            viewModel.registerUser(RegistrationFields(name, email, password, consent))
        }
    }

    private fun initAlreadyRegisterBtnClick() {
        binding.buttonAlreadyRegistered.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(GROUP_ID_FROM_DEEP_LINKING, this.intent.getStringExtra(GROUP_ID_FROM_DEEP_LINKING))
            startActivity(intent)
        }
    }

    private fun initObservers() {
        observeRegistrationState()
    }

    private fun observeRegistrationState() {
        viewModel.registrationStateLiveData.observe(this) { it ->
            handleRegistrationState(it)
        }
    }

    private fun handleRegistrationState(it: IRegistrationState?) {
        resetErrorFields()
        when (it) {
            is RegistrationSuccess -> handleRegistrationSuccessState()
            is RegistrationUnknownFailure -> toastUtils.showToast(R.string.registration_unknown_issue)
            is RegistrationUserExistFailure -> toastUtils.showToast(R.string.user_already_registered)
            is RegistrationValidationFailure -> handleValidationFailure(it)
            is RegistrationEmailAlreadyInUseFailure -> toastUtils.showToast(R.string.email_already_in_use)
            is RegistrationNetworkFailure -> toastUtils.showNetworkErrorToast()
            null -> {}//do nothing
        }
    }

    private fun handleRegistrationSuccessState() {
        toastUtils.showToast(R.string.successfully_registered)
        val intent = Intent(this, VerificationActivity::class.java)
        intent.putExtra(GROUP_ID_FROM_DEEP_LINKING, this.intent.getStringExtra(GROUP_ID_FROM_DEEP_LINKING))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun handleValidationFailure(it: RegistrationValidationFailure) {
        it.errors.forEach { validation ->
            when (validation) {
                VALID -> {}//do nothing
                NAME_IS_BLANK -> setPersonNameError(R.string.enter_name)
                NAME_LETTERS_FAILURE -> setPersonNameError(R.string.name_validation_message)
                NAME_LENGTH_FAILURE -> setPersonNameError(R.string.name_length_error_message)
                EMAIL_IS_BLANK -> setEmailAddressError(R.string.enter_email)
                EMAIL_IS_INVALID -> setEmailAddressError(R.string.email_validation_message)
                PASSWORD_IS_BLANK -> setPasswordError(R.string.enter_password)
                PASSWORD_IS_SHORT -> setPasswordError(R.string.password_too_short)
                PASSWORD_WITHOUT_DIGIT -> setPasswordError(R.string.password_digit_validation_message)
                PASSWORD_WITHOUT_UPPER_LETTER -> setPasswordError(R.string.password_capital_letter_validation_message)
                PASSWORD_WITHOUT_LOWER_LETTER -> setPasswordError(R.string.password_small_letter_validation_message)
                PASSWORD_IS_LONG -> setPasswordError(R.string.password_too_long)
                CONSENT_NOT_GIVEN -> {
                    binding.consentError.visibility = TextView.VISIBLE
                    binding.scrollView.scrollTo(0, binding.consentError.bottom)
                }
            }
        }
    }

    private fun setPersonNameError(@StringRes error: Int) {
        binding.personName.error = getString(error)
    }

    private fun setEmailAddressError(@StringRes error: Int) {
        binding.emailAddress.error = getString(error)
    }

    private fun setPasswordError(@StringRes error: Int) {
        binding.password.error = getString(error)
    }

    private fun resetErrorFields() {
        binding.personName.isErrorEnabled = false
        binding.emailAddress.isErrorEnabled = false
        binding.password.isErrorEnabled = false
        binding.consentError.visibility = TextView.INVISIBLE
    }

    private fun handleLoginViaGoogle() {
        binding.googleRegistration.setOnClickListener {
            loginViaGoogle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ONE_TAP -> handleGoogleSignIn(data)
            else -> handleFacebookSignIn(requestCode, resultCode, data) // Pass the activity result back to the Facebook SDK
        }
    }

    private fun handleFacebookSignIn(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleGoogleSignIn(data: Intent?) {
        try {
            val credential = googleAuth.oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            idToken?.let { signInWithGoogleCredentials(it) } ?: toastUtils.showFailedToSignInViaGoogleToast()
        } catch (exception: ApiException) {
            handleApiException(exception)
        }
    }

    private fun signInWithGoogleCredentials(idToken: String?) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    createUserViaSocialAccount(auth.currentUser)
                } else {
                    task.exception?.message?.let { toastUtils.showToast(it) }
                }
            }
    }


    private fun handleApiException(exception: ApiException) {
        when (exception.statusCode) {
            CommonStatusCodes.CANCELED -> showOneTapUI = false
            CommonStatusCodes.NETWORK_ERROR -> exception.statusMessage?.let { toastUtils.showToast(it) }
            else -> exception.statusMessage?.let { toastUtils.showToast(it) }
        }
    }


    private fun createUserViaSocialAccount(user: FirebaseUser?) {
        val email = user?.email ?: ""
        val name = user?.displayName ?: ""
        viewModel.createUserViaSocialAccount(email, name)
    }

    private fun handleLoginViaFacebook() {
        binding.facebookRegistration.setOnClickListener {
//            toastUtils.showToast("Feature under construction")
            /**
             * Temporary commented as successful login or registration possible only when facebook dev account is in "Live" mode.
             * This mode requires app uploading to the Play Market.
             * There is only "App Development" mode available,
             * which doesn't allow use user account without manual registration one as tester in dev account.
             */
            val loginM = LoginManager.getInstance()
            loginM.registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Log.i("***888", "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.i("***888", "facebook:onError", error)
                }
            })
            loginM.logInWithReadPermissions(this, facebookCallbackManager, listOf("public_profile", "email"))
        }
    }

    private fun initFBCallbackManager() {
        facebookCallbackManager = CallbackManager.Factory.create()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d("***888", "signInWithCredential:success")
                createUserViaSocialAccount(auth.currentUser)
            } else if (isAccountExists(task)) {
                toastUtils.showToast(R.string.user_with_this_email_already_exists)
            } else {
                Log.i("***888", "signInWithCredential:failure", task.exception)
                toastUtils.showToast("Authentication failed.")
            }
        }
    }

    private fun isAccountExists(task: Task<AuthResult>): Boolean {
        return (!task.isSuccessful && (task.exception as FirebaseAuthUserCollisionException).errorCode == ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL)
    }

    companion object {
        const val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
        const val ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL = "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"
    }
}
