package com.khrd.pingapp.di

import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.GetGroupsByIdsUseCase
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases.*
import com.khrd.pingapp.homescreen.sendping.GetGroupMembersUseCase
import com.khrd.pingapp.homescreen.sendping.GetUserGroupsUseCase
import com.khrd.pingapp.homescreen.sendping.SendPushUseCase
import com.khrd.pingapp.homescreen.usecases.GetGroupUsersUseCase
import com.khrd.pingapp.homescreen.usecases.GetGroupUsersUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.pings.*
import com.khrd.pingapp.homescreen.usecases.pings.received.ConvertToReceivedPingUseCase
import com.khrd.pingapp.homescreen.usecases.pings.received.ConvertToReceivedPingUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.pings.received.LoadReceivedPingsUseCase
import com.khrd.pingapp.homescreen.usecases.pings.received.LoadReceivedPingsUseCaseImpl
import com.khrd.pingapp.pushnotification.CreatePushDataUseCase
import com.khrd.pingapp.repository.pings.PingsRepository
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCase
import com.khrd.pingapp.utils.OnlineHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
class PingsModule {
    @Provides
    fun provideCreatePingUseCase(
        repository: PingsRepository,
        authAPI: FirebaseAuthAPI,
        connectionStatus: ConnectionStatus,
        createPushDataUseCase: CreatePushDataUseCase,
        sendPushUseCase: SendPushUseCase,
        @MainCoroutineScope mainCoroutineScope: CoroutineScope
    ): CreatePingUseCase =
        CreatePingUseCaseImpl(repository, authAPI, connectionStatus, createPushDataUseCase, sendPushUseCase, mainCoroutineScope)


    @Provides
    fun provideRewriteScheduledPingsUseCase(
        createPingUseCase: CreatePingUseCase,
        deletePingsUseCase: DeleteScheduledPingsUseCase
    ): RewriteScheduledPingsUseCase =
        RewriteScheduledPingsUseCaseImpl(createPingUseCase, deletePingsUseCase)

    @Provides
    fun provideRescheduleExpiredRecurringPingsUseCase(
        createPingUseCase: CreatePingUseCase,
        deletePingsUseCase: DeleteScheduledPingsUseCase,
    ):RescheduleExpiredRecurringPingsUseCase =
        RescheduleExpiredRecurringPingsUseCaseImpl(createPingUseCase, deletePingsUseCase)

    @Provides
    fun provideDeleteScheduledPingsUseCase(repository: PingsRepository): DeleteScheduledPingsUseCase =
        DeleteScheduledPingsUseCaseImpl(repository)

    @Provides
    fun provideSendScheduledPingsAfterRebootUseCase(
        rewriteScheduledPingsUseCase: RewriteScheduledPingsUseCase
    ): SendScheduledPingsAfterRebootUseCase =
        SendScheduledPingsAfterRebootUseCaseImpl(rewriteScheduledPingsUseCase)

    @Provides
    fun provideLoadReceivedPingsUseCase(
        firebaseAuth: FirebaseAuthAPI,
        pingsRepository: PingsRepository
    ): LoadReceivedPingsUseCase = LoadReceivedPingsUseCaseImpl(firebaseAuth, pingsRepository)

    @Provides
    fun provideConvertToReceivedPingUseCase(
        firebaseAuth: FirebaseAuthAPI,
        getUsersByIdUseCase: GetUsersByIdUseCase,
        getGroupUseCase: GetGroupUseCase,
        @IoCoroutineScope coroutineScope: CoroutineScope,
        onlineHelper: OnlineHelper,
        usersRepository: UsersRepository
    ): ConvertToReceivedPingUseCase =
        ConvertToReceivedPingUseCaseImpl(firebaseAuth, getUsersByIdUseCase, getGroupUseCase, coroutineScope, onlineHelper, usersRepository)

    @Provides
    fun provideGetSentPingsUseCase(
        pingsRepository: PingsRepository,
        firebaseAuth: FirebaseAuthAPI
    ): GetSentPingsUseCase = GetSentPingsUseCaseImpl(pingsRepository, firebaseAuth)

    @Provides
    fun provideGetScheduledPingsUseCase(
        pingsRepository: PingsRepository,
        firebaseAuth: FirebaseAuthAPI
    ): GetScheduledPingsUseCase = GetScheduledPingsUseCaseImpl(pingsRepository, firebaseAuth)

    @Provides
    fun provideGetPingUserDataUseCase(
        getUserGroupsUseCase: GetUserGroupsUseCase,
        getGroupMembersUseCase: GetGroupMembersUseCase,
        getGroupUseCase: GetGroupUseCase,
        @IoCoroutineScope ioCoroutineScope: CoroutineScope
    ): GetGroupUsersUseCase = GetGroupUsersUseCaseImpl(getGroupMembersUseCase, getUserGroupsUseCase, getGroupUseCase, ioCoroutineScope)

    @Provides
    fun provideConvertToSentPingsUseCase(
        getUsersByIdUseCase: GetUsersByIdUseCase,
        getGroupsByIdsUseCase: GetGroupsByIdsUseCase,
        firebaseAuth: FirebaseAuthAPI,
        getGroupUseCase: GetGroupUseCase,
        @IoCoroutineScope ioCoroutineScope: CoroutineScope,
        onlineHelper: OnlineHelper,
        usersRepository: UsersRepository
    ): ConvertToSentPingItemUseCase =
        ConvertToSentPingItemUseCaseImpl(
            getUsersByIdUseCase,
            getGroupsByIdsUseCase,
            firebaseAuth,
            getGroupUseCase,
            ioCoroutineScope,
            onlineHelper,
            usersRepository
        )

    @Provides
    fun provideChangePingSeenStatus(
        pingsRepository: PingsRepository
    ): ChangePingSeenStatusUseCase = ChangePingSeenStatusUseCaseImpl(pingsRepository)

    @Provides
    fun provideConvertToReceiverStatusItemsUseCase(
        getUsersByIdUseCase: GetUsersByIdUseCase,
        onlineHelper: OnlineHelper
    ): ConvertToReceiverStatusItemsUseCase = ConvertToReceiverStatusItemsUseCaseImpl(getUsersByIdUseCase, onlineHelper)

    @Provides
    fun provideClearReceivedPingsCacheUseCase(
        pingsRepository: PingsRepository
    ): ClearReceivedPingsCacheUseCase = ClearPingsCacheUseCaseImpl(pingsRepository)

    @Provides
    fun provideSubscribeToUserChangesUseCase(usersRepository: UsersRepository): SubscribeToUserChangesUseCase =
        SubscribeToUserChangesUseCaseImpl(usersRepository)
}