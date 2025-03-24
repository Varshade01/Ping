package com.khrd.pingapp.pushnotification

import com.khrd.pingapp.constants.DbConstants.SERVER_KEY
import com.khrd.pingapp.constants.PushConstants.CONTENT_TYPE
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request()
                .newBuilder()
                .addHeader("Authorization", "key=${SERVER_KEY.decode()}")
                .addHeader("Content-Type", CONTENT_TYPE)
                .build()
        )
    }
}