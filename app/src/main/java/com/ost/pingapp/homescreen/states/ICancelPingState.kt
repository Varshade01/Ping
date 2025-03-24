package com.khrd.pingapp.homescreen.states

interface ICancelPingState

sealed class CancelPingState : ICancelPingState

data class CancelPingSuccess(val pingId: String, val time: Long) : CancelPingState()

class CancelPingFailure : CancelPingState()

data class CancelPingOffline(val pingId: String, val time: Long): CancelPingState()