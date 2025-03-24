package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.pings.DatabasePing

interface ICreatePingState

sealed class CreatePingState : ICreatePingState

data class CreatePingSuccess(val pings:List<DatabasePing>) : CreatePingState()

class CreatePingFailure : CreatePingState()

object CreatePingOfflineState: CreatePingState()