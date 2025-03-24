package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.viewholders

import com.khrd.pingapp.homescreen.fragments.intouch.viewpager.adapter.DisplayablePingItem

sealed class GetPingsState

data class GetPingsSuccess(val items: List<DisplayablePingItem>, val offset: String) : GetPingsState()

object GetPingFailure : GetPingsState()
