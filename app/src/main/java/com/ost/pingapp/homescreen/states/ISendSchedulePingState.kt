package com.khrd.pingapp.homescreen.states

interface ISendSchedulePingState

sealed class SendSchedulePingState : ISendSchedulePingState

class SendSchedulePingSuccess : SendSchedulePingState()

class SendSchedulePingFailure : SendSchedulePingState()