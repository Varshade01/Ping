package com.khrd.pingapp.pushnotification

interface ICreatePushState

sealed class CreatePushState : ICreatePushState

data class CreatePushSuccess(val pushNotification: PushNotification) : CreatePushState()

class CreatePushFailure : CreatePushState()