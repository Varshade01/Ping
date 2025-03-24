package com.khrd.pingapp

import com.khrd.pingapp.firebase.authentication.*
import com.khrd.pingapp.homescreen.HomeScreen
import com.khrd.pingapp.registration.RegistrationActivity
import com.khrd.pingapp.registration.verification.VerificationActivity
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.Assert.assertEquals
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
class SplashScreenActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mockFirebaseAuthAPI: FirebaseAuthAPI

    @Inject
    lateinit var mockToastUtils: ToastUtils

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun `registration activity should be created when current user is null`() {
        // Setup
        val controller = Robolectric.buildActivity(SplashScreenActivity::class.java)
        val activity = controller.get()
        val shadowActivity = shadowOf(activity)
        activity.auth = mockFirebaseAuthAPI
        // Execute
        `when`(mockFirebaseAuthAPI.currentUserId()).thenReturn(null)
        controller.create()
        controller.start()

        // Verify
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            RegistrationActivity::
            class.java.name, startedActivity.component?.className
        )

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `if user exist and reload successful and email is verified`() {
        // Setup
        val controller = Robolectric.buildActivity(SplashScreenActivity::class.java)
        val activity = controller.get()
        val shadowActivity = shadowOf(activity)
        activity.auth = mockFirebaseAuthAPI

        // Execute
        `when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("vx7J42JTJ9cB14JF8HG4PiDEWgU2")
        `when`(mockFirebaseAuthAPI.reload(any())).thenAnswer { invocation ->
            val callback: (ReloadState) -> Unit = invocation.getArgument(0)
            callback(ReloadStateSuccess)
        }
        `when`(mockFirebaseAuthAPI.isEmailVerified()).thenReturn(true)
        controller.create()
        controller.start()

        // Verify
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            HomeScreen::
            class.java.name, startedActivity.component?.className
        )
        // Cleanup
        controller.destroy()
    }

    @Test
    fun `if user exist and reload successful and email isn't verified`() {
        // Setup
        val controller = Robolectric.buildActivity(SplashScreenActivity::class.java)
        val activity = controller.get()
        val shadowActivity = shadowOf(activity)
        activity.auth = mockFirebaseAuthAPI

        // Execute
        `when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("vx7J42JTJ9cB14JF8HG4PiDEWgU2")
        `when`(mockFirebaseAuthAPI.reload(any())).thenAnswer { invocation ->
            val callback: (ReloadState) -> Unit = invocation.getArgument(0)
            callback(ReloadStateSuccess)
        }
        `when`(mockFirebaseAuthAPI.isEmailVerified()).thenReturn(false)
        controller.create()
        controller.start()

        // Verify
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            VerificationActivity::
            class.java.name, startedActivity.component?.className
        )
        // Cleanup
        controller.destroy()
    }

    @Test
    fun `if user exist and reload failed due to network issues`() {
        // Setup
        val controller = Robolectric.buildActivity(SplashScreenActivity::class.java)
        val activity = controller.get()
        val shadowActivity = shadowOf(activity)
        activity.auth = mockFirebaseAuthAPI
        activity.toastUtils = mockToastUtils

        // Execute
        `when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("vx7J42JTJ9cB14JF8HG4PiDEWgU2")
        `when`(mockFirebaseAuthAPI.reload(any())).thenAnswer { invocation ->
            val callback: (ReloadState) -> Unit = invocation.getArgument(0)
            callback(ReloadStateFailure(ReloadError.NetworkFailure))
        }
        `when`(mockFirebaseAuthAPI.isEmailVerified()).thenReturn(true)
        controller.create()
        controller.start()

        // Verify
        verify(mockToastUtils, times(1)).showNetworkErrorToast()
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            HomeScreen::
            class.java.name, startedActivity.component?.className
        )
        // Cleanup
        controller.destroy()
    }

    @Test
    fun `if user exist and reload failed due invalid user`() {
        // Setup
        val controller = Robolectric.buildActivity(SplashScreenActivity::class.java)
        val activity = controller.get()
        val shadowActivity = shadowOf(activity)
        activity.auth = mockFirebaseAuthAPI
        activity.toastUtils = mockToastUtils

        // Execute
        `when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("vx7J42JTJ9cB14JF8HG4PiDEWgU2")
        `when`(mockFirebaseAuthAPI.reload(any())).thenAnswer { invocation ->
            val callback: (ReloadState) -> Unit = invocation.getArgument(0)
            callback(ReloadStateFailure(ReloadError.InvalidUserFailure))
        }
        controller.create()
        controller.start()

        // Verify
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            RegistrationActivity::
            class.java.name, startedActivity.component?.className
        )

        // Cleanup
        controller.destroy()
    }

    @Test
    fun `if user exist and reload failed due unknown error`() {
        // Setup
        val controller = Robolectric.buildActivity(SplashScreenActivity::class.java)
        val activity = controller.get()
        val shadowActivity = shadowOf(activity)
        activity.auth = mockFirebaseAuthAPI
        activity.toastUtils = mockToastUtils

        // Execute
        `when`(mockFirebaseAuthAPI.currentUserId()).thenReturn("vx7J42JTJ9cB14JF8HG4PiDEWgU2")
        `when`(mockFirebaseAuthAPI.reload(any())).thenAnswer { invocation ->
            val callback: (ReloadState) -> Unit = invocation.getArgument(0)
            callback(ReloadStateFailure(ReloadError.UnknownFailure))
        }
        controller.create()
        controller.start()

        // Verify
        verify(mockToastUtils, times(1)).showUnknownErrorToast()
        val startedActivity = shadowActivity.peekNextStartedActivity()
        assertEquals(
            RegistrationActivity::
            class.java.name, startedActivity.component?.className
        )
        // Cleanup
        controller.destroy()
    }
}