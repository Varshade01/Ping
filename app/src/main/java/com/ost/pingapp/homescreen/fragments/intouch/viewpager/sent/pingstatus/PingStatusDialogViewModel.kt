package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.homescreen.adapter.UserItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases.ConvertToReceiverStatusItemsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PingStatusDialogViewModel @Inject constructor(
    private val convertToReceiverStatusItemsUseCase: ConvertToReceiverStatusItemsUseCase,
    private val getGroupUseCase: GetGroupUseCase,
) : ViewModel() {
    private val _receiversStatusItemsLiveData = MutableLiveData<GetReceiversStatusState>()
    val getReceiversStatusItemsLiveData: LiveData<GetReceiversStatusState> get() = _receiversStatusItemsLiveData

    private val _groupNameLiveData = MutableLiveData<String>()
    val groupNameLiveData: LiveData<String> get() = _groupNameLiveData

    fun getPingReceivers(receivers: List<UserItem>, listOfSeen: List<String>) {
        convertToReceiverStatusItemsUseCase.convertToReceiverStatusItems(receivers, listOfSeen) {
            _receiversStatusItemsLiveData.postValue(it)
        }
    }

    fun getGroupNameById(groupId: String) {
        getGroupUseCase.getGroup(groupId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { groupState ->
            if (groupState is GroupSuccess) {
                val groupName = groupState.group.name ?: ""
                _groupNameLiveData.postValue(groupName)
            } else {
                _groupNameLiveData.postValue("")
            }
        }
    }
}