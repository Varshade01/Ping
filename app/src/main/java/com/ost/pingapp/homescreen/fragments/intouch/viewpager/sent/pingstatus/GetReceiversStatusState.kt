package com.khrd.pingapp.homescreen.fragments.intouch.viewpager.sent.pingstatus

sealed interface GetReceiversStatusState

data class GetReceiversStatusSuccess(val items: List<ReceiverStatusItem>) : GetReceiversStatusState

object GetReceiversStatusFailure : GetReceiversStatusState

sealed interface GetUserMutedStatusState

data class GetUserMutedStatusSuccess(val isMuted: Boolean) : GetUserMutedStatusState

object GetUserMutedStatusFailure : GetUserMutedStatusState