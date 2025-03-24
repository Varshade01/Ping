package com.khrd.pingapp.di

import android.content.Context
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.usecases.leaveGroup.LeaveGroupUseCase
import com.khrd.pingapp.homescreen.changePassword.ChangePasswordUseCase
import com.khrd.pingapp.homescreen.changePassword.ChangePasswordUseCaseImpl
import com.khrd.pingapp.homescreen.deleteAccount.usecases.DeleteAccountUseCaseImpl
import com.khrd.pingapp.homescreen.deleteAccount.usecases.IDeleteAccountUseCase
import com.khrd.pingapp.homescreen.usecases.deleteprofilephoto.DeleteProfilePhotoUseCase
import com.khrd.pingapp.homescreen.usecases.deleteprofilephoto.DeleteProfilePhotoUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.jobposition.JobNameValidationUseCase
import com.khrd.pingapp.homescreen.usecases.jobposition.JobNameValidationUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.jobposition.UpdateJobPositionUseCase
import com.khrd.pingapp.homescreen.usecases.jobposition.UpdateJobPositionUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.updateprofilephoto.UpdateProfilePhotoUseCase
import com.khrd.pingapp.homescreen.usecases.updateprofilephoto.UpdateProfilePhotoUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.username.RenameUserNameUseCase
import com.khrd.pingapp.homescreen.usecases.username.RenameUsernameUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.username.ValidateUserNameUseCase
import com.khrd.pingapp.homescreen.usecases.username.ValidateUserNameUseCaseImpl
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.resetpassword.ResetPasswordUseCase
import com.khrd.pingapp.resetpassword.ResetPasswordUseCaseImpl
import com.khrd.pingapp.workmanager.PingAppWorkManager
import com.khrd.pingapp.workmanager.PingAppWorkManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ProfileModule {

    @Provides
    fun provideJobValidationUseCase(): JobNameValidationUseCase = JobNameValidationUseCaseImpl()

    @Provides
    fun provideJobUpdateUseCase(
        repository: UsersRepository,
        connectionStatus: ConnectionStatus,
        firebaseAuth: FirebaseAuthAPI
    ): UpdateJobPositionUseCase = UpdateJobPositionUseCaseImpl(repository, connectionStatus, firebaseAuth)

    @Provides
    fun provideValidateUsernameUseCase(): ValidateUserNameUseCase = ValidateUserNameUseCaseImpl()

    @Provides
    fun provideRenameUsernameUseCase(
        repository: UsersRepository,
        connectionStatus: ConnectionStatus,
        firebaseAuth: FirebaseAuthAPI
    ): RenameUserNameUseCase =
        RenameUsernameUseCaseImpl(repository, connectionStatus, firebaseAuth)

    @Provides
    fun provideDeleteAccountUseCase(
        usersRepository: UsersRepository, firebaseAuth: FirebaseAuthAPI, leaveGroupUseCase: LeaveGroupUseCase
    ): IDeleteAccountUseCase = DeleteAccountUseCaseImpl(usersRepository, firebaseAuth, leaveGroupUseCase)

    @Provides
    fun provideChangePasswordUseCase(firebaseAuth: FirebaseAuthAPI, firebaseConnectionStatus: ConnectionStatus): ChangePasswordUseCase =
        ChangePasswordUseCaseImpl(firebaseAuth, firebaseConnectionStatus)

    @Provides
    fun provideUpdateProfilePhotoUseCase(repository: UsersRepository): UpdateProfilePhotoUseCase = UpdateProfilePhotoUseCaseImpl(repository)

    @Provides
    fun provideDeleteProfilePhotoUseCase(repository: UsersRepository): DeleteProfilePhotoUseCase = DeleteProfilePhotoUseCaseImpl(repository)

    @Provides
    fun bindResetPasswordUseCase(): ResetPasswordUseCase = ResetPasswordUseCaseImpl()

    @Provides
    fun providePingsWorkManager(@ApplicationContext context:Context): PingAppWorkManager = PingAppWorkManagerImpl(context)
}