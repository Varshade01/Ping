package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.pings.DatabasePing

interface IScheduledPingsState

sealed class ScheduledPingsState : IScheduledPingsState

data class ScheduledPingsSuccess(val pings:List<DatabasePing>) : ScheduledPingsState()

class ScheduledPingsFailure : ScheduledPingsState()