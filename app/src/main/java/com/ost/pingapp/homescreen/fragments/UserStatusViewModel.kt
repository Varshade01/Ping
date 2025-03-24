package com.khrd.pingapp.homescreen.fragments

import android.os.ConditionVariable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusFailure
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusState
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusSuccess
import com.khrd.pingapp.homescreen.states.GetUserSuccess
import com.khrd.pingapp.homescreen.usecases.GetUserUseCase
import com.khrd.pingapp.homescreen.usecases.UpdateMuteStateUseCase
import com.khrd.pingapp.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserStatusViewModel @Inject constructor(
    private val updateMuteStateUseCase: UpdateMuteStateUseCase,
    private val connectionStatus: ConnectionStatus,
    private val firebaseAuth: FirebaseAuthAPI,
    private val getGroupUseCase: GetGroupUseCase,
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {

    private val _isUserMutedLivedata = MutableLiveData<Event<GetUserMutedStatusState>>()
    val isUserMutedLivedata: LiveData<Event<GetUserMutedStatusState>> get() = _isUserMutedLivedata
    private val _userLivedata = MutableLiveData<UserItem>()
    val userLivedata: LiveData<UserItem> get() = _userLivedata

    private lateinit var userItem: UserItem
    private var group: DatabaseGroup? = null

    fun init(user: UserItem, group: DatabaseGroup?) {
        if (!::userItem.isInitialized) {
            userItem = user
        }
        this.group = group
        filterCommonGroups()
    }

    fun muteUser() {
        val connectionExists = connectionStatus.getConnectionStatus()
        val currentUserId = firebaseAuth.currentUserId() ?: ""
        val userId = userItem.userId ?: ""
        if (connectionExists) {
            if (userItem.muted) {
                updateMuteStateUseCase.unMuteItem(currentUserId, userId) {
                    when (it) {
                        is UserRequestState.UserRequestSuccess -> {
                            _isUserMutedLivedata.postValue(Event(GetUserMutedStatusSuccess(false)))
                            userItem.muted = false
                        }

                        is UserRequestState.UserRequestFail -> {
                            _isUserMutedLivedata.postValue(Event(GetUserMutedStatusFailure))
                        }

                        else -> {}
                    }
                }
            } else {
                updateMuteStateUseCase.muteItem(currentUserId, userId) {
                    when (it) {
                        is UserRequestState.UserRequestSuccess -> {
                            _isUserMutedLivedata.postValue(Event(GetUserMutedStatusSuccess(true)))
                            userItem.muted = true
                        }

                        is UserRequestState.UserRequestFail -> {
                            _isUserMutedLivedata.postValue(Event(GetUserMutedStatusFailure))
                        }

                        else -> {}
                    }
                }
            }
        } else {
            _isUserMutedLivedata.value = Event(GetUserMutedStatusFailure)
        }
    }

    private fun filterCommonGroups() {
        getUserUseCase.getUser(firebaseAuth.currentUserId(), DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) {
            viewModelScope.launch(Dispatchers.IO) {
                when (it) {
                    is GetUserSuccess -> {
                        userItem.groups = getUserGroups(it.user).intersect(userItem.groups.toSet()).toList()
                        _userLivedata.postValue(userItem)
                    }

                    else -> {}
                }
            }
        }
    }

    private fun getUserGroups(databaseUser: DatabaseUser): MutableList<DatabaseGroup?> {
        val userGroups = mutableListOf<DatabaseGroup?>()
        databaseUser.groups?.keys?.forEach { groupId ->
            val conditionVariable = ConditionVariable()
            getGroupUseCase.getGroup(groupId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { groupState ->
                if (groupState is GroupSuccess) {
                    userGroups.add(groupState.group)
                }
                conditionVariable.open()
            }
            conditionVariable.block()
        }
        return userGroups
    }

    fun openSendPingDialog(userId: String?, findNavController: NavController) {
        val usersId = userId?.let { arrayOf(it) }
        findNavController.navigateUp()
        val action = HomescreenNavGraphDirections.openSendPingDialog(users = usersId, group = group?.id)
        findNavController.navigate(action)
    }
}