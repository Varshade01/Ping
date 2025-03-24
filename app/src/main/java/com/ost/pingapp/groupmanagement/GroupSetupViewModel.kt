package com.khrd.pingapp.groupmanagement

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khrd.pingapp.data.groups.*
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.adapter.MuteGroupItem
import com.khrd.pingapp.groupmanagement.states.*
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.GroupNameValidationUseCase
import com.khrd.pingapp.groupmanagement.usecases.LinkValidationUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.createGroup.CreateGroupUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.createGroup.GenerateLinkUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.joinGroup.GetGroupByLinkUseCase
import com.khrd.pingapp.groupmanagement.usecases.joinGroup.JoinGroupUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.leaveGroup.LeaveGroupUseCaseImpl
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.CheckMutedItemsUseCase
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.RenameGroupUseCaseImpl
import com.khrd.pingapp.homescreen.usecases.UpdateMuteStateUseCase
import com.khrd.pingapp.homescreen.usecases.updategroupimage.UpdateGroupImageUseCase
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupSetupViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuthAPI,
    private val createGroupUseCase: CreateGroupUseCaseImpl,
    private val generateLinkUseCase: GenerateLinkUseCaseImpl,
    private val renameGroupUseCase: RenameGroupUseCaseImpl,
    private val leaveGroupUseCase: LeaveGroupUseCaseImpl,
    private val groupNameValidationUseCase: GroupNameValidationUseCase,
    private val joinGroupUseCase: JoinGroupUseCaseImpl,
    private val linkValidationUseCase: LinkValidationUseCaseImpl,
    private val getGroupByLinkUseCase: GetGroupByLinkUseCase,
    private val getGroupUseCase: GetGroupUseCase,
    private val userRepository: UsersRepository,
    private val dataStoreManager: DataStoreManager,
    private val updateGroupImageUseCase: UpdateGroupImageUseCase,
    private val updateMuteStateUseCase: UpdateMuteStateUseCase,
    private val connectionStatus: ConnectionStatus,
    private val toastUtils: ToastUtils,
    private val checkMutedItemsUseCase: CheckMutedItemsUseCase
) : ViewModel() {

    private val _createGroupStateLiveData = MutableLiveData<ICreateGroupState?>()
    val createGroupStateLiveData: LiveData<ICreateGroupState?> get() = _createGroupStateLiveData

    private val _renameGroupStateLiveData = MutableLiveData<RenameGroupState>()
    val renameGroupStateLiveData: LiveData<RenameGroupState> get() = _renameGroupStateLiveData

    private val _joinGroupStateLiveData = MutableLiveData<JoinGroupState?>()
    val joinGroupStateLiveData: LiveData<JoinGroupState?> get() = _joinGroupStateLiveData

    private val _leaveGroupLiveData = MutableLiveData<ILeaveGroupState?>()
    val leaveGroupLiveData: LiveData<ILeaveGroupState?> get() = _leaveGroupLiveData

    private val _shareInvitationLinkLiveData = MutableLiveData<String?>()
    val shareInvitationLinkLiveData: LiveData<String?> get() = _shareInvitationLinkLiveData

    private val _updateGroupImageStateLiveData = MutableLiveData<GroupState?>()
    val updateGroupImageStateLiveData: LiveData<GroupState?> get() = _updateGroupImageStateLiveData

    private val _updateGroupImageLoadingStateLiveData = MutableLiveData<Boolean>()
    val updateGroupImageLoadingStateLiveData: LiveData<Boolean> get() = _updateGroupImageLoadingStateLiveData

    private val _updateGroupMuteButtonStateLiveData = MutableLiveData<MuteGroupState>()
    val updateGroupMuteButtonStateLiveData: LiveData<MuteGroupState> get() = _updateGroupMuteButtonStateLiveData

    private var _createGroup: DatabaseGroup = DatabaseGroup()
    private var _currentGroup: DatabaseGroup? = null
    private var _invitationLink: String = ""
    private var groupIdFromDeepLink: String? = null

    fun onCreateGroupNameChanged(enteredName: String) {
        _createGroup.name = enteredName
    }

    fun onJoinGroupLinkChanged(link: String) {
        _invitationLink = link
    }

    fun onShareLinkClicked() {
        _shareInvitationLinkLiveData.value = _currentGroup?.invitationLink
    }

    fun onMuteGroupClicked(group: MuteGroupItem) {
        val connectionExists = connectionStatus.getConnectionStatus()
        val currentUserId = firebaseAuth.currentUserId() ?: ""
        val groupId = _currentGroup?.id ?: ""
        if (connectionExists) {
            if (group.muted) {
                updateMuteStateUseCase.unMuteItem(currentUserId, groupId) {
                    when (it) {
                        is UserRequestState.UserRequestSuccess -> {
                            _updateGroupMuteButtonStateLiveData.postValue((MuteGroupSuccess(isMuted = false)))
                        }
                        is UserRequestState.UserRequestFail ->
                            _updateGroupMuteButtonStateLiveData.value = MuteGroupFailure(MuteGroupError.MUTE_GROUP_FAILED)
                    }
                }
            } else {
                updateMuteStateUseCase.muteItem(currentUserId, groupId) {
                    when (it) {
                        is UserRequestState.UserRequestSuccess -> {
                            _updateGroupMuteButtonStateLiveData.postValue((MuteGroupSuccess(isMuted = true)))
                        }
                        is UserRequestState.UserRequestFail ->
                            _updateGroupMuteButtonStateLiveData.value = MuteGroupFailure(MuteGroupError.MUTE_GROUP_FAILED)
                    }
                }
            }
        } else {
            _updateGroupMuteButtonStateLiveData.value = MuteGroupFailure(MuteGroupError.NETWORK_ERROR)
            toastUtils.showNetworkErrorToast()
        }
    }


    fun onCreateGroupAction(action: ICreateGroupState) {
        when (action) {
            is CreateGroupSaveAction -> {
                if (validateName(_createGroup.name)) {
                    if (_createGroup.id.isEmpty()) {
                        createGroup()
                    } else {
                        renameGroup()
                    }
                }
            }
            is CreateGroupEditAction -> _createGroupStateLiveData.value = CreateGroupSaveAction
            is CreateGroupDoneAction -> _createGroupStateLiveData.value = CreateGroupDoneAction
            is CreateGroupCopyLinkAction -> _createGroupStateLiveData.value = CreateGroupCopyLinkAction(_createGroup.invitationLink)
            is CreateGroupConfirmationAction -> {
            }
            is CreateGroupFailureState -> {
            }
        }
    }

    private fun renameGroup() {
        renameGroupUseCase.renameGroup(_createGroup.id, _createGroup.name!!) {
            when (it) {
                is GroupSuccess -> {
                    _createGroupStateLiveData.value = CreateGroupEditAction(GroupNameValidationState.VALID, _createGroup.invitationLink!!)
                }
                is GroupFailure -> {
                    handleRenameGroupError(it.error)
                }
                is GroupOfflineState -> {
                }
            }
        }
    }

    private fun createGroup() {
        val userId = firebaseAuth.currentUserId()
        userId?.let {
            createGroupUseCase.createGroup(it, _createGroup.name) { groupState ->
                when (groupState) {
                    is GroupFailure -> {
                        handleCreateGroupErrors(groupState.error)
                    }
                    is GroupSuccess -> {
                        viewModelScope.launch {
                            dataStoreManager.saveCurrentGroup(groupState.group.id)
                            generateInvitationLink(groupState)
                        }
                    }
                    is GroupOfflineState -> {
                    }
                }
            }
        }
    }

    private fun handleCreateGroupErrors(error: GroupError) {
        when (error) {
            GroupError.UNEXISTING_GROUP -> _createGroupStateLiveData.value =
                CreateGroupFailureState(CreateGroupActionError.CREATE_GROUP_FAILED)
            GroupError.UNKNOWN_ERROR -> _createGroupStateLiveData.value =
                CreateGroupFailureState(CreateGroupActionError.CREATE_GROUP_FAILED)
            GroupError.NETWORK_ERROR -> _createGroupStateLiveData.value = CreateGroupFailureState(CreateGroupActionError.NETWORK_ERROR)
        }
    }

    fun createAnotherGroup() {
        firebaseAuth.currentUserId()?.let {
            createGroupUseCase.createGroup(it, _createGroup.name) { groupState ->
                when (groupState) {
                    is GroupFailure -> {
                        CreateGroupFailureState(CreateGroupActionError.NETWORK_ERROR)
                    }
                    is GroupSuccess -> {
                        viewModelScope.launch {
                            dataStoreManager.saveCurrentGroup(groupState.group.id)
                            generateInvitationLink(groupState)
                        }
                    }
                    is GroupOfflineState -> {
                    }
                }
            }
        }
    }

    private fun generateInvitationLink(it: GroupState) {
        val id = getGroupIdFromState(it)
        _createGroup.id = id
        generateLinkUseCase.generateLink(id) { linkState ->
            when (linkState) {
                is GroupFailure -> {
                    _createGroupStateLiveData.value = CreateGroupFailureState(CreateGroupActionError.GENERATE_LINK_FAILED)
                }
                is GroupSuccess -> {
                    _createGroup.invitationLink = linkState.group.invitationLink.toString()
                    _createGroupStateLiveData.value = CreateGroupEditAction(GroupNameValidationState.VALID, _createGroup.invitationLink!!)
                }
                is GroupOfflineState -> {
                    _createGroup.invitationLink = linkState.group.invitationLink.toString()
                    _createGroupStateLiveData.value = CreateGroupEditAction(GroupNameValidationState.VALID, _createGroup.invitationLink!!)
                }
            }
        }
    }

    private fun getGroupIdFromState(state: GroupState) = when (state) {
        is GroupFailure -> ""
        is GroupOfflineState -> state.group.id
        is GroupSuccess -> state.group.id
        is GroupImageUpdateOfflineURI -> state.groupId
    }

    fun onRenameGroupAction(state: RenameGroupState) {

        _currentGroup?.let { currentGroup ->
            when (state) {
                is RenameGroupSaveAction -> {
                    val validationState = groupNameValidationUseCase.validateName(state.name)
                    if (validationState == GroupNameValidationState.VALID) {
                        val newName = state.name.trim()
                        renameGroupUseCase.renameGroup(currentGroup.id, newName) { groupState ->
                            when (groupState) {
                                is GroupSuccess -> {
                                    val name = groupState.group.name ?: ""
                                    currentGroup.name = name
                                    _renameGroupStateLiveData.postValue(RenameGroupSaveAction(GroupNameValidationState.VALID, name))
                                }
                                is GroupFailure -> {
                                    handleRenameGroupError(groupState.error)
                                }
                                is GroupOfflineState -> {
                                    val name = groupState.group.name ?: ""
                                    val groupId = groupState.group.id
                                    currentGroup.name = name
                                    _renameGroupStateLiveData.postValue(
                                        RenameGroupOfflineSaveAction(
                                            groupId,
                                            GroupNameValidationState.VALID,
                                            name
                                        )
                                    )
                                }
                            }
                        }
                    } else _renameGroupStateLiveData.value = RenameGroupSaveAction(validation = validationState)
                }
                is RenameGroupEditAction -> {
                    _renameGroupStateLiveData.value = RenameGroupEditAction
                }
                is RenameGroupFailure -> {
                    _renameGroupStateLiveData.value = RenameGroupFailure(RenameGroupError.RENAME_GROUP_FAILED)
                }
            }
        }

    }

    private fun handleRenameGroupError(error: GroupError) {
        when (error) {
            GroupError.UNEXISTING_GROUP -> _renameGroupStateLiveData.value = RenameGroupFailure(RenameGroupError.RENAME_GROUP_FAILED)
            GroupError.UNKNOWN_ERROR -> _renameGroupStateLiveData.value = RenameGroupFailure(RenameGroupError.RENAME_GROUP_FAILED)
            GroupError.NETWORK_ERROR -> _renameGroupStateLiveData.value = RenameGroupFailure(RenameGroupError.NETWORK_ERROR)
        }

    }

    fun onLeaveGroupAction() {
        leaveGroup()
    }

    private fun leaveGroup() {
        _currentGroup?.let { group ->
            leaveGroupUseCase.leaveGroup(group.id) {
                when (it) {
                    is GroupFailure -> handleLeaveGroupError(it.error)
                    is GroupSuccess -> {
                        _leaveGroupLiveData.value = LeaveGroupAction
                    }
                    is GroupOfflineState -> {
                        _leaveGroupLiveData.value = LeaveGroupOffline
                    }
                }
            }
        }
    }

    private fun handleLeaveGroupError(error: GroupError) {
        when (error) {
            GroupError.UNEXISTING_GROUP -> _leaveGroupLiveData.value = LeaveGroupFailure(LeaveGroupError.LEAVE_GROUP_FAILED)
            GroupError.UNKNOWN_ERROR -> _leaveGroupLiveData.value = LeaveGroupFailure(LeaveGroupError.LEAVE_GROUP_FAILED)
            GroupError.NETWORK_ERROR -> _leaveGroupLiveData.value = LeaveGroupFailure(LeaveGroupError.NETWORK_ERROR)
        }
    }

    fun onJoinGroupAction() {
        if (validateLink(_invitationLink)) {
            getGroupByLinkUseCase.getGroupByLink(_invitationLink) { groupState ->
                when (groupState) {
                    is GroupFailure -> {
                        handleJoinGroupError(groupState.error)
                    }
                    is GroupSuccess -> {
                        val userId = firebaseAuth.currentUserId()
                        if (userId != null) {
                            userRepository.getUserGroups(userId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { it ->
                                when (it) {
                                    is UserRequestState.UserRequestSuccess -> {
                                        val groupsIds = it.user?.groups?.keys?.toList()
                                        // if list of user groups do not contains current group -> JoinByOutsideLinkConfirmedState(current group name)
                                        if (groupsIds?.contains(groupState.group.id) == true) {
                                            _joinGroupStateLiveData.postValue(JoinGroupFailure(JoinGroupError.SAME_GROUP_ERROR))
                                        } else {
                                            _joinGroupStateLiveData.postValue(JoinByOutsideLinkConfirmedState(groupState.group.name))
                                        }
                                    }
                                    UserRequestState.UserRequestFail -> {
                                        Log.i("***", "Current group is empty")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun joinGroup(userId: String?) {
        userId?.let {
            getGroupByLinkUseCase.getGroupByLink(_invitationLink) { groupState ->
                when (groupState) {
                    is GroupFailure -> {
                        handleJoinGroupError(groupState.error)
                    }
                    is GroupSuccess -> {
                        joinGroupUseCase.joinGroup(userId, groupState.group.id, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) {
                            when (it) {
                                is GroupFailure -> handleJoinGroupError(it.error)
                                is GroupSuccess -> {
                                    viewModelScope.launch {
                                        dataStoreManager.saveCurrentGroup(it.group.id)
                                        _joinGroupStateLiveData.value = JoinGroupAction(_invitationLink)
                                    }
                                }

                                is GroupOfflineState -> {
                                }

                            }
                        }
                    }
                    is GroupOfflineState -> {
                    }
                }
            }
        }
    }

    private fun handleJoinGroupError(error: GroupError) {
        when (error) {
            GroupError.UNEXISTING_GROUP -> _joinGroupStateLiveData.value =
                JoinGroupFailure(JoinGroupError.UNEXISTING_GROUP)
            GroupError.UNKNOWN_ERROR -> _joinGroupStateLiveData.value =
                JoinGroupFailure(JoinGroupError.UNKNOWN_ERROR)
            GroupError.NETWORK_ERROR -> {
                _joinGroupStateLiveData.value =
                    JoinGroupFailure(JoinGroupError.NETWORK_ERROR)
            }
        }
    }

    fun joinAnotherGroup() {
        if (groupIdFromDeepLink.isNullOrEmpty()) {
            joinGroup()
        } else {
            joinByLink()
        }
    }

    fun joinGroup() {
        val userId = firebaseAuth.currentUserId()
        linkValidationUseCase.validateLink(_invitationLink) {
            when (it) {
                is GroupFailure -> {
                    handleJoinGroupError(it.error)
                }
                is GroupSuccess -> {
                    joinGroup(userId)
                }
            }
        }
    }

    private fun joinByLink() {
        val userId = firebaseAuth.currentUserId()
        userId?.let { userId ->
            getGroupUseCase.getGroup(groupIdFromDeepLink, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { groupState ->
                when (groupState) {
                    is GroupFailure -> {
                        handleJoinGroupError(groupState.error)
                    }
                    is GroupSuccess -> {
                        joinGroupUseCase.joinGroup(userId, groupIdFromDeepLink!!, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) {
                            when (it) {
                                is GroupFailure -> _joinGroupStateLiveData.value =
                                    JoinGroupFailure(JoinGroupError.OUTSIDE_LINK_INVALID)
                                is GroupSuccess -> {
                                    viewModelScope.launch {
                                        dataStoreManager.saveCurrentGroup(it.group.id)
                                        _joinGroupStateLiveData.value = JoinGroupAction("")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun validateLink(link: String): Boolean {
        if (link.isEmpty()) {
            _joinGroupStateLiveData.value = JoinGroupFailure(JoinGroupError.EMPTY_FIELD)
        } else if (!link.startsWith("https://khrd.page.link/")) {
            _joinGroupStateLiveData.value = JoinGroupFailure(JoinGroupError.LINK_INVALID)
        } else {
            return true
        }
        return false
    }

    private fun validateName(name: String?): Boolean =
        when (val state = groupNameValidationUseCase.validateName(name)) {
            GroupNameValidationState.VALID -> true
            GroupNameValidationState.EMPTY_FIELD -> {
                _createGroupStateLiveData.value = CreateGroupEditAction(state, "")
                false
            }
            GroupNameValidationState.TOO_LONG -> {
                _createGroupStateLiveData.value = CreateGroupEditAction(state, "")
                false
            }
            GroupNameValidationState.INVALID_CHARS -> {
                _createGroupStateLiveData.value = CreateGroupEditAction(state, "")
                false
            }
        }

    fun initGroup(group: DatabaseGroup?) {
        _currentGroup = group
    }

    fun setGroupIdFromDeepLink(groupIdFromDeepLink: String?) {
        this.groupIdFromDeepLink = groupIdFromDeepLink
        if (groupIdFromDeepLink != null) {
            getGroupUseCase.getGroup(groupIdFromDeepLink, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { groupState ->
                when (groupState) {
                    is GroupFailure -> {
                        handleJoinGroupError(groupState.error)
                    }
                    is GroupSuccess -> {
                        val userId = firebaseAuth.currentUserId()
                        if (userId != null) {
                            userRepository.getUserGroups(userId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) {
                                when (it) {
                                    is UserRequestState.UserRequestSuccess -> {
                                        val groupsIds = it.user?.groups?.keys?.toList()
                                        // if list of user groups do not contains current group -> JoinByOutsideLinkConfirmedState(current group name)
                                        if (groupsIds?.contains(groupState.group.id) == true) {
                                            _joinGroupStateLiveData.postValue(JoinGroupFailure(JoinGroupError.OUTSIDE_LINK_SAME_GROUP_ERROR))
                                        } else {
                                            _joinGroupStateLiveData.postValue(JoinByOutsideLinkConfirmedState(groupState.group.name))
                                        }
                                    }
                                    UserRequestState.UserRequestFail -> {
                                        _joinGroupStateLiveData.postValue(JoinGroupFailure(JoinGroupError.UNKNOWN_ERROR))
                                        Log.i("***", "Current group is empty")
                                    }
                                    is UserRequestState.UserRequestOffline -> {
                                        _joinGroupStateLiveData.postValue(JoinGroupFailure(JoinGroupError.NETWORK_ERROR))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateGroupImage(bytes: ByteArray, uri: Uri) {
        _currentGroup?.id?.let {
            _updateGroupImageLoadingStateLiveData.postValue(true)
            updateGroupImageUseCase.updateGroupImage(it, bytes, uri) { state ->
                when (state) {
                    is GroupSuccess -> {
                        _currentGroup?.photoURL = state.group.photoURL
                        _updateGroupImageLoadingStateLiveData.postValue(false)
                        _updateGroupImageStateLiveData.postValue(state)
                    }
                    is GroupImageUpdateOfflineURI -> {
                        _updateGroupImageLoadingStateLiveData.postValue(false)
                        _updateGroupImageStateLiveData.postValue(state)
                    }
                    is GroupFailure -> {
                        _updateGroupImageLoadingStateLiveData.postValue(false)
                        _updateGroupImageStateLiveData.postValue(GroupFailure(GroupError.UNKNOWN_ERROR))
                    }
                }
            }
        }
    }

    fun initMuteGroupItem() {
        viewModelScope.launch {
            val containsMutedGroup = checkMutedItemsUseCase.containsMutedGroup(_currentGroup?.id)
            _updateGroupMuteButtonStateLiveData.postValue(MuteGroupSuccess(isMuted =  containsMutedGroup))
        }
    }
}
