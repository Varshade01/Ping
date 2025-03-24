package com.khrd.pingapp.utils

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocaleHelperEntryPoint {
    val localeHelper: LocaleHelper
}