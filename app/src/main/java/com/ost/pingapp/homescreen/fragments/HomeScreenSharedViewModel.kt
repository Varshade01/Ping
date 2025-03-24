package com.khrd.pingapp.homescreen.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.khrd.pingapp.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeScreenSharedViewModel @Inject constructor() : ViewModel() {

    private val _eventLiveData = MutableLiveData<Event<Unit>>()
    val eventLiveData: LiveData<Event<Unit>> get() = _eventLiveData

    fun navigateToGroupTab() {
        _eventLiveData.postValue(Event(Unit))
    }
}