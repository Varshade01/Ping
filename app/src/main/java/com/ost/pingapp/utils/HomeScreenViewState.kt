package com.khrd.pingapp.utils

import android.view.View

interface HomeScreenViewState {
    fun saveGroupView(view: View?)
    fun restoreGroupView(): View?
    fun setGroupSearchViewExpandedState(state: Boolean)
    fun getGroupSearchViewExpandedState(): Boolean
    fun clearState()
}