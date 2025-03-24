package com.khrd.pingapp

import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.login.LoginActivity
import com.khrd.pingapp.registration.verification.VerificationActivity
import com.khrd.pingapp.registration.verification.VerificationViewModel
import com.khrd.pingapp.registration.verification.isEmailVerified.IsEmailVerifiedUseCase
import com.khrd.pingapp.registration.verification.logout.LogoutNetworkFailure
import com.khrd.pingapp.registration.verification.logout.LogoutSuccess
import com.khrd.pingapp.registration.verification.logout.LogoutUnknownFailure
import com.khrd.pingapp.registration.verification.logout.LogoutUseCase
import com.khrd.pingapp.registration.verification.sendEmail.*
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class VerificationActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockIsEmailVerifiedUseCase: IsEmailVerifiedUseCase

    @Inject
    lateinit var mockToastUtils: ToastUtils

    @Inject
    lateinit var mockLogoutUseCase: LogoutUseCase

    @Inject
    lateinit var mockSendConfirmationEmailUseCase: SendConfirmationEmailUseCase

    lateinit var factory: ViewModelProvider.Factory

    @Before
    fun init() {
        hiltRule.inject()
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return VerificationViewModel(mockIsEmailVerifiedUseCase, mockLogoutUseCase, mockSendConfirmationEmailUseCase) as T
            }
        }
    }

    @Test
    fun `login activity should be created when logout is successful`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }
        val shadowActivity = shadowOf(verificationActivity)

        Mockito.`when`(mockLogoutUseCase.logout()).thenReturn(MutableLiveData(LogoutSuccess()))
        viewModel.logout()

        controller.create()
        controller.start()
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            LoginActivity::
            class.java.name, startedActivity.component?.className
        )

        controller.destroy()
    }

    @Test
    fun `network toast should be displayed when logout is failed with network failure`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }

        Mockito.`when`(mockLogoutUseCase.logout()).thenReturn(MutableLiveData(LogoutNetworkFailure()))
        viewModel.logout()

        controller.create()
        controller.start()

        verify(mockToastUtils, times(1)).showNetworkErrorToast()

        controller.destroy()
    }

    @Test
    fun `unknown toast should be displayed when logout is failed with unknown failure`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }

        Mockito.`when`(mockLogoutUseCase.logout()).thenReturn(MutableLiveData(LogoutUnknownFailure()))
        viewModel.logout()

        controller.create()
        controller.start()

        verify(mockToastUtils, times(1)).showUnknownErrorToast()

        controller.destroy()
    }

    @Test
    fun `toast should be displayed when sending email is successful`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }

        Mockito.`when`(mockSendConfirmationEmailUseCase.sendConfirmationEmail()).thenReturn(MutableLiveData(SendConfirmationEmailSuccess()))
        viewModel.sendConfirmationEmail()

        controller.create()
        controller.start()

        verify(mockToastUtils, times(1)).showToast(any())

        controller.destroy()
    }

    @Test
    fun `network toast should be displayed when sending email is failed with network failure`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }

        Mockito.`when`(mockSendConfirmationEmailUseCase.sendConfirmationEmail()).thenReturn(MutableLiveData(SendConfirmationEmailNetworkFailure()))
        viewModel.sendConfirmationEmail()

        controller.create()
        controller.start()

        verify(mockToastUtils, times(1)).showNetworkErrorToast()

        controller.destroy()
    }

    @Test
    fun `too  many requests toast should be displayed when sending email is failed due to too many requests`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }

        Mockito.`when`(mockSendConfirmationEmailUseCase.sendConfirmationEmail()).thenReturn(MutableLiveData(SendConfirmationEmailTooManyRequests()))
        viewModel.sendConfirmationEmail()

        controller.create()
        controller.start()

        verify(mockToastUtils, times(1)).showTooManyRequestToast()

        controller.destroy()
    }

    @Test
    fun `unknown toast should be displayed when sending email is failed with unknown failure`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }

        Mockito.`when`(mockSendConfirmationEmailUseCase.sendConfirmationEmail()).thenReturn(MutableLiveData(SendConfirmationEmailUnknownFailure()))
        viewModel.sendConfirmationEmail()

        controller.create()
        controller.start()

        verify(mockToastUtils, times(1)).showUnknownErrorToast()

        controller.destroy()
    }

    @Test
    fun `home screen should be created when verification is successful`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }
        val shadowActivity = shadowOf(verificationActivity)

        Mockito.`when`(mockIsEmailVerifiedUseCase.isEmailVerified()).thenReturn(MutableLiveData(true))
        viewModel.login()

        controller.create()
        controller.start()
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            HomeScreen::
            class.java.name, startedActivity.component?.className
        )

        controller.destroy()
    }

    @Test
    fun `toast should be displayed when verification is failed`() {
        val controller = Robolectric.buildActivity(VerificationActivity::class.java)
        val verificationActivity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by verificationActivity.viewModels<VerificationViewModel> { factory }

        Mockito.`when`(mockIsEmailVerifiedUseCase.isEmailVerified()).thenReturn(MutableLiveData(false))
        viewModel.login()

        controller.create()
        controller.start()

        verify(mockToastUtils, times(1)).showToast(any())

        controller.destroy()
    }
}