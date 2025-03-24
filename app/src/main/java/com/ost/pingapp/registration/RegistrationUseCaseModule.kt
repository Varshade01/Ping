package com.khrd.pingapp.registration

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RegistrationUseCaseModule {

    @Binds
    abstract fun bindRegistrationUseCase(
        registrationUseCaseImpl: RegistrationUseCaseImpl
    ): RegistrationUseCase
}