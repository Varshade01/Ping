package com.khrd.pingapp.groupmanagement.usecases.renameGroup

import android.os.ConditionVariable
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.pings.DatabasePing
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.homescreen.states.GetUserSuccess
import com.khrd.pingapp.homescreen.usecases.GetUserUseCase
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CheckMutedItemsUseCaseImpl(
    private val firebaseAuth: FirebaseAuthAPI,
    private val getUserUseCase: GetUserUseCase,
) : CheckMutedItemsUseCase {
    override fun containsMutedItems(databasePing: DatabasePing): Boolean {
        val mutedItems = provideMutedItems()
        val pingsFrom = databasePing.from.keys.toList()
        return pingsFrom.intersect(mutedItems.toSet()).isNotEmpty()
                ||(!databasePing.groupId.isNullOrEmpty()
                &&mutedItems.contains(databasePing.groupId))
    }

    override suspend fun containsMutedGroup(groupItem: String?): Boolean {
        val mutedItems: List<String> = suspendCoroutine { cont ->
            getMutedItems {
                cont.resume(it)
            }
        } ?: listOf()
        return mutedItems.contains(groupItem)
    }

    private fun provideMutedItems(): List<String> {
        val conditionVariable = ConditionVariable()
        var mutedItems = listOf<String>()
        getMutedItems {
            it?.let { mutedItems = it }
            conditionVariable.open()
        }
        conditionVariable.block()
        return mutedItems
    }

    private fun getMutedItems(callback: (List<String>?) -> Unit) {
        val result = mutableListOf<String>()
        val currentUserId = firebaseAuth.currentUserId()

        getUserUseCase.getUser(currentUserId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE) { userState ->
            when (userState) {
                is GetUserSuccess -> {
                    userState.user.mutedItems?.keys?.let { it -> result.addAll(it) }
                    callback(result)
                }
                else -> {
                    callback(null)
                }
            }
        }
    }
}