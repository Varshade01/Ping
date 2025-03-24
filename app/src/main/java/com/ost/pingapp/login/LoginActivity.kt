package com.khrd.pingapp.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
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
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.databinding.ActivityLoginBinding
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.login.LoginState.*
import com.khrd.pingapp.registration.RegistrationActivity
import com.khrd.pingapp.registration.verification.VerificationActivity
import com.khrd.pingapp.resetpassword.ResetPasswordActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : SocialAccountActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    private lateinit var auth: FirebaseAuth
    private lateinit var facebookCallbackManager: CallbackManager

    private var showOneTapUI = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        binding = ActivityLoginBinding.inflate(layoutInflater)
        initFBCallbackManager()
        initListeners()
        initObservers()
        setContentView(binding.root)
    }

    private fun initObservers() {
        observeLoginState()
    }

    private fun initListeners() {
        handleLoginBtnClick()
        handleForgotPassBtnClick()
        handleRegisterNewAccBtnClick()
        handleTextChangeListeners()
        handleLoginViaGoogle()
        handleLoginViaFacebook()
    }

    private fun handleTextChangeListeners() {
        binding.tieLoginEmail.addTextChangedListener { if (!it.isNullOrBlank()) binding.tilLoginEmail.error = "" }
        binding.tieLoginPassword.addTextChangedListener { if (!it.isNullOrBlank()) binding.tilLoginPassword.error = "" }
    }

    private fun handleRegisterNewAccBtnClick() {
        binding.registerNewAccBtn.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun handleForgotPassBtnClick() {
        binding.forgotPasswordBtn.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun handleLoginBtnClick() {
        binding.loginBtn.setOnClickListener {
            val email = binding.tieLoginEmail.text.toString().trim()
            val password = binding.tieLoginPassword.text.toString().trim()
            viewModel.login(email, password)
        }
    }

    private fun observeLoginState() {
        viewModel.loginStateLiveData.observe(this) { state ->
            handleLoginStates(state)
        }
    }

    private fun handleLoginStates(state: LoginState?) {
        when (state) {
            LoginSuccess -> handleSuccessLoginState()
            EmailNotVerified -> handleEmailNotVerifiedState()
            InvalidCredentials -> toastUtils.showInvalidCredentialsToast()
            NetworkFailure -> toastUtils.showNetworkErrorToast()
            TooManyRequests -> toastUtils.showTooManyRequestToast()
            UnknownFailure -> toastUtils.showUnknownErrorToast()
            EmptyEmail -> setEmptyEmailMessage()
            InvalidEmail -> setInvalidEmailMessage()
            EmptyPassword -> setEmptyPasswordMessage()
        }
    }

    private fun setInvalidEmailMessage() {
        binding.tilLoginEmail.error = getString(R.string.invalid_email)
    }

    private fun setEmptyEmailMessage() {
        binding.tilLoginEmail.error = getString(R.string.empty_field)
    }

    private fun setEmptyPasswordMessage() {
        binding.tilLoginPassword.error = getString(R.string.empty_field)
    }

    private fun handleEmailNotVerifiedState() {
        val intent = Intent(this, VerificationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(Constants.GROUP_ID_FROM_DEEP_LINKING, this.intent.getStringExtra(Constants.GROUP_ID_FROM_DEEP_LINKING))
        startActivity(intent)
    }

    private fun handleSuccessLoginState() {
        val intent = Intent(this, HomeScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(Constants.GROUP_ID_FROM_DEEP_LINKING, this.intent.getStringExtra(Constants.GROUP_ID_FROM_DEEP_LINKING))
        startActivity(intent)
    }

    private fun handleLoginViaGoogle() {
        binding.googleRegistration.setOnClickListener {
            loginViaGoogle()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("***888", "facebook:onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ONE_TAP -> handleGoogleSignIn(data)
            else -> handleFacebookSignIn(requestCode, resultCode, data) // Pass the activity result back to the Facebook SDK
        }
    }

    private fun handleFacebookSignIn(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("***888", "facebook:handleFacebookSignIn")
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleGoogleSignIn(data: Intent?) {
        try {
            val credential = googleAuth.oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            idToken?.let { signInWithCredentials(it) } ?: toastUtils.showFailedToSignInViaGoogleToast()
        } catch (exception: ApiException) {
            handleApiException(exception)
        }
    }

    private fun signInWithCredentials(idToken: String?) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    handleSuccessLoginState()
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
                    Log.d("***888", "facebook:success")
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
                handleSuccessLoginState()
            } else if (isAccountExists(task)) {
                toastUtils.showToast(R.string.user_with_this_email_already_exists)
            } else {
                Log.i("***888", "signInWithCredential:failure", task.exception)
                toastUtils.showToast("Authentication failed.")
            }
        }
    }

    private fun isAccountExists(task: Task<AuthResult>): Boolean {
        return (!task.isSuccessful && (task.exception as FirebaseAuthUserCollisionException).errorCode == RegistrationActivity.ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL)
    }

    companion object {
        const val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
        const val ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL = "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"
    }

}