package com.khrd.pingapp.pushnotification

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SendPushService {
    @POST("fcm/send")
    suspend fun sendPushNotification(@Body data: PushNotification?): Response<ResponseBody>
}