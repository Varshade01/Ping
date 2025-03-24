package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.pings.DatabasePing

interface IRewriteScheduledPingsState

sealed class RewriteScheduledPingsState: IRewriteScheduledPingsState

data class RewriteScheduledPingsSuccess(val databasePing: DatabasePing):RewriteScheduledPingsState()

class RewriteScheduledPingsFailure():RewriteScheduledPingsState()