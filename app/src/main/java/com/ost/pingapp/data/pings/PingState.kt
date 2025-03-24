package com.khrd.pingapp.data.pings

interface IPingState

sealed class PingState : IPingState

data class PingStateSuccess(val pings: List<DatabasePing>) : PingState()

object PingStateFailure : PingState()

object PingOfflineState : PingState()
