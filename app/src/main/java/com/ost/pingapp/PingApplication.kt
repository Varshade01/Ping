package com.khrd.pingapp

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.*
import androidx.work.Configuration
import com.khrd.pingapp.pushnotification.NotificationUtils
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.lifecycle.LifecycleOwner
import com.khrd.pingapp.utils.viewstate.AppViewState
import com.khrd.pingapp.utils.OnlineManager

@HiltAndroidApp
class PingApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var notificationUtils: NotificationUtils

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var onlineManager: OnlineManager

    @Inject
    lateinit var appViewState: AppViewState

    override fun onCreate() {
        super.onCreate()
        notificationUtils.createChannels()
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        EmojiManager.install(GoogleEmojiProvider())
    }

    val lifecycleObserver: DefaultLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            appViewState.isAppVisible = false
            Log.i("PingApplication", "App stopped")
            onlineManager.stop()
        }

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            appViewState.isAppVisible = true
            Log.i("PingApplication", "App started")
            onlineManager.start()
        }
    }

    override fun getWorkManagerConfiguration() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}