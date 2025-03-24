package com.khrd.pingapp.homescreen.sendping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.PingData
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.GroupChangedIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.SendPingDatePickedIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.SendPingEmojiPickedIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.SendPingIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.SendPingReceiverAddedIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.SendPingReceiverAsGroupIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.SendPingReceiverRemovedIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.SendPingRecurringTimePickedIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.SendPingTimePickedIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogIntent.UnschedulePingIntent
import com.khrd.pingapp.homescreen.sendping.SendPingDialogSideEffect.SchedulePingEffect
import com.khrd.pingapp.homescreen.sendping.SendPingDialogSideEffect.SendPingFailureEffect
import com.khrd.pingapp.homescreen.sendping.SendPingDialogSideEffect.SendPingOfflineEffect
import com.khrd.pingapp.homescreen.sendping.SendPingDialogSideEffect.SendPingSuccessEffect
import com.khrd.pingapp.homescreen.states.CreatePingFailure
import com.khrd.pingapp.homescreen.states.CreatePingOfflineState
import com.khrd.pingapp.homescreen.states.CreatePingSuccess
import com.khrd.pingapp.homescreen.states.GetGroupUsersFailure
import com.khrd.pingapp.homescreen.states.GetGroupUsersSuccess
import com.khrd.pingapp.homescreen.states.GetUserGroupsFailure
import com.khrd.pingapp.homescreen.states.GetUserGroupsSuccess
import com.khrd.pingapp.homescreen.usecases.GetGroupUsersUseCase
import com.khrd.pingapp.homescreen.usecases.pings.CreatePingUseCase
import com.khrd.pingapp.pushnotification.CreatePushDataUseCase
import com.khrd.pingapp.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class SendPingDialogViewModel @Inject constructor(
    val getGroupUsersUseCase: GetGroupUsersUseCase,
    val getGroupsForSendPingDialogUseCase: GetGroupsForSendPingDialogUseCase,
    val createPingUseCase: CreatePingUseCase,
    val getGroupUseCase: GetGroupUseCase,
    val firebaseAuth: FirebaseAuthAPI,
    val sendPushUseCase: SendPushUseCase,
    val createPushDataUseCase: CreatePushDataUseCase,
    val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _ping = PingData()
    private val _sendPingDialogStateLiveData = MutableLiveData<SendPingDialogState>()
    val sendPingDialogStateLiveData: LiveData<SendPingDialogState> get() = _sendPingDialogStateLiveData

    private val _sendPingDialogSideEffectLiveData = MutableLiveData<Event<SendPingDialogSideEffect>>()
    val sendPingDialogSideEffectLiveData: LiveData<Event<SendPingDialogSideEffect>> get() = _sendPingDialogSideEffectLiveData

    private val calendar = Calendar.getInstance()

    private var _currentState = SendPingDialogState()

    private var _groupMembers = listOf<DatabaseUser>()
    private var _listOfGroups = listOf<DatabaseGroup>()
    private var _groupId: String? = runBlocking { dataStoreManager.getCurrentGroup() }
    var isInitialized: Boolean = false


    fun init(group: String?, receivers: List<String>?, sentToEveryone: Boolean) {
        if (isInitialized) {
            return
        }
        if (group != null) {
            _groupId = group
        }
        _ping.groupFrom = _groupId
        postDialogState(_currentState.copy(isLoading = true))
        getGroupsForSendPingDialogUseCase.loadGroups {
            when (it) {
                is GetUserGroupsFailure -> {
                }

                is GetUserGroupsSuccess -> {
                    _listOfGroups = it.listOfGroups
                    _currentState = _currentState.copy(listOfGroups = it.listOfGroups, isLoading = false, isGroupChecked = sentToEveryone)
                    updateGroupFrom()
                    loadUsers(receivers)
                }
            }
        }
    }

    private fun postDialogState(state: SendPingDialogState) {
        _currentState = state
        _sendPingDialogStateLiveData.postValue(_currentState)
    }

    private fun postDialogSideEffect(state: SendPingDialogSideEffect) {
        viewModelScope.launch(Dispatchers.Main) {
            _sendPingDialogSideEffectLiveData.value = Event(state)
        }
    }

    private fun loadUsers(initialReceivers: List<String>? = null) {
        _groupId?.let {
            postDialogState(_currentState.copy(isLoading = true))
            getGroupUsersUseCase.getGroupUsers(it, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) {
                when (it) {
                    GetGroupUsersFailure -> {
                    }

                    is GetGroupUsersSuccess -> {
                        _groupMembers = it.members
                        if (_currentState.isGroupChecked) {
                            _ping.receivers = _groupMembers.map { it.id ?: "" }
                        }
                        updateAdapter()
                        if (initialReceivers != null) {
                            onSendPingIntent(SendPingReceiverAddedIntent(initialReceivers))
                        }
                    }
                }
                postDialogState(_currentState.copy(isLoading = false))
                isInitialized = true
            }
        }
    }

    fun onSendPingIntent(action: SendPingDialogIntent) {
        when (action) {
            SendPingIntent -> {
                if (_ping.receivers.isEmpty()) {
                    postDialogSideEffect(SendPingFailureEffect(SendPingError.NO_RECEIVER))
                } else if (_ping.scheduledTime != null && _ping.scheduledTime?.time!! < Calendar.getInstance().time.time) {
                    postDialogSideEffect(SendPingFailureEffect(SendPingError.SCHEDULED_FOR_PAST))
                } else {
                    sendPing()
                }
            }

            is SendPingDatePickedIntent -> {
                onDatePicked(action.year, action.month, action.day)
                updateDialogState()
            }

            is SendPingTimePickedIntent -> {
                onTimePicked(action.hour, action.minute)
                _currentState = _currentState.copy(isScheduleChecked = true)
                updateDialogState()
            }

            is SendPingRecurringTimePickedIntent -> {
                _ping.recurringTime = action.recurringTime
                _currentState = _currentState.copy(recursion = action.recurringTime)
                updateDialogState()
            }

            is SendPingReceiverAddedIntent -> {
                if (!_currentState.isGroupChecked) {
                    _ping.receivers = _ping.receivers.toMutableList().apply { addAll(action.receiversId) }
                    action.receiversId.forEach() { receiver ->
                        val user = _groupMembers.find { it.id == receiver }
                        //here the list of chips is not displayed correctly if there are more than 4 users
                        _currentState = _currentState.copy(
                            listOfChips = _currentState.listOfChips.toMutableList().apply {
                                add(ReceiverChip(user?.id, user?.username, user?.photoURL))
                            })
                    }
                    updateAdapter()
                }
            }

            is SendPingReceiverRemovedIntent -> {
                if (_currentState.isGroupChecked) {
                    _ping.receivers = emptyList()
                    _ping.groupId = null
                    _currentState = _currentState.copy(isGroupChecked = false, listOfChips = listOf())
                } else {
                    _currentState = _currentState.copy(
                        listOfChips = _currentState.listOfChips.toMutableList().apply { removeIf { it.id == action.id } })
                    _ping.receivers = _ping.receivers.toMutableList().apply { remove(action.id) }
                }
                updateAdapter()
            }

            is SendPingEmojiPickedIntent -> {
                _ping.emoji = action.emoji
                _currentState = _currentState.copy(emoji = action.emoji)
                updateAdapter()
            }

            UnschedulePingIntent -> {
                _ping.scheduledTime = null
                _currentState = _currentState.copy(isScheduleChecked = false)
                updateAdapter()
            }

            is GroupChangedIntent -> {
                _groupId = action.groupId
                _ping.groupFrom = _groupId
                _ping.receivers = emptyList()
                _currentState = _currentState.copy(listOfChips = listOf())
                loadUsers()
                updateGroupFrom()
            }

            is SendPingReceiverAsGroupIntent -> {
                _currentState = _currentState.copy(isGroupChecked = action.isGroupPing)
                if (_currentState.isGroupChecked) {
                    _ping.receivers = _groupMembers.mapNotNull { it.id }
                    _ping.groupId = _groupId
                    val group = _listOfGroups.find { it.id == _groupId }
                    if (group != null) {
                        _currentState = _currentState.copy(listOfChips = listOf(ReceiverChip(group.id, group.name, group.photoURL)))
                    }
                } else {
                    _ping.receivers = emptyList()
                    _ping.groupId = null
                    _currentState = _currentState.copy(listOfChips = listOf())
                }
                updateAdapter()
            }

        }
    }

    private fun updateGroupFrom() {
        val group = getCurrentGroup()
        if (group != null) {
            _currentState = _currentState.copy(groupName = group.name ?: "")
            if (_currentState.isGroupChecked) {
                _ping.groupId = _groupId
                _currentState = _currentState.copy(listOfChips = listOf(ReceiverChip(group.id, group.name, group.photoURL)))
            }
        }
        updateAdapter()
    }

    private fun updateAdapter() {
        getCurrentGroup()?.let {
            postDialogState(
                _currentState.copy(
                    usersInGroup = _groupMembers.toMutableList().filter { !_ping.receivers.contains(it.id) },
                )
            )
        }
    }

    private fun updateDialogState() {
        postDialogState(_currentState)
    }

    private fun getCurrentGroup() = _listOfGroups.find { it.id == _groupId }

    private fun onDatePicked(year: Int, month: Int, day: Int) {
        calendar.set(year, month, day)
        _ping.scheduledTime = calendar.time
    }

    private fun onTimePicked(hour: Int, minute: Int) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        _ping.scheduledTime = calendar.time
    }

    private fun sendPing() {
        _ping.timestamp = System.currentTimeMillis()
        createPingUseCase.createPing(_ping) {
            when (it) {
                is CreatePingSuccess -> {
                    _ping.pingId = it.pings[0].id
                    if (it.pings[0].scheduledTime != null) {
                        postDialogSideEffect(SchedulePingEffect(it.pings[0].id, it.pings[0].scheduledTime!!, it.pings[0].recurringTime))
                    } else {
                        postDialogSideEffect(SendPingSuccessEffect)
                    }
                }

                is CreatePingFailure -> {
                    postDialogSideEffect(SendPingFailureEffect(SendPingError.PING_CREATION_FAILURE))
                }

                is CreatePingOfflineState -> {
                    postDialogSideEffect(SendPingOfflineEffect(_ping))
                }
            }
        }
    }
}