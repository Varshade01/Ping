package com.khrd.pingapp.pushnotification

import com.khrd.pingapp.constants.PushConstants.NEW_PING
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.useradministration.usecases.GetUsersByIdUseCase
import javax.inject.Inject

class CreatePushDataUseCaseImpl @Inject constructor(
    val getUsersByIdUseCase: GetUsersByIdUseCase
) : CreatePushDataUseCase {
    override fun createPushData(ping: DatabasePing, callback: (CreatePushState) -> Unit) {
        val receivers: List<String> = ping.receivers.keys.toList()
        getUsersByIdUseCase.getUsers(receivers, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) {
            if (it.isNullOrEmpty()) {
                callback(CreatePushFailure())
            } else {
                create(ping, it, receivers, callback)
            }
        }
    }

    private fun create(
        ping: DatabasePing,
        groupMembers: List<DatabaseUser>,
        receivers: List<String>,
        callback: (CreatePushState) -> Unit
    ) {
        val receiversTokens = mutableListOf<String>()
        groupMembers.forEach { user ->
            user.fcmTokens?.values?.toList()?.let { it1 -> receiversTokens.addAll(it1) }
        }
        callback(CreatePushSuccess(PushNotification(receiversTokens, PushData(NEW_PING, ping.id))))
    }
}
