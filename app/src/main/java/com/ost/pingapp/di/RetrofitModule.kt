package com.khrd.pingapp.di

import com.khrd.pingapp.constants.PushConstants
import com.khrd.pingapp.pushnotification.HeaderInterceptor
import com.khrd.pingapp.pushnotification.SendPushService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(HeaderInterceptor())
        val client = httpClient.build()
        return Retrofit.Builder()
            .baseUrl(PushConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    @Singleton
    @Provides
    fun provideSendPushService(retrofit: Retrofit): SendPushService = retrofit.create(SendPushService::class.java)
}