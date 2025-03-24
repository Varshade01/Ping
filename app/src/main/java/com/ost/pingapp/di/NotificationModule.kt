package com.khrd.pingapp.di

import android.app.NotificationManager
import android.content.Context
import com.khrd.pingapp.homescreen.sendping.SendPushUseCase
import com.khrd.pingapp.homescreen.sendping.SendPushUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.pings.received.ConvertToReceivedPingUseCase
import com.khrd.pingapp.pushnotification.*
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class NotificationModule {
    @Provides
    fun providePushNotificationManager(@ApplicationContext context: Context): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Provides
    fun provideNotificationUtils(notificationManager: NotificationManager, @ApplicationContext context: Context): NotificationUtils =
        NotificationUtils(notificationManager, context)

    @Provides
    fun provideSendPushUseCase(sendPushService: SendPushService): SendPushUseCase = SendPushUseCaseImpl(sendPushService)

    @Provides
    fun provideCreatePushNotificationUseCase(
        getUsersByIdUseCase: GetUsersByIdUseCase
    ): CreatePushDataUseCase = CreatePushDataUseCaseImpl(getUsersByIdUseCase)

    @Provides
    fun provideShowPushNotificationUseCase(
        convertPingsUseCase: ConvertToReceivedPingUseCase,
        notificationUtils: NotificationUtils
    ): ShowPushNotificationUseCase =
        ShowPushNotificationUseCaseImpl(convertPingsUseCase, notificationUtils)
}