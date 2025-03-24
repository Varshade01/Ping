package com.khrd.pingapp.utils.viewstate

sealed class ViewState {
    data class ReceivedPingsViewState(val firstCompletelyVisibleItemIndex: Int) : ViewState()
    object DefaultViewState : ViewState()
}