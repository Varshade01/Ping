package com.khrd.pingapp

import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khrd.pingapp.resetpassword.ResetPasswordActivity
import com.khrd.pingapp.resetpassword.ResetPasswordState
import com.khrd.pingapp.resetpassword.ResetPasswordUseCase
import com.khrd.pingapp.resetpassword.ResetPasswordViewModel
import com.khrd.pingapp.utils.DialogUtils
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.CountDownLatch
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class ResetPasswordActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    lateinit var factory: ViewModelProvider.Factory

    @Inject
    lateinit var mockResetPasswordUseCase: ResetPasswordUseCase

    @Inject
    lateinit var mockToastUtils: ToastUtils

    @Inject
    lateinit var mockDialogUtils: DialogUtils

    @Before
    fun init() {
        hiltRule.inject()
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ResetPasswordViewModel(mockResetPasswordUseCase) as T
            }
        }
    }

    @Test
    fun `network toast should be displayed if reset password failed with network error`() {
        val controller = Robolectric.buildActivity(ResetPasswordActivity::class.java)
        val latch = CountDownLatch(1)
        val resetPasswordActivity = controller.get()
        val viewModel by resetPasswordActivity.viewModels<ResetPasswordViewModel> { factory }

        Mockito.`when`(mockResetPasswordUseCase.resetPassword(any())).thenReturn(MutableLiveData(ResetPasswordState.NetworkFailure))
        viewModel.resetPasswordStateLiveData.observeForever {
            Assert.assertEquals(ResetPasswordState.NetworkFailure, it)
            latch.countDown()
        }
        controller.create()
        controller.start()
        viewModel.onResetPasswordButtonClick("mock@gmail.com")

        latch.await()
        verify(mockToastUtils).showNetworkErrorToast()
        controller.destroy()
    }

    @Test
    fun `success dialog should be displayed if reset password succeeded`() {
        val controller = Robolectric.buildActivity(ResetPasswordActivity::class.java)
        val latch = CountDownLatch(1)
        val resetPasswordActivity = controller.get()
        val viewModel by resetPasswordActivity.viewModels<ResetPasswordViewModel> { factory }

        Mockito.`when`(mockResetPasswordUseCase.resetPassword(any())).thenReturn(MutableLiveData(ResetPasswordState.ResetPasswordSuccess))
        viewModel.resetPasswordStateLiveData.observeForever {
            Assert.assertEquals(ResetPasswordState.ResetPasswordSuccess, it)
            latch.countDown()
        }
        controller.create()
        controller.start()
        viewModel.onResetPasswordButtonClick("mock@gmail.com")

        latch.await()
        verify(mockDialogUtils).showSuccessDialog(any(), any(), any(), any())
        controller.destroy()
    }

    @Test
    fun `InvalidEmail toast should be displayed if reset password failed with InvalidEmail error`() {
        val controller = Robolectric.buildActivity(ResetPasswordActivity::class.java)
        val latch = CountDownLatch(1)
        val resetPasswordActivity = controller.get()
        val viewModel by resetPasswordActivity.viewModels<ResetPasswordViewModel> { factory }

        Mockito.`when`(mockResetPasswordUseCase.resetPassword(any())).thenReturn(MutableLiveData(ResetPasswordState.InvalidEmail))
        viewModel.resetPasswordStateLiveData.observeForever {
            Assert.assertEquals(ResetPasswordState.InvalidEmail, it)
            latch.countDown()
        }
        controller.create()
        controller.start()
        viewModel.onResetPasswordButtonClick("mock@gmail.com")

        latch.await()
        verify(mockToastUtils).showToast(any())
        controller.destroy()
    }

    @Test
    fun `TooManyRequests toast should be displayed if reset password failed with TooManyRequests error`() {
        val controller = Robolectric.buildActivity(ResetPasswordActivity::class.java)
        val latch = CountDownLatch(1)
        val resetPasswordActivity = controller.get()
        val viewModel by resetPasswordActivity.viewModels<ResetPasswordViewModel> { factory }

        Mockito.`when`(mockResetPasswordUseCase.resetPassword(any())).thenReturn(MutableLiveData(ResetPasswordState.TooManyRequests))
        viewModel.resetPasswordStateLiveData.observeForever {
            Assert.assertEquals(ResetPasswordState.TooManyRequests, it)
            latch.countDown()
        }
        controller.create()
        controller.start()
        viewModel.onResetPasswordButtonClick("mock@gmail.com")

        latch.await()
        verify(mockToastUtils).showTooManyRequestToast()
        controller.destroy()
    }

    @Test
    fun `UnknownFailure toast should be displayed if reset password failed with unknown error`() {
        val controller = Robolectric.buildActivity(ResetPasswordActivity::class.java)
        val latch = CountDownLatch(1)
        val resetPasswordActivity = controller.get()
        val viewModel by resetPasswordActivity.viewModels<ResetPasswordViewModel> { factory }

        Mockito.`when`(mockResetPasswordUseCase.resetPassword(any())).thenReturn(MutableLiveData(ResetPasswordState.UnknownFailure))
        viewModel.resetPasswordStateLiveData.observeForever {
            Assert.assertEquals(ResetPasswordState.UnknownFailure, it)
            latch.countDown()
        }
        controller.create()
        controller.start()
        viewModel.onResetPasswordButtonClick("mock@gmail.com")

        latch.await()
        verify(mockToastUtils).showUnknownErrorToast()
        controller.destroy()
    }
}