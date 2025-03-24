package com.khrd.pingapp.homescreen.states

import com.khrd.pingapp.data.pings.DatabasePing

interface ISendScheduledPingsAfterRebootState

sealed class SendScheduledPingsAfterRebootState : ISendScheduledPingsAfterRebootState

class SendScheduledPingsAfterRebootSuccess(val databasePing: DatabasePing) : SendScheduledPingsAfterRebootState()

class SendScheduledPingsAfterRebootFailure : SendScheduledPingsAfterRebootState()