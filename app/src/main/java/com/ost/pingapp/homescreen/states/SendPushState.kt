package com.khrd.pingapp.homescreen.states

interface ISendPushState

sealed class SendPushState : ISendPushState

class SendPushSuccess : SendPushState()

class SendPushFailure : SendPushState()