package com.khrd.pingapp.utils.messenger

import android.content.Context
import com.khrd.pingapp.data.groups.GroupSuccess
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.datastore.DataStoreManager
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.groupmanagement.usecases.GetGroupUseCase
import com.khrd.pingapp.groupmanagement.usecases.renameGroup.CheckMutedItemsUseCase
import com.khrd.pingapp.homescreen.states.GetUserSuccess
import com.khrd.pingapp.homescreen.usecases.GetUserUseCase
import com.khrd.pingapp.homescreen.usecases.pings.received.LoadReceivedPingsUseCase
import com.khrd.pingapp.repository.pings.ReceivedPingsData
import com.khrd.pingapp.utils.ToastUtils
import com.khrd.pingapp.utils.viewstate.AppViewState
import com.khrd.pingapp.utils.viewstate.ViewState.ReceivedPingsViewState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppReceivedPingsMessengerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoCoroutineScope private val ioCoroutineScope: CoroutineScope,
    private val appViewState: AppViewState,
    private val toastUtils: ToastUtils,
    private val loadReceivedPingsUseCase: LoadReceivedPingsUseCase,
    private val getUserUseCase: GetUserUseCase,
    private val getGroupUseCase: GetGroupUseCase,
    private val checkMutedItemsUseCase: CheckMutedItemsUseCase,
    private val auth: FirebaseAuthAPI,
    private val dataStoreManager: DataStoreManager,
) : AppReceivedPingsMessenger {

    private var isListening = false

    override fun startListening() {
        if (!isListening) {
            ioCoroutineScope.launch {
                loadReceivedPingsUseCase.loadReceivedPings().collect { pingsData ->
                    if (pingsData == null) {
                        return@collect
                    }
                    handlePingsData(pingsData)
                }
            }
        }
    }

    private suspend fun handlePingsData(pingsData: ReceivedPingsData) {
        val ping = pingsData.listOfPings.find { it.groupId == pingsData.groupId }

        ping?.let { databasePing ->
            if (needToShowToast(databasePing)) {
                ioCoroutineScope.launch {
                    dataStoreManager.setShownPing(databasePing.id)
                    handleToasts(databasePing)
                }
            }
        }
    }

    private fun handleToasts(databasePing: DatabasePing) {
        if (isGroupPing(databasePing)) {
            showToastWithGroup(databasePing)
        } else {
            showToastWithUser(databasePing)
        }
    }

    private fun isGroupPing(databasePing: DatabasePing) = databasePing.groupId != null

    private suspend fun needToShowToast(databasePing: DatabasePing): Boolean {
        val viewState = appViewState.viewState
        val isVisibleState = viewState !is ReceivedPingsViewState || viewState.firstCompletelyVisibleItemIndex != 0
        val containsMutedItems = checkMutedItemsUseCase.containsMutedItems(databasePing)
        val isRead = databasePing.views.containsKey(auth.currentUserId())
        val isAlreadyShown = withContext(Dispatchers.IO) { dataStoreManager.getLastShownPing() == databasePing.id }
        return !containsMutedItems && isVisibleState && appViewState.isAppVisible && !isRead && !isAlreadyShown
    }

    private fun showToastWithUser(ping: DatabasePing) {
        getUserUseCase.getUser(ping.from.keys.first(), DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { state ->
            when (state) {
                is GetUserSuccess -> {
                    toastUtils.showReceivedPingToast(ping.message, state.user.username)
                }

                else -> {}
            }
        }
    }

    private fun showToastWithGroup(ping: DatabasePing) {
        getGroupUseCase.getGroup(ping.groupId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { state ->
            when (state) {
                is GroupSuccess -> {
                    toastUtils.showReceivedPingToast(ping.message, state.group.name)
                }

                else -> {}
            }
        }
    }
}