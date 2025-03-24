package com.khrd.pingapp.di_test

import com.khrd.pingapp.di.ProfileModule
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.usecases.leaveGroup.LeaveGroupUseCase
import com.khrd.pingapp.homescreen.changePassword.ChangePasswordUseCase
import com.khrd.pingapp.homescreen.deleteAccount.usecases.IDeleteAccountUseCase
import com.khrd.pingapp.homescreen.usecases.jobposition.JobNameValidationUseCase
import com.khrd.pingapp.homescreen.usecases.jobposition.UpdateJobPositionUseCase
import com.khrd.pingapp.homescreen.usecases.updateprofilephoto.UpdateProfilePhotoUseCase
import com.khrd.pingapp.homescreen.usecases.username.RenameUserNameUseCase
import com.khrd.pingapp.homescreen.usecases.username.ValidateUserNameUseCase
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.resetpassword.ResetPasswordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import org.mockito.Mockito.mock

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ProfileModule::class])
class ProfileTestModule {
    @Provides
    fun provideJobValidationUseCase(): JobNameValidationUseCase = mock(JobNameValidationUseCase::class.java)

    @Provides
    fun provideJobUpdateUseCase(repository: UsersRepository): UpdateJobPositionUseCase = mock(UpdateJobPositionUseCase::class.java)

    @Provides
    fun provideValidateUsernameUseCase(): ValidateUserNameUseCase = mock(ValidateUserNameUseCase::class.java)

    @Provides
    fun provideRenameUsernameUseCase(repository: UsersRepository): RenameUserNameUseCase = mock(RenameUserNameUseCase::class.java)

    @Provides
    fun provideDeleteAccountUseCase(
        usersRepository: UsersRepository, firebaseAuth: FirebaseAuthAPI, leaveGroupUseCase: LeaveGroupUseCase
    ): IDeleteAccountUseCase = mock(IDeleteAccountUseCase::class.java)

    @Provides
    fun provideChangePasswordUseCase(firebaseAuth: FirebaseAuthAPI, firebaseConnectionStatus: ConnectionStatus): ChangePasswordUseCase =
        mock(ChangePasswordUseCase::class.java)

    @Provides
    fun provideUpdateProfilePhotoUseCase(repository: UsersRepository): UpdateProfilePhotoUseCase = mock(UpdateProfilePhotoUseCase::class.java)

    @Provides
    fun bindResetPasswordUseCase(): ResetPasswordUseCase = mock(ResetPasswordUseCase::class.java)
}