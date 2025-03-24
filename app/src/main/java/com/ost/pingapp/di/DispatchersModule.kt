package com.khrd.pingapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
class DispatchersModule {

    @DefaultCoroutineScope
    @Provides
    fun providesDefaultDispatcher(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @IoCoroutineScope
    @Provides
    fun providesIoDispatcher(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @MainCoroutineScope
    @Provides
    fun providesMainDispatcher(): CoroutineScope = CoroutineScope(Dispatchers.Main)
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class DefaultCoroutineScope

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class IoCoroutineScope

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class MainCoroutineScope