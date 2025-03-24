package com.khrd.pingapp.homescreen.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.khrd.pingapp.HomescreenNavGraphDirections
import com.khrd.pingapp.data.groups.DatabaseGroup
import com.khrd.pingapp.homescreen.adapter.UserItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MultipleUsersViewModel @Inject constructor() : ViewModel() {

    private val _multipleUsersLivedata = MutableLiveData<List<UserItem>>()
    val multipleUsersLivedata: LiveData<List<UserItem>> get() = _multipleUsersLivedata
    private var group: DatabaseGroup? = null

    fun init(userItems: List<UserItem>, group: DatabaseGroup) {
        this.group = group
        _multipleUsersLivedata.value = userItems
    }

    fun openSendPingDialog(findNavController: NavController) {
        findNavController.navigateUp()
        val action =
            HomescreenNavGraphDirections.openSendPingDialog(
                users = _multipleUsersLivedata.value?.mapNotNull { it.userId }?.toTypedArray(),
                group = group?.id
            )
        findNavController.navigate(action)
    }
}