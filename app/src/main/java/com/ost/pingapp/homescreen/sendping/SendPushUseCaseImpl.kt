package com.khrd.pingapp.homescreen.sendping

import android.util.Log
import com.khrd.pingapp.homescreen.states.SendPushFailure
import com.khrd.pingapp.homescreen.states.SendPushState
import com.khrd.pingapp.homescreen.states.SendPushSuccess
import com.khrd.pingapp.pushnotification.PushNotification
import com.khrd.pingapp.pushnotification.SendPushService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SendPushUseCaseImpl @Inject constructor(
    val sendPushService: SendPushService
) : SendPushUseCase {
    override suspend fun sendPush(body: PushNotification): SendPushState = withContext(Dispatchers.IO) {
        val response = sendPushService.sendPushNotification(body)
        if (response.isSuccessful) {
            Log.d("*****", "Response success ${response.body().toString()}")
            return@withContext SendPushSuccess()
        } else {
            Log.d("*****", "Response failure ${response.errorBody().toString()}")
            return@withContext SendPushFailure()
        }
    }
}