package com.khrd.pingapp.utils

import android.content.Context

interface LocaleHelper {
    fun initLocale(context: Context): Context
    suspend fun setLocale(locale: String)
    fun getCurrentLocale(): String
}