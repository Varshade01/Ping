package com.khrd.pingapp.homescreen.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusFailure
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusState
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus.GetUserMutedStatusSuccess
import com.khrd.pingapp.homescreen.usecases.UpdateMuteStateUseCase
import com.khrd.pingapp.utils.Event
import com.khrd.pingapp.utils.NavControllerUtils.navigateSafe
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupStatusDialogViewModel @Inject constructor(
    private val updateMuteStateUseCase: UpdateMuteStateUseCase,
    private val connectionStatus: ConnectionStatus,
) : ViewModel() {
    private val _isUserMutedLivedata = MutableLiveData<Event<GetUserMutedStatusState>>()
    val isUserMutedLivedata: LiveData<Event<GetUserMutedStatusState>> get() = _isUserMutedLivedata

    private val _allUsersLivedata = MutableLiveData<Pair<DatabaseGroup, DatabaseUser>>()
    val allUsersLivedata: LiveData<Pair<DatabaseGroup, DatabaseUser>> get() = _allUsersLivedata

    private lateinit var groupItem: DatabaseGroup
    private lateinit var currentItem: DatabaseUser

    fun init(group: DatabaseGroup, currentUser: DatabaseUser) {
        if (!::groupItem.isInitialized && !::currentItem.isInitialized) {
            groupItem = group
            currentItem = currentUser
            _allUsersLivedata.value = Pair(group, currentUser)
        }
    }

    fun muteGroup() {
        val connectionExists = connectionStatus.getConnectionStatus()
        val currentUserId = currentItem.id ?: return

        val groupId = groupItem.id
        if (connectionExists) {
            if (currentItem.mutedItems?.containsKey(groupId) == true) {
                updateMuteStateUseCase.unMuteItem(currentUserId, groupId) { it ->
                    when (it) {
                        is UserRequestState.UserRequestSuccess -> {
                            _isUserMutedLivedata.postValue(Event(GetUserMutedStatusSuccess(false)))
                            val newValueUnmuteItem = it.user?.mutedItems ?: return@unMuteItem
                            currentItem.mutedItems =
                                currentItem.mutedItems?.toMutableMap()?.let { HashMap(it.apply { clear() }) } ?: newValueUnmuteItem
                        }

                        is UserRequestState.UserRequestFail -> {
                            _isUserMutedLivedata.postValue(Event(GetUserMutedStatusFailure))
                        }

                        else -> {}
                    }
                }
            } else {
                updateMuteStateUseCase.muteItem(currentUserId, groupId) { it ->
                    when (it) {
                        is UserRequestState.UserRequestSuccess -> {
                            _isUserMutedLivedata.postValue(Event(GetUserMutedStatusSuccess(true)))
                            val newValueMuteItem = it.user?.mutedItems ?: return@muteItem
                            currentItem.mutedItems =
                                currentItem.mutedItems?.toMutableMap()?.let { HashMap(it.apply { putAll(newValueMuteItem) }) }
                                    ?: newValueMuteItem
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

    fun openSendPingDialog(groupId: String?, findNavController: NavController, sentToEveryone: Boolean) {
        findNavController.navigateUp()

        val action = HomescreenNavGraphDirections.openSendPingDialog(group = groupId, sentToEveryone = sentToEveryone)
        findNavController.navigateSafe(action)
    }
}