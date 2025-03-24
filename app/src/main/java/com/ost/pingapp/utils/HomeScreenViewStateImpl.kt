package com.khrd.pingapp.utils

import android.view.View

class HomeScreenViewStateImpl : HomeScreenViewState {
    private var groupView: View? = null
    private var groupExpandedState: Boolean = false

    override fun saveGroupView(view: View?) {
        groupView = view
    }

    override fun restoreGroupView(): View? = groupView

    override fun setGroupSearchViewExpandedState(state: Boolean) {
        groupExpandedState = state
    }

    override fun getGroupSearchViewExpandedState(): Boolean = groupExpandedState

    override fun clearState() {
        groupView = null
        groupExpandedState = false
    }

}