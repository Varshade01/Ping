package com.khrd.pingapp.homescreen.fragments

import android.os.Build
import android.os.ConditionVariable
import androidx.annotation.RequiresApi
import androidx.core.text.toSpannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khrd.pingapp.data.groups.*
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.leaveGroup.LeaveGroupUseCase
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.listeners.UsersListener
import com.khrd.pingapp.homescreen.sendping.GetGroupMembersUseCase
import com.khrd.pingapp.homescreen.sendping.GetGroupsForSendPingDialogUseCase
import com.khrd.pingapp.homescreen.sendping.GetUserGroupsUseCase
import com.khrd.pingapp.homescreen.states.*
import com.khrd.pingapp.homescreen.usecases.GetUserUseCase
import com.khrd.pingapp.homescreen.usecases.UpdateMuteStateUseCase
import com.khrd.pingapp.utils.Event
import com.khrd.pingapp.utils.OnlineHelper
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuthAPI,
    private val getGroupUseCase: GetGroupUseCase,
    private val getUserGroupsUseCase: GetUserGroupsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getGroupsForSendPingDialogUseCase: GetGroupsForSendPingDialogUseCase,
    private val getGroupMembersUseCase: GetGroupMembersUseCase,
    private val leaveGroupUseCase: LeaveGroupUseCase,
    private val updateMuteStateUseCase: UpdateMuteStateUseCase,
    private val connectionStatus: ConnectionStatus,
    private val toastUtils: ToastUtils,
    private val dataStoreManager: DataStoreManager,
    private val onlineHelper: OnlineHelper,
) : ViewModel() {

    private val _shareInvitationLinkLiveData = MutableLiveData<Event<String?>>()
    val shareInvitationLinkLiveData: LiveData<Event<String?>> get() = _shareInvitationLinkLiveData

    private val _usersLiveData = MutableLiveData<UsersItemsData?>()
    val usersLiveData: LiveData<UsersItemsData?> get() = _usersLiveData

    //Remove next?
    private val _joinGroupLiveData = MutableLiveData<JoinByOutsideLinkState>()
    val joinGroupLiveData: LiveData<JoinByOutsideLinkState> get() = _joinGroupLiveData
    private val _sendPingLiveData = MutableLiveData<Event<SendPingDialogSuccess>>()
    val sendPingLiveData: LiveData<Event<SendPingDialogSuccess>> get() = _sendPingLiveData

    private val _openGroupListDialogLiveData = MutableLiveData<Event<Pair<List<DatabaseGroup>, DatabaseUser>>>()
    val openGroupListDialogLiveData: LiveData<Event<Pair<List<DatabaseGroup>, DatabaseUser>>> get() = _openGroupListDialogLiveData

    private val _searchInAllGroupsLiveData = MutableLiveData<Boolean>()
    val searchInAllGroupsLiveData: LiveData<Boolean> get() = _searchInAllGroupsLiveData

    private val _groupStateLiveData = MutableLiveData<GroupScreenState>()
    val groupStateLiveData: LiveData<GroupScreenState> get() = _groupStateLiveData

    private var _currentGroup: DatabaseGroup? = null
    private var _currentState = GroupScreenState()

    private val usersListener = object : UsersListener() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onUsersChanged(groupId: String, users: List<DatabaseUser>?) {
            viewModelScope.launch(Dispatchers.IO) {
                users?.let { databaseUsers ->
                    val mutedUsers = getMutedItems(databaseUsers)
                    val userItemList = mapDatabaseUserToUserItem(users)
                    handleMuteGroupState(mutedUsers)
                    val filteredGroupsUserItems = filterCommonGroups(userItemList)
                    val mutedCheckedUsers = handleMutedItems(filteredGroupsUserItems, mutedUsers)
                    _usersLiveData.postValue(UsersItemsData(groupId, mutedCheckedUsers, _currentGroup?.id == groupId))
                    setEmptyGroupState((filteredGroupsUserItems.isEmpty() && (_currentGroup?.id == groupId)))
                    setLoadingGroupState(groupLoaded = true)
                }
            }
        }

        private fun mapDatabaseUserToUserItem(users: List<DatabaseUser>): MutableList<UserItem> {
            val userItemList = mutableListOf<UserItem>()
            users.forEach {
                val userGroups = getUserGroups(it)
                val online = onlineHelper.getOnlineOfMultipleDevices(it.online?.values?.toList())
                val userItem = UserItem(it.username?.toSpannable(), it.job, it.photoURL, it.id, userGroups, online, isHide = it.hideInfo)

                userItemList.add(userItem)
            }
            return userItemList
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
    }

    private fun handleMuteGroupState(mutedUsers: List<String>) {
        if (mutedUsers.contains(_currentGroup?.id)) {
            setMutedGroupState(groupMuted = true)
        } else {
            setMutedGroupState(groupMuted = false)
        }
    }

    private fun handleMutedItems(groupUsers: List<UserItem>, mutedUsers: List<String>): List<UserItem> {
        val result = groupUsers.map {
            if (mutedUsers.contains(it.userId)) {
                it.apply { muted = true }
            } else {
                it
            }
        }
        return result
    }

    private fun getMutedItems(users: List<DatabaseUser>): List<String> {
        val currentUser = users.find { it.id == firebaseAuth.currentUserId() }
        return currentUser?.mutedItems?.keys?.toList() ?: emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun filterCommonGroups(userItemList: MutableList<UserItem>): List<UserItem> {
        //getting current user groups
        val currentUserGroups = userItemList.find { it.userId == firebaseAuth.currentUserId() }?.groups
        val result = userItemList.onEach { userItem ->
            //update userItem groups. After transformation user will have groups which are common with current user groups
            currentUserGroups?.intersect(userItem.groups)?.toList()?.let { userItem.groups = it }
        }
        //filtering current user
        result.removeIf { it.userId == firebaseAuth.currentUserId() }
        return result
    }

    fun searchInAllGroups(selected: Boolean) {
        _searchInAllGroupsLiveData.postValue(selected)
        getUserGroupsUseCase.getUserGroups(DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { state ->
            if (state is UserRequestState.UserRequestSuccess) {
                val groupsIds = state.user?.groups?.keys
                setSingleGroupState(groupsIds?.size == 1)
                groupsIds?.forEach { groupId ->
                    loadUsers(groupId)
                }
            }
        }
    }

    fun loadGroups() {
        getUserGroupsUseCase.getUserGroups(DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { state ->
            setLoadingGroupState(groupLoaded = false)
            when (state) {
                UserRequestState.UserRequestFail -> {
                    _currentGroup = null
                    setDatabaseGroupState(null)
                    _usersLiveData.value = null
                    setLoadingGroupState(groupLoaded = true)
                }
                is UserRequestState.UserRequestSuccess -> {
                    viewModelScope.launch {
                        val currentGroup = dataStoreManager.getCurrentGroup()
                        // check if current group in shared preferences && if in -> show current group
                        if (currentGroup.isNotEmpty() && state.user?.groups?.keys?.contains(currentGroup) == true) {
                            getGroup(currentGroup)
                        } // if not -> sharedPref group goes null and get 0 index group
                        else {
                            dataStoreManager.saveCurrentGroup("")
                            val groups = state.user?.groups?.keys?.toList()
                            if (!groups.isNullOrEmpty()) {
                                getGroup(groups[0])
                            } else {
                                getGroup(null)
                            }
                        }
                        val groupsIds = state.user?.groups?.keys
                        setSingleGroupState(groupsIds?.size == 1)
                    }
                }
            }
        }
    }

    fun getGroup(id: String?) {
        if (id != null) {
            getGroupUseCase.getGroup(id, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { groupState ->
                when (groupState) {
                    is GroupFailure -> {
                        _currentGroup = null
                        setDatabaseGroupState(null)
                        _usersLiveData.value = null
                        setLoadingGroupState(groupLoaded = true)
                        if (groupState.error == GroupError.UNEXISTING_GROUP) {
                            leaveGroupUseCase.leaveGroup(id) {
                                when (it) {
                                    is GroupFailure -> {
                                    }
                                    is GroupOfflineState -> {
                                    }
                                    is GroupSuccess -> loadGroups()
                                }
                            }
                        }
                    }
                    is GroupSuccess -> {
                        viewModelScope.launch {
                            dataStoreManager.saveCurrentGroup(groupState.group.id)
                            _currentGroup = groupState.group
                            setDatabaseGroupState(groupState.group)
                            loadUsers(groupState.group.id)
                        }
                    }
                }
            }
        } else {
            _currentGroup = null
            setDatabaseGroupState(null)
            _usersLiveData.postValue(null)
            setLoadingGroupState(groupLoaded = true)
        }
    }

    private fun loadUsers(groupId: String) {
        getGroupMembersUseCase.addListener(groupId, usersListener)
        getGroupMembersUseCase.getGroupMembers(groupId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
    }

    fun clickOpenBottomSheet() {
        getGroupsForSendPingDialogUseCase.loadGroups { groups ->
            when (groups) {
                is GetUserGroupsSuccess -> {
                    getUserUseCase.getUser(firebaseAuth.currentUserId() ?: "", DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { user ->
                        when (user) {
                            is GetUserSuccess -> {
                                _openGroupListDialogLiveData.postValue(Event(Pair(groups.listOfGroups, user.user)))
                            }
                            is GetUserFailure -> {}
                        }
                    }
                }
                is GetUserGroupsFailure -> {
                }
            }
        }
    }


    fun newGroupSelected(id: String?) {
        viewModelScope.launch {
            id?.let {
                dataStoreManager.saveCurrentGroup(it)
                loadGroups()
            }
        }
    }

    fun getCurrentGroup() = _currentGroup

    fun invitationLinkClicked() {
        _shareInvitationLinkLiveData.value = Event(_currentGroup?.invitationLink)
    }

    fun onSendPingToUserClicked(user: UserItem) {
        if (isSearchInAllGroups()) {
            setFirstCommonGroupForSendPingDialog(user)
        } else {
            setGroupForSendPingDialog(user)
        }
    }

    private fun setFirstCommonGroupForSendPingDialog(user: UserItem) {
        getUserUseCase.getUser(user.userId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { getUserState ->
            if (getUserState is GetUserSuccess) {
                if (isUserHasCurrentGroup(getUserState.user.groups?.keys)) {
                    setGroupForSendPingDialog(user)
                } else {
                    getUserGroupsUseCase.getUserGroups(DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { userRequestState ->
                        if (userRequestState is UserRequestState.UserRequestSuccess) {
                            setFirstCommonGroup(getUserState, userRequestState, user)
                        }
                    }
                }
            }
        }
    }

    private fun isUserHasCurrentGroup(keys: MutableSet<String>?) = keys?.contains(_currentGroup?.id) == true

    private fun isSearchInAllGroups() = _searchInAllGroupsLiveData.value == true

    private fun setFirstCommonGroup(getUserState: GetUserSuccess, userRequestState: UserRequestState.UserRequestSuccess, user: UserItem) {
        val matchedUserGroupsList = getUserState.user.groups?.keys?.toList()
        val currentUserGroupsList = userRequestState.user?.groups?.keys?.toList()
        if (matchedUserGroupsList != null && currentUserGroupsList != null) {
            setGroupForSendPingDialog(user, matchedUserGroupsList.intersect(currentUserGroupsList).first())
        }
    }

    private fun setGroupForSendPingDialog(user: UserItem, group: String? = null) {
        _sendPingLiveData.postValue(Event(SendPingDialogSuccess(user.userId, group)))
    }

    private fun setDatabaseGroupState(databaseGroup: DatabaseGroup?) {
        postCurrentState(_currentState.copy(databaseGroup = databaseGroup))
    }

    private fun setSingleGroupState(userHasOnlyOneGroup: Boolean) {
        postCurrentState(_currentState.copy(userHasOneGroup = userHasOnlyOneGroup))
    }

    fun setSearchState(collapsed: Boolean) {
        postCurrentState(_currentState.copy(isSearchCollapsed = collapsed))
    }

    fun setEmptyGroupState(groupEmpty: Boolean) {
        postCurrentState(_currentState.copy(isGroupEmpty = groupEmpty))
    }

    private fun setLoadingGroupState(groupLoaded: Boolean) {
        postCurrentState(_currentState.copy(groupLoaded = groupLoaded))
    }

    private fun setMutedGroupState(groupMuted: Boolean) {
        postCurrentState(_currentState.copy(isMuted = groupMuted))
    }

    private fun postCurrentState(state: GroupScreenState) {
        _currentState = state
        _groupStateLiveData.postValue(_currentState)
    }

    fun onMuteUserClicked(user: UserItem) {
        val connectionExists = connectionStatus.getConnectionStatus()
        val currentUserId = firebaseAuth.currentUserId() ?: ""
        val userId = user.userId ?: ""
        if (connectionExists) {
            if (user.muted) {
                updateMuteStateUseCase.unMuteItem(currentUserId, userId)
            } else {
                updateMuteStateUseCase.muteItem(currentUserId, userId)
            }
        } else {
            toastUtils.showNetworkErrorToast()
        }
    }

    override fun onCleared() {
        getGroupMembersUseCase.removeListener(usersListener)
    }
}