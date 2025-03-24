package com.khrd.pingapp.di

import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.firebase.FirebaseDynamicLinkAPI
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.usecases.*
import com.khrd.pingapp.groupmanagement.usecases.createGroup.CreateGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.createGroup.CreateGroupUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.createGroup.GenerateLinkUseCase
import com.khrd.pingapp.groupmanagement.usecases.createGroup.GenerateLinkUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.joinGroup.GetGroupByLinkUseCase
import com.khrd.pingapp.groupmanagement.usecases.joinGroup.GetGroupByLinkUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.joinGroup.JoinGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.joinGroup.JoinGroupUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.leaveGroup.LeaveGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.leaveGroup.LeaveGroupUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.RenameGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.RenameGroupUseCaseImpl
import com.khrd.pingapp.homescreen.changeEmail.usecases.ChangeEmailUseCase
import com.khrd.pingapp.homescreen.changeEmail.usecases.ChangeEmailUseCaseImpl
import com.khrd.pingapp.homescreen.sendping.*
import com.khrd.pingapp.homescreen.usecases.GetGroupUsersUseCase
import com.khrd.pingapp.homescreen.usecases.updategroupimage.UpdateGroupImageUseCase
import com.khrd.pingapp.homescreen.usecases.updategroupimage.UpdateGroupImageUseCaseImpl
import com.khrd.pingapp.repository.groups.GroupRepository
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.utils.HomeScreenViewState
import com.khrd.pingapp.utils.HomeScreenViewStateImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GroupModule {
    @Provides
    fun provideCreateGroupUseCase(
        groupRepository: GroupRepository,
        usersRepository: UsersRepository,
        firebaseConnectionStatus: ConnectionStatus
    ): CreateGroupUseCase = CreateGroupUseCaseImpl(groupRepository, usersRepository, firebaseConnectionStatus)

    @Provides
    fun provideGenerateLinkUseCase(firebaseDynamicLink: FirebaseDynamicLinkAPI, groupRepository: GroupRepository): GenerateLinkUseCase =
        GenerateLinkUseCaseImpl(firebaseDynamicLink, groupRepository)

    @Provides
    fun provideRenameGroupUseCase(groupRepository: GroupRepository, firebaseConnectionStatus: ConnectionStatus): RenameGroupUseCase =
        RenameGroupUseCaseImpl(groupRepository, firebaseConnectionStatus)

    @Provides
    fun provideGroupNameValidationUseCase(): GroupNameValidationUseCase = GroupNameValidationUseCaseImpl()

    @Provides
    fun provideLeaveGroupUseCase(
        usersRepository: UsersRepository,
        groupRepository: GroupRepository,
        getGroupUsersUseCase: GetGroupUsersUseCase,
        firebaseConnectionStatus: ConnectionStatus,
        firebaseAuth: FirebaseAuthAPI,
        dataStoreManager: DataStoreManager,
        @IoCoroutineScope coroutineScope: CoroutineScope
    ): LeaveGroupUseCase =
        LeaveGroupUseCaseImpl(
            usersRepository,
            groupRepository,
            getGroupUsersUseCase,
            firebaseConnectionStatus,
            firebaseAuth,
            dataStoreManager,
            coroutineScope
        )

    @Provides
    fun provideJoinGroupUseCase(
        usersRepository: UsersRepository,
        groupRepository: GroupRepository,
        firebaseConnectionStatus: ConnectionStatus
    ): JoinGroupUseCase =
        JoinGroupUseCaseImpl(usersRepository, groupRepository, firebaseConnectionStatus)

    @Provides
    fun provideGetGroupUseCase(groupRepository: GroupRepository): GetGroupUseCase = GetGroupUseCaseImpl(groupRepository)

    @Provides
    fun provideLinkValidationUseCase(groupRepository: GroupRepository, firebaseConnectionStatus: ConnectionStatus): LinkValidationUseCase =
        LinkValidationUseCaseImpl(groupRepository, firebaseConnectionStatus)

    @Provides
    fun provideGetGroupByLinkUseCase(groupRepository: GroupRepository, firebaseConnectionStatus: ConnectionStatus): GetGroupByLinkUseCase =
        GetGroupByLinkUseCaseImpl(groupRepository, firebaseConnectionStatus)

    @Provides
    fun provideChangeEmailUseCase(usersRepository: UsersRepository, firebaseAuth: FirebaseAuthAPI): ChangeEmailUseCase =
        ChangeEmailUseCaseImpl(usersRepository, firebaseAuth)

    @Provides
    fun provideGetGroupMembersUseCase(usersRepository: UsersRepository): GetGroupMembersUseCase =
        GetGroupMembersUseCaseImpl(usersRepository)

    @Provides
    fun GetGroupsForSendPingDialogUseCase(
        getUserGroupsUseCase: GetUserGroupsUseCase, getGroupUseCase: GetGroupUseCase,
        @IoCoroutineScope ioCoroutineScope: CoroutineScope
    ): GetGroupsForSendPingDialogUseCase =
        GetGroupsForSendPingDialogUseCaseImpl(getUserGroupsUseCase, getGroupUseCase, ioCoroutineScope)

    @Provides
    fun provideGetUserGroupsUseCase(firebaseAuth: FirebaseAuthAPI, usersRepository: UsersRepository): GetUserGroupsUseCase =
        GetUserGroupsUseCaseImpl(firebaseAuth, usersRepository)

    @Provides
    fun provideGetGroupsByIdsUseCase(groupRepository: GroupRepository): GetGroupsByIdsUseCase = GetGroupsByIdsUseCaseImpl(groupRepository)

    @Provides
    @Singleton
    fun provideHomeScreenViewState(): HomeScreenViewState = HomeScreenViewStateImpl()

    @Provides
    fun provideUpdateGroupImageUseCase(repository: GroupRepository, connectionStatus: ConnectionStatus): UpdateGroupImageUseCase =
        UpdateGroupImageUseCaseImpl(repository, connectionStatus)

}