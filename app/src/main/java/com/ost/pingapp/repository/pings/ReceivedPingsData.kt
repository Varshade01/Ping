package com.khrd.pingapp.repository.pings

import com.khrd.pingapp.data.pings.DatabasePing


data class ReceivedPingsData(val listOfPings: List<DatabasePing>,val groupId: String? = null)