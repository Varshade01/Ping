package com.khrd.pingapp.di

import android.content.Context
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.datastore.DataStoreManagerImpl
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.groupmanagement.listeners.GroupImageOfflineUpdateHelper
import com.khrd.pingapp.groupmanagement.listeners.GroupImageOfflineUpdateHelperImpl
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.CheckMutedItemsUseCase
import com.khrd.pingapp.homescreen.usecases.GetUserUseCase
import com.khrd.pingapp.homescreen.usecases.OnlineHandlerUseCase
import com.khrd.pingapp.homescreen.usecases.pings.received.LoadReceivedPingsUseCase
import com.khrd.pingapp.utils.*
import com.khrd.pingapp.utils.deviceIdHelper.DeviceIdHelper
import com.khrd.pingapp.utils.deviceIdHelper.DeviceIdHelperImpl
import com.khrd.pingapp.utils.imageLoader.ImageLoader
import com.khrd.pingapp.utils.imageLoader.ImageLoaderImpl
import com.khrd.pingapp.utils.messenger.AppReceivedPingsMessenger
import com.khrd.pingapp.utils.messenger.AppReceivedPingsMessengerImpl
import com.khrd.pingapp.utils.passwordValidation.PasswordValidationUseCase
import com.khrd.pingapp.utils.passwordValidation.PasswordValidationUseCaseImpl
import com.khrd.pingapp.utils.viewstate.AppViewState
import com.khrd.pingapp.utils.viewstate.AppViewStateImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilsModule {

    @Singleton
    @Provides
    fun provideReceivedPingsMessenger(
        @ApplicationContext context: Context,
        @IoCoroutineScope ioCoroutineScope: CoroutineScope,
        appViewState: AppViewState,
        toastUtils: ToastUtils,
        loadReceivedPingsUseCase: LoadReceivedPingsUseCase,
        getUserUseCase: GetUserUseCase,
        getGroupUseCase: GetGroupUseCase,
        checkMutedItemsUseCase: CheckMutedItemsUseCase,
        auth: FirebaseAuthAPI,
        dataStoreManager:DataStoreManager
    ): AppReceivedPingsMessenger =
        AppReceivedPingsMessengerImpl(
            context,
            ioCoroutineScope,
            appViewState,
            toastUtils,
            loadReceivedPingsUseCase,
            getUserUseCase,
            getGroupUseCase,
            checkMutedItemsUseCase,
            auth,
            dataStoreManager
        )
    @Provides
    fun providePasswordValidationUseCase(): PasswordValidationUseCase = PasswordValidationUseCaseImpl()

    @Provides
    fun provideDataStoreAPI(@ApplicationContext context: Context): DataStoreManager = DataStoreManagerImpl(context)
    @Singleton
    @Provides
    fun providePingsAlarmManager(): PingsAlarmManager = PingsAlarmManagerImpl()

    @Singleton
    @Provides
    fun provideAppViewState(): AppViewState = AppViewStateImpl()

    @Singleton
    @Provides
    fun provideToastUtils(): ToastUtils = ToastUtilsImpl()

    @Singleton
    @Provides
    fun provideDialogUtils(): DialogUtils = DialogUtilsImpl()

    @Provides
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader = ImageLoaderImpl(context)

    @Provides
    fun provideGroupImageOfflineUpdateHelper(): GroupImageOfflineUpdateHelper = GroupImageOfflineUpdateHelperImpl()

    @Provides
    fun provideLocaleHelper(dataStoreManager: DataStoreManager): LocaleHelper =
        LocaleHelperImpl(dataStoreManager)

    @Provides
    fun provideDeviceIdHelper(@ApplicationContext context: Context): DeviceIdHelper = DeviceIdHelperImpl(context)

    @Singleton
    @Provides
    fun provideOnlineManager(@IoCoroutineScope coroutineScope: CoroutineScope, onlineHandlerUseCase: OnlineHandlerUseCase): OnlineManager =
        OnlineManagerImpl(coroutineScope, onlineHandlerUseCase)

    @Singleton
    @Provides
    fun provideOnlineHelper(): OnlineHelper = OnlineHelperImpl()
}
