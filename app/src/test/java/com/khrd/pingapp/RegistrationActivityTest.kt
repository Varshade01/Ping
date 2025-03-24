package com.khrd.pingapp

import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.registration.*
import com.khrd.pingapp.registration.verification.VerificationActivity
import com.khrd.pingapp.useradministration.usecases.CreateUserUseCase
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
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
import org.robolectric.Shadows
import javax.inject.Inject


@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
class RegistrationActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockFirebaseAuthAPI: FirebaseAuthAPI

    var mockRegistrationUseCase: RegistrationUseCase = Mockito.mock(RegistrationUseCase::class.java)

    @Inject
    lateinit var mockCreateUserUseCase: CreateUserUseCase

    @Inject
    lateinit var mockToastUtils: ToastUtils

    lateinit var factory: ViewModelProvider.Factory

    @Before
    fun init() {
        hiltRule.inject()
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return RegistrationViewModel(mockRegistrationUseCase, mockCreateUserUseCase, mockFirebaseAuthAPI) as T
            }
        }
    }

    @Test
    fun `verification activity should be created when user is successfully registered`() {
        val controller = Robolectric.buildActivity(RegistrationActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<RegistrationViewModel> { factory }
        val shadowActivity = Shadows.shadowOf(activity)

        val fields = RegistrationFields("Name", "example@mail.com", "Password1", true)

        Mockito.`when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("some_id")
        Mockito.`when`(mockRegistrationUseCase.registerUser(fields)).thenReturn(MutableLiveData(RegistrationSuccess()))
        Mockito.`when`(mockCreateUserUseCase.createUser(mockFirebaseAuthAPI.currentUserId()!!, fields.email, fields.name))
            .thenReturn(MutableLiveData(RegistrationSuccess()))

        viewModel.registerUser(fields)

        controller.create()
        controller.start()
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            VerificationActivity::
            class.java.name, startedActivity.component?.className
        )

        controller.destroy()
    }

    @Test
    fun `network error toast should be displayed when registration is failed with network error`() {
        val controller = Robolectric.buildActivity(RegistrationActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<RegistrationViewModel> { factory }

        val fields = RegistrationFields("Name", "example@mail.com", "Password1", true)

        Mockito.`when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("some_id")
        Mockito.`when`(mockRegistrationUseCase.registerUser(fields)).thenReturn(MutableLiveData(RegistrationNetworkFailure()))

        viewModel.registerUser(fields)

        controller.create()
        controller.start()
        verify(mockToastUtils, times(1)).showNetworkErrorToast()

        controller.destroy()
    }

    @Test
    fun `toast should be displayed when registration is failed with unknown error`() {
        val controller = Robolectric.buildActivity(RegistrationActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<RegistrationViewModel> { factory }

        val fields = RegistrationFields("Name", "example@mail.com", "Password1", true)

        Mockito.`when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("some_id")
        Mockito.`when`(mockRegistrationUseCase.registerUser(fields)).thenReturn(MutableLiveData(RegistrationUnknownFailure()))

        viewModel.registerUser(fields)

        controller.create()
        controller.start()
        verify(mockToastUtils, times(1)).showToast(any())

        controller.destroy()
    }

    @Test
    fun `toast should be displayed when user is already registered`() {
        val controller = Robolectric.buildActivity(RegistrationActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<RegistrationViewModel> { factory }

        val fields = RegistrationFields("Name", "example@mail.com", "Password1", true)

        Mockito.`when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("some_id")
        Mockito.`when`(mockRegistrationUseCase.registerUser(fields)).thenReturn(MutableLiveData(RegistrationUserExistFailure()))

        viewModel.registerUser(fields)

        controller.create()
        controller.start()
        verify(mockToastUtils, times(1)).showToast(any())

        controller.destroy()
    }

    @Test
    fun `toast should be displayed when email is already in use`() {
        val controller = Robolectric.buildActivity(RegistrationActivity::class.java)
        val activity = controller.get().apply {
            toastUtils = mockToastUtils
        }
        val viewModel by activity.viewModels<RegistrationViewModel> { factory }

        val fields = RegistrationFields("Name", "example@mail.com", "Password1", true)

        Mockito.`when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("some_id")
        Mockito.`when`(mockRegistrationUseCase.registerUser(fields)).thenReturn(MutableLiveData(RegistrationEmailAlreadyInUseFailure()))

        viewModel.registerUser(fields)

        controller.create()
        controller.start()
        verify(mockToastUtils, times(1)).showToast(any())

        controller.destroy()
    }
}