package com.khrd.pingapp

import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.login.LoginActivity
import com.khrd.pingapp.login.LoginState
import com.khrd.pingapp.login.LoginUseCase
import com.khrd.pingapp.login.LoginViewModel
import com.khrd.pingapp.registration.RegistrationActivity
import com.khrd.pingapp.registration.verification.VerificationActivity
import com.khrd.pingapp.resetpassword.ResetPasswordActivity
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class LoginActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockLoginUseCase: LoginUseCase

    @Inject
    lateinit var mockToastUtils: ToastUtils

    lateinit var factory: ViewModelProvider.Factory

    @Before
    fun before() {
        hiltRule.inject()

        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return LoginViewModel(mockLoginUseCase) as T
            }
        }
    }

    @Test
    fun `activity should not be null`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }

        // Execute
        controller.create()
        controller.start()

        // Verify
        assertNotNull(activity)

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `click on Forgot Password should launch ResetPasswordActivity`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val shadowActivity = shadowOf(activity)

        // Execute
        controller.create()
        controller.start()
        activity.findViewById<Button>(R.id.forgot_password_btn).callOnClick()

        // Verify
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(ResetPasswordActivity::class.java.name, startedActivity.component?.className)

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `click on Register Account should launch RegistrationActivity`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val shadowActivity = shadowOf(activity)

        // Execute
        controller.create()
        controller.start()
        activity.findViewById<Button>(R.id.register_new_acc_btn).callOnClick()

        // Verify
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(RegistrationActivity::class.java.name, startedActivity.component?.className)

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `errors in text fields should disappear on entering text`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        controller.create()

        val tilEmail = activity.findViewById<TextInputLayout>(R.id.til_login_email)
        val tilPassword = activity.findViewById<TextInputLayout>(R.id.til_login_password)
        val tieEmail = activity.findViewById<TextInputEditText>(R.id.tie_login_email)
        val tiePassword = activity.findViewById<TextInputEditText>(R.id.tie_login_password)

        tilEmail.error = "error"
        tilPassword.error = "error"

        // Execute
        controller.start()

        tieEmail.setText("text")
        tiePassword.setText("text")

        // Verify
        assertTrue(tilEmail.error.isNullOrBlank())
        assertTrue(tilPassword.error.isNullOrBlank())

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `HomeScreenActivity should be launched on successful login`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val shadowActivity = shadowOf(activity)
        val viewModel by activity.viewModels<LoginViewModel> { factory }

        // Execute
        `when`(mockLoginUseCase.login("email", "password")).thenReturn(MutableLiveData(LoginState.LoginSuccess))

        viewModel.login("email", "password")

        controller.create()
        controller.start()

        // Verify
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(HomeScreen::class.java.name, startedActivity.component?.className)

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `VerificationActivity should be launched when email is not verified`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val shadowActivity = shadowOf(activity)
        val viewModel by activity.viewModels<LoginViewModel> { factory }

        // Execute
        `when`(mockLoginUseCase.login("email", "password")).thenReturn(MutableLiveData(LoginState.EmailNotVerified))

        viewModel.login("email", "password")

        controller.create()
        controller.start()

        // Verify
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(VerificationActivity::class.java.name, startedActivity.component?.className)

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `invalid credentials toast should be displayed when login failed because of invalid credentials`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<LoginViewModel> { factory }

        // Execute
        `when`(mockLoginUseCase.login("email", "password")).thenReturn(MutableLiveData(LoginState.InvalidCredentials))

        viewModel.login("email", "password")

        controller.create()
        controller.start()

        // Verify
        verify(mockToastUtils, times(1)).showToast(any())

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `network error toast should be displayed when login failed with network error`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<LoginViewModel> { factory }

        // Execute
        `when`(mockLoginUseCase.login("email", "password")).thenReturn(MutableLiveData(LoginState.NetworkFailure))

        viewModel.login("email", "password")

        controller.create()
        controller.start()

        // Verify
        verify(mockToastUtils, times(1)).showNetworkErrorToast()

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `too many requests toast should be displayed when too many login requests were made`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<LoginViewModel> { factory }

        // Execute
        `when`(mockLoginUseCase.login("email", "password")).thenReturn(MutableLiveData(LoginState.TooManyRequests))

        viewModel.login("email", "password")

        controller.create()
        controller.start()

        // Verify
        verify(mockToastUtils, times(1)).showTooManyRequestToast()

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `unknown error toast should be displayed when login failed due to unknown error`() {
        // Setup
        val controller = Robolectric.buildActivity(LoginActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<LoginViewModel> { factory }

        // Execute
        `when`(mockLoginUseCase.login("email", "password")).thenReturn(MutableLiveData(LoginState.UnknownFailure))

        viewModel.login("email", "password")

        controller.create()
        controller.start()

        // Verify
        verify(mockToastUtils, times(1)).showUnknownErrorToast()

        // Cleanup
        controller.destroy()
    }
}