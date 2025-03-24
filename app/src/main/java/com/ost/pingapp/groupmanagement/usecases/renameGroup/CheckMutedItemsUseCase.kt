package com.khrd.pingapp.groupmanagement.usecases.renameGroup

import com.khrd.pingapp.data.pings.DatabasePing

interface CheckMutedItemsUseCase {
    fun containsMutedItems(databasePing: DatabasePing): Boolean
    suspend fun containsMutedGroup(groupItem: String?): Boolean
}