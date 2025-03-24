package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.received

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.DisplayablePingItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.ReceivedPingItem
import com.khrd.pingapp.homescreen.states.LoadReceivedPingsState
import com.khrd.pingapp.homescreen.usecases.pings.ChangePingSeenStatusUseCase
import com.khrd.pingapp.homescreen.usecases.pings.SubscribeToUserChangesUseCase
import com.khrd.pingapp.homescreen.usecases.pings.received.ConvertToReceivedPingUseCase
import com.khrd.pingapp.homescreen.usecases.pings.received.LoadReceivedPingsUseCase
import com.khrd.pingapp.repository.pings.ReceivedPingsData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReceivedPingsViewModel @Inject constructor(
    private val loadReceivedPingsUseCase: LoadReceivedPingsUseCase,
    private val convertToReceivedPingUseCase: ConvertToReceivedPingUseCase,
    private val changePingSeenStatusUseCase: ChangePingSeenStatusUseCase,
    private val subscribeToUserChangesUseCase: SubscribeToUserChangesUseCase,
    private val firebaseAuth: FirebaseAuthAPI
) : ViewModel() {

    private val _receivedPingsLiveData = MutableLiveData<LoadReceivedPingsState>()
    val receivedPingsLiveData: LiveData<LoadReceivedPingsState> get() = _receivedPingsLiveData

    private val processedIdsOnViewed = mutableListOf<String>()

    private var _pingsRepositoryFlow: StateFlow<ReceivedPingsData?> = loadReceivedPingsUseCase.loadReceivedPings()

    private var _usersChangedInReceivedPingsListener: () -> Unit = {
        _pingsRepositoryFlow.value?.let {
            updateData(it)
        }
    }

    init {
        viewModelScope.launch {
            _pingsRepositoryFlow.collect { pingsData ->
                if (pingsData == null) {
                    return@collect
                }
                updateData(pingsData)
                subscribeToUserChangesUseCase.subscribeForUsersChanges(
                    getIdsOfReceivers(pingsData),
                    _usersChangedInReceivedPingsListener
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscribeToUserChangesUseCase.unSubscribeForUsersChanges(_usersChangedInReceivedPingsListener)
    }

    private fun updateData(pingsData: ReceivedPingsData) {
        convertToReceivedPingUseCase.convertToReceivedPing(
            pingsData.listOfPings,
            DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE
        ) { state ->
            _receivedPingsLiveData.value = state
        }
    }

    fun onRecyclerViewScrolledToLast() {
        loadReceivedPingsUseCase.loadMoreReceivedPings()
    }

    fun onRecyclerViewScrolledSetViewed(items: List<DisplayablePingItem>) {
        items.forEach { item ->
            if (!(item as ReceivedPingItem).seen && !processedIdsOnViewed.contains(item.id)) {
                processedIdsOnViewed.add(item.id)
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        changePingSeenStatusUseCase.changePingSeenStatus(item.id)
                    }
                }, 2000)
            }
        }
    }

    private fun getIdsOfReceivers(pingsData: ReceivedPingsData): List<String> {
        val listOfIds = pingsData.listOfPings.map { it.from.keys.first() }.toSet().toMutableList()
        //Subscribing to current user to observe mute changes
        val currentUser = firebaseAuth.currentUserId()
        if (currentUser != null){
            listOfIds.add(currentUser)
        }
        return listOfIds
    }
}