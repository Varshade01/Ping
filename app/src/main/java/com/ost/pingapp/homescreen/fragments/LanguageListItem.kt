package com.khrd.pingapp.homescreen.fragments

import androidx.annotation.StringRes

data class LanguageListItem(@StringRes val languageName: Int, val imageRes: Int, val locale: String, val isSelected : Boolean)
