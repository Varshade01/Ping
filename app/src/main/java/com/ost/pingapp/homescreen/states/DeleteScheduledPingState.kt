package com.khrd.pingapp.homescreen.states

interface IDeleteScheduledPingState

sealed class DeleteScheduledPingState:IDeleteScheduledPingState

class DeleteScheduledPingSuccess(): DeleteScheduledPingState()

class DeleteScheduledPingFailure(): DeleteScheduledPingState()

class DeleteScheduledPingOffline(): DeleteScheduledPingState()
