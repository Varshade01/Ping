package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent

sealed class LoadPingsAction

object LoadSentPingsAction : LoadPingsAction()

object LoadScheduledPingsAction: LoadPingsAction()
