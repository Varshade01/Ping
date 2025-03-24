package com.khrd.pingapp.homescreen.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.khrd.pingapp.R
import com.khrd.pingapp.constants.LocaleConstants
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.states.*
import com.khrd.pingapp.homescreen.usecases.GetUserUseCase
import com.khrd.pingapp.homescreen.usecases.HideUserInformationUseCase
import com.khrd.pingapp.homescreen.usecases.deleteprofilephoto.DeleteProfilePhotoUseCase
import com.khrd.pingapp.homescreen.usecases.jobposition.JobNameValidationUseCase
import com.khrd.pingapp.homescreen.usecases.jobposition.UpdateJobPositionUseCase
import com.khrd.pingapp.homescreen.usecases.pings.ClearReceivedPingsCacheUseCase
import com.khrd.pingapp.homescreen.usecases.updateprofilephoto.UpdateProfilePhotoUseCase
import com.khrd.pingapp.homescreen.usecases.username.RenameUserNameUseCase
import com.khrd.pingapp.homescreen.usecases.username.ValidateUserNameUseCase
import com.khrd.pingapp.registration.verification.logout.ILogoutState
import com.khrd.pingapp.registration.verification.logout.LogoutUseCase
import com.khrd.pingapp.utils.Event
import com.khrd.pingapp.utils.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuthAPI,
    private val jobNameValidationUseCaseImpl: JobNameValidationUseCase,
    private val validateUserNameUseCase: ValidateUserNameUseCase,
    private val jobUpdateUseCase: UpdateJobPositionUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val renameUserNameUseCase: RenameUserNameUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateProfilePhotoUseCase: UpdateProfilePhotoUseCase,
    private val deleteProfilePhotoUseCase: DeleteProfilePhotoUseCase,
    private val clearReceivedPingsCacheUseCase: ClearReceivedPingsCacheUseCase,
    private val hideUserInformationUseCase: HideUserInformationUseCase,
    private val localeHelper: LocaleHelper,
) : ViewModel() {

    private val logoutIsClicked = MutableLiveData<Boolean>()
    var currentUser: DatabaseUser? = null

    private val _hideUserInfoLiveData = MutableLiveData<Event<IHideUserInfoState>>()
    val hideUserInfoLiveData: LiveData<Event<IHideUserInfoState>> get() = _hideUserInfoLiveData

    private val _editJobLiveData = MutableLiveData<Event<EditJobPositionState>>()
    val editJobLiveData: LiveData<Event<EditJobPositionState>> get() = _editJobLiveData
    private val _editNameStateLiveData = MutableLiveData<Event<IEditNameState>>()
    val editNameStateLiveData: LiveData<Event<IEditNameState>> get() = _editNameStateLiveData
    private val _profileStateLiveData = MutableLiveData<Event<ProfileState>>()
    val profileStateLiveData: LiveData<Event<ProfileState>> get() = _profileStateLiveData
    private val _updateProfilePhotoStateLiveData = MutableLiveData<Event<UpdateProfilePhotoState>>()
    val updateProfilePhotoStateLiveData: LiveData<Event<UpdateProfilePhotoState>> get() = _updateProfilePhotoStateLiveData
    private val _deleteProfilePhotoStateLiveData = MutableLiveData<Event<DeleteProfilePhotoState>>()
    val deleteProfilePhotoStateLiveData: LiveData<Event<DeleteProfilePhotoState>> get() = _deleteProfilePhotoStateLiveData
    private val _currentUserLiveData = MutableLiveData<DatabaseUser?>()
    val currentUserLiveData: LiveData<DatabaseUser?> get() = _currentUserLiveData
    val logoutStateLiveData: LiveData<ILogoutState?>
        get() = Transformations.switchMap(logoutIsClicked) {
            logoutUseCase.logout()
        }
    private val _languagesLiveData = MutableLiveData(getLanguages())
    val languagesLiveData: LiveData<List<LanguageListItem>> get() = _languagesLiveData

    private fun getLanguages(): List<LanguageListItem> {
        return mutableListOf(
            LanguageListItem(
                R.string.language_english,
                R.drawable.ic_english_flag,
                LocaleConstants.ENGLISH,
                localeHelper.getCurrentLocale() == LocaleConstants.ENGLISH
            ),
            LanguageListItem(
                R.string.language_ukrainian,
                R.drawable.ic_ukrainian_flag,
                LocaleConstants.UKRAINIAN,
                localeHelper.getCurrentLocale() == LocaleConstants.UKRAINIAN
            )
        )
    }

    fun updateLocale(locale: String) {
        runBlocking {
            localeHelper.setLocale(locale)
            _languagesLiveData.value = getLanguages()
        }
    }

    fun editJobPosition(name: String) {
        val validation = jobNameValidationUseCaseImpl.validate(name)
        if (validation == JobNameValidationState.VALID) {
            jobUpdateUseCase.updateJobPosition(name) { state ->
                when (state) {
                    is UserRequestState.UserRequestSuccess -> {
                        currentUser?.job = name
                        _editJobLiveData.postValue(Event(JobPositionSaveState(JobPositionSaveSuccessState(name))))
                    }
                    UserRequestState.UserRequestFail -> {
                        _editJobLiveData.value = Event(JobPositionSaveState(JobPositionFailureState))
                    }
                    is UserRequestState.UserRequestOffline -> {
                        currentUser?.job = name
                        _editJobLiveData.value = Event(JobPositionSaveState(JobPositionOfflineUpdateState(name)))
                    }
                }
            }
        } else {
            _editJobLiveData.value = Event(JobPositionSaveState(JobPositionSaveFailureState(validation)))
        }
    }

    fun getCurrentUser() {
        val id = firebaseAuth.currentUserId()
        if (id != null) {
            getUserUseCase.getUser(id, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) {
                when (it) {
                    is GetUserFailure -> {
                        currentUser = null
                        _currentUserLiveData.value = null
                    }
                    is GetUserSuccess -> {
                        currentUser = it.user
                        _currentUserLiveData.value = currentUser
                        it.user.username?.let { user ->
                        }
                    }
                }
            }
        } else {
            currentUser = null
            _currentUserLiveData.value = null
        }
    }

    fun editUserName(name: String) {
        val validation = validateUserNameUseCase.validateName(name)
        if (validation == UserNameValidationState.VALID) {
            currentUser?.id?.let {
                renameUserNameUseCase.renameUsername(name) {
                    when (it) {
                        RenameUsernameStateSuccess -> {
                            currentUser!!.username = name
                            _editNameStateLiveData.value = Event(EditNameSuccess())
                        }
                        RenameUsernameStateFailure -> {
                            _editNameStateLiveData.value = Event(EditNameFailure())
                        }
                        RenameUsernameOfflineState -> {
                            _editNameStateLiveData.value = Event(EditNameOffline(name))
                        }
                    }
                }
            }
        } else {
            _editNameStateLiveData.value = Event(NameSaveState(validation))
        }
    }

    fun onActionChanged(action: ProfileAction) {
        when (action) {
            is ChangePasswordAction -> {
                _profileStateLiveData.value = Event(ToChangePasswordActivity())
            }
            is DeleteAccountAction -> {
                _profileStateLiveData.value = Event(ToDeleteAccount())
            }
        }
    }

    fun logout() {
        logoutIsClicked.value = true
        clearReceivedPingsCacheUseCase.clearCache()
    }

    fun onHideClicked(isHide: Boolean) {
        currentUser?.id?.let {
            hideUserInformationUseCase.hideUserInfo(it, isHide) {
                when (it) {
                    is UserRequestState.UserRequestSuccess -> {
                        currentUser = currentUser?.copy(hideInfo = isHide)
                        _hideUserInfoLiveData.value = Event(HideUserInfoSuccess(isHide))
                        _currentUserLiveData.value = currentUser
                    }
                    is UserRequestState.UserRequestFail -> {
                        _hideUserInfoLiveData.value = Event(HideUserInfoFailure())
                    }
                }
            }
        }
    }

    fun updateProfilePhoto(bytes: ByteArray) {
        currentUser?.id?.let {
            updateProfilePhotoUseCase.updateProfilePhoto(it, bytes) { state ->
                when (state) {
                    is UserRequestState.UserRequestSuccess -> {
                        currentUser?.photoURL = state.user?.photoURL
                        _updateProfilePhotoStateLiveData.value = Event(UpdateProfilePhotoSuccess(state.user?.photoURL))
                    }
                    else -> {
                        _updateProfilePhotoStateLiveData.value = Event(UpdateProfilePhotoFail)
                    }
                }
            }
        }
    }

    fun deleteProfilePhoto() {
        currentUser?.id?.let {
            deleteProfilePhotoUseCase.deleteProfilePhoto(it) { state ->
                when (state) {
                    is UserRequestState.UserRequestSuccess -> {
                        currentUser?.photoURL = null
                        _deleteProfilePhotoStateLiveData.value = Event(DeleteProfilePhotoStateSuccess)
                    }
                    else -> {
                        _deleteProfilePhotoStateLiveData.value = Event(DeleteProfilePhotoStateFail)
                    }
                }
            }
        }
    }

    fun getUserEmail() = firebaseAuth.currentUserEmail()

}