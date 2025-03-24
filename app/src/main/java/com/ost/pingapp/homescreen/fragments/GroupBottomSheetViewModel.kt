package com.khrd.pingapp.homescreen.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.groupmanagement.states.MuteGroupError
import com.khrd.pingapp.groupmanagement.states.MuteGroupFailure
import com.khrd.pingapp.groupmanagement.states.MuteGroupState
import com.khrd.pingapp.groupmanagement.states.MuteGroupSuccess
import com.khrd.pingapp.homescreen.usecases.UpdateMuteStateUseCase
import com.khrd.pingapp.utils.ToastUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GroupBottomSheetViewModel @Inject constructor(
    private val connectionStatus: ConnectionStatus,
    private val firebaseAuth: FirebaseAuthAPI,
    private val updateMuteStateUseCase: UpdateMuteStateUseCase,
    private val toastUtils: ToastUtils
) : ViewModel() {

    private val _updateMuteGroupButtonStateLiveData = MutableLiveData<MuteGroupState>()
    val updateMuteGroupButtonStateLiveData: LiveData<MuteGroupState> get() = _updateMuteGroupButtonStateLiveData

    fun onMuteGroupClicked(groupId: String, isMuted: Boolean) {
        val connectionExists = connectionStatus.getConnectionStatus()
        val currentUserId = firebaseAuth.currentUserId() ?: ""
        if (connectionExists) {
            if (isMuted) {
                updateMuteStateUseCase.unMuteItem(currentUserId, groupId) {
                    when (it) {
                        is UserRequestState.UserRequestSuccess -> {
                            _updateMuteGroupButtonStateLiveData.postValue(MuteGroupSuccess(groupId = groupId, isMuted = false))
                        }

                        is UserRequestState.UserRequestFail ->
                            _updateMuteGroupButtonStateLiveData.postValue(MuteGroupFailure(MuteGroupError.MUTE_GROUP_FAILED))
                    }

                }
            } else {
                updateMuteStateUseCase.muteItem(currentUserId, groupId) {
                    when (it) {
                        is UserRequestState.UserRequestSuccess -> {
                            _updateMuteGroupButtonStateLiveData.postValue(MuteGroupSuccess(groupId = groupId, isMuted = true))

                        }
                        is UserRequestState.UserRequestFail ->
                            _updateMuteGroupButtonStateLiveData.postValue(MuteGroupFailure(MuteGroupError.MUTE_GROUP_FAILED))
                    }
                }
            }
        } else {
            _updateMuteGroupButtonStateLiveData.postValue(MuteGroupFailure(MuteGroupError.NETWORK_ERROR))
            toastUtils.showNetworkErrorToast()
        }
    }
}