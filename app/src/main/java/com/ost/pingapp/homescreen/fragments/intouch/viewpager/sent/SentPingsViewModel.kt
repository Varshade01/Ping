package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.SentPingScheduledItem
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders.GetPingFailure
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders.GetPingsSuccess
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases.ConvertToSentPingItemUseCase
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases.GetScheduledPingsUseCase
import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.usecases.GetSentPingsUseCase
import com.khrd.pingapp.homescreen.sendping.BaseSentPingsError
import com.khrd.pingapp.homescreen.sendping.BaseSentPingsError.ScheduledPingsError
import com.khrd.pingapp.homescreen.sendping.BaseSentPingsError.SentPingsError
import com.khrd.pingapp.homescreen.sendping.SentPingsState
import com.khrd.pingapp.homescreen.sendping.StatusDialogData
import com.khrd.pingapp.homescreen.states.*
import com.khrd.pingapp.homescreen.usecases.pings.DeleteScheduledPingsUseCase
import com.khrd.pingapp.homescreen.usecases.pings.SubscribeToUserChangesUseCase
import com.khrd.pingapp.repository.users.UsersRepository
import com.khrd.pingapp.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SentPingsViewModel @Inject constructor(
    private val deleteScheduledPingsUseCase: DeleteScheduledPingsUseCase,
    private val getSentPingsUseCase: GetSentPingsUseCase,
    private val getScheduledPingsUseCase: GetScheduledPingsUseCase,
    private val convertToSentPingItemUseCase: ConvertToSentPingItemUseCase,
    private val subscribeToUserChangesUseCase: SubscribeToUserChangesUseCase,
    private val firebaseAuth: FirebaseAuthAPI,
    private val usersRepository: UsersRepository
) : ViewModel() {
    private val _cancelPingStateLiveData = MutableLiveData<CancelPingState?>()
    val cancelPingStateLiveData: LiveData<CancelPingState?> get() = _cancelPingStateLiveData

    private val _sentPingsStateLiveData = MutableLiveData<SentPingsState>()
    val sentPingsStateLiveData: LiveData<SentPingsState> get() = _sentPingsStateLiveData

    private val _errorEffectLiveData = MutableLiveData<Event<BaseSentPingsError>>()
    val errorEffectLiveData: LiveData<Event<BaseSentPingsError>> get() = _errorEffectLiveData

    private val _openGroupStatusLiveData = MutableLiveData<Event<StatusDialogData>>()
    val openGroupStatusLiveData: LiveData<Event<StatusDialogData>> get() = _openGroupStatusLiveData

    private var isScheduledLoaded = false
    private var _currentState = SentPingsState()

    private var _sentPings: StateFlow<List<DatabasePing>?> = getSentPingsUseCase.getSentPings()
    private var _scheduledPings: StateFlow<List<DatabasePing>?> = getScheduledPingsUseCase.getScheduledPings()

    private var _usersChangedInScheduledPingsListener: () -> Unit = {
        _scheduledPings.value?.let {
            updateScheduledPingsData(it)
        }
    }
    private var _usersChangedInSentPingsListener: () -> Unit = {
        _sentPings.value?.let {
            updateSentPingsData(it)
        }
    }

    init {
        loadPings(LoadSentPingsAction)
    }

    fun onCancelPing(pingId: String, time: Long) {
        deleteScheduledPingsUseCase.deleteScheduledPings(pingId) {
            when (it) {
                is DeleteScheduledPingSuccess -> {
                    _cancelPingStateLiveData.value = CancelPingSuccess(pingId, time)
                }
                is DeleteScheduledPingFailure -> {
                    _cancelPingStateLiveData.value = CancelPingFailure()
                }
                is DeleteScheduledPingOffline -> {
                    _cancelPingStateLiveData.value = CancelPingOffline(pingId, time)
                }
            }
        }
    }

    private fun loadPings(action: LoadPingsAction) {
        when (action) {
            LoadScheduledPingsAction -> {
                loadScheduledPings()
            }
            LoadSentPingsAction -> {
                loadSentPings()
            }
        }
    }

    fun onRecyclerViewScrolled(action: LoadPingsAction) {
        when (action) {
            LoadScheduledPingsAction -> {
                getScheduledPingsUseCase.loadMoreScheduledPings()
            }
            LoadSentPingsAction -> {
                getSentPingsUseCase.loadMore()
            }
        }
    }

    private fun loadSentPings() {
        viewModelScope.launch {
            _sentPings.collect { pings ->
                pings?.let { listOfPings ->
                    updateSentPingsData(listOfPings)
                    subscribeToUserChangesUseCase.subscribeForUsersChanges(
                        getSetOfReceivers(listOfPings),
                        _usersChangedInSentPingsListener
                    )
                }

            }
        }
    }

    private fun getSetOfReceivers(listOfPings: List<DatabasePing>): List<String> {
        val listOfIds = listOfPings.map { it.receivers.keys.first() }.toSet().toMutableList()
        //Subscribing to current user to observe mute changes
        val currentUser = firebaseAuth.currentUserId()
        if (currentUser != null) {
            listOfIds.add(currentUser)
        }
        return listOfIds
    }

    private fun updateSentPingsData(pings: List<DatabasePing>) {
        convertToSentPingItemUseCase.convertToSentPing(pings) {
            when (it) {
                GetPingFailure -> _errorEffectLiveData.value = Event(SentPingsError)
                is GetPingsSuccess -> {
                    postSentPingState(_currentState.copy(sentItems = it.items.filterIsInstance<SentPingItem>()))
                }
            }
        }
    }

    private fun loadScheduledPings() {
        viewModelScope.launch {
            _scheduledPings.collect { pings ->
                pings?.let { listOfPings ->
                    updateScheduledPingsData(listOfPings)
                    subscribeToUserChangesUseCase.subscribeForUsersChanges(
                        getSetOfReceivers(pings),
                        _usersChangedInScheduledPingsListener
                    )
                }
            }
        }
    }

    private fun updateScheduledPingsData(pings: List<DatabasePing>) {
        convertToSentPingItemUseCase.convertToSentPing(pings) {
            when (it) {
                GetPingFailure -> _errorEffectLiveData.value = Event(ScheduledPingsError)
                is GetPingsSuccess -> {
                    postSentPingState(_currentState.copy(scheduledItems = it.items.filterIsInstance<SentPingScheduledItem>()))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscribeToUserChangesUseCase.unSubscribeForUsersChanges(_usersChangedInScheduledPingsListener)
        subscribeToUserChangesUseCase.unSubscribeForUsersChanges(_usersChangedInSentPingsListener)
    }

    fun onHeaderClicked() {
        if (!isScheduledLoaded) {
            loadPings(LoadScheduledPingsAction)
            isScheduledLoaded = true
        }
        postSentPingState(_currentState.copy(showScheduled = !_currentState.showScheduled))
    }

    private fun postSentPingState(state: SentPingsState) {
        _currentState = state
        _sentPingsStateLiveData.postValue(_currentState)
    }

    fun onPhotoClicked(item: SentPingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            getCurrentUser(firebaseAuth.currentUserId())?.let {
                _openGroupStatusLiveData.postValue(Event(StatusDialogData(item, it)))
            }
        }
    }

    private suspend fun getCurrentUser(userId: String?): DatabaseUser? {
        if (!userId.isNullOrBlank()) {
            val requestState = usersRepository.getUserSuspend(userId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
            if (requestState is UserRequestState.UserRequestSuccess) {
                return requestState.user
            }
        }
        return null
    }
}