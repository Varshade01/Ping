package com.khrd.pingapp.utils

import android.content.Context
import com.khrd.pingapp.datastore.DataStoreManager
import kotlinx.coroutines.runBlocking
import java.util.*

class LocaleHelperImpl(val dataStoreManager: DataStoreManager) : LocaleHelper {

    override fun initLocale(context: Context): Context = updateResources(context, getCurrentLocale())

    override suspend fun setLocale(locale: String) {
        dataStoreManager.saveCurrentLocale(locale)
    }

    override fun getCurrentLocale(): String = runBlocking { dataStoreManager.getCurrentLocale() ?: Locale.getDefault().language }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }
}