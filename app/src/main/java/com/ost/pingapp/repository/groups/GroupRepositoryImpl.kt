package com.khrd.pingapp.repository.groups

import android.os.ConditionVariable
import com.khrd.pingapp.data.database.groups.GroupDao
import com.khrd.pingapp.data.database.groups.GroupMapper
import com.khrd.pingapp.data.groups.*
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val firebaseGroupsDataSource: GroupsDataSource,
    private val groupDao: GroupDao,
    private val mapper: GroupMapper,
    private val firebaseConnectionStatus: ConnectionStatus,
    @IoCoroutineScope private val ioCoroutineScope: CoroutineScope
) : GroupRepository {

    private var cachedGroups = linkedSetOf<DatabaseGroup>()

    private val groupConditionVariable = ConditionVariable()
    private val groupByLinkConditionVariable = ConditionVariable()
    private val getGroupThreadExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val getGroupByLinkThreadExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun createGroup(uid: String, name: String?, callback: (GroupState) -> Unit) {
        firebaseGroupsDataSource.createGroup(uid, name) { state ->
            ioCoroutineScope.launch {
                if (state is GroupSuccess) {
                    groupDao.insertGroupToDb(mapper.mapToEntity(state.group))
                }
                withContext(Dispatchers.Main) { callback(state) }
            }
        }
    }

    override fun removeGroup(id: String, callback: (GroupState) -> Unit) {
        firebaseGroupsDataSource.removeGroup(id) { state ->
            ioCoroutineScope.launch {
                if (state is GroupSuccess) {
                    groupDao.deleteGroup(id)
                }
                withContext(Dispatchers.Main) { callback(state) }
            }
        }
    }

    override fun updateGroupName(groupId: String, name: String, callback: (GroupState) -> Unit) {
        firebaseGroupsDataSource.updateGroupName(groupId, name) { state ->
            if (state is GroupSuccess) {
                val newName = state.group.name ?: ""
                updateCachedGroupName(
                    groupId,
                    newName
                ) //should we save whole cached group instead of separate parts like name, image and so on?
            }
            callback(state)
        }
    }

    override fun addInvitationLinkToGroup(id: String, link: String, callback: (GroupState) -> Unit) {
        firebaseGroupsDataSource.addInvitationLinkToGroup(id, link) { state -> callback(state) }
    }

    override fun getGroup(id: String, loadFlag: DataLoadFlag, callback: (GroupState) -> Unit) {
        ioCoroutineScope.launch(getGroupThreadExecutor) {
            val cachedGroup = cachedGroups.find { it.id == id }
            val loadFromServer = loadFlag == DataLoadFlag.LOAD_FROM_SERVER ||
                    (loadFlag == DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE && cachedGroup == null)
            if (loadFromServer && firebaseConnectionStatus.getConnectionStatus()) {
                firebaseGroupsDataSource.getGroup(id) { state ->
                    ioCoroutineScope.launch {
                        if (state is GroupSuccess) {
                            if (cachedGroups.find { it.id == state.group.id } != state.group) {
                                cachedGroups.add(state.group)
                                groupDao.insertGroupToDb(mapper.mapToEntity(state.group))
                            }
                        } else if (state is GroupFailure && state.error == GroupError.UNEXISTING_GROUP) {
                            val group = cachedGroups.find { it.id == id }
                            if (group != null) {
                                cachedGroups.remove(group)
                            }
                        }

                        withContext(Dispatchers.Main) { callback(state) }
                        groupConditionVariable.open()
                    }
                }
                groupConditionVariable.close()
                groupConditionVariable.block()
            } else {
                if (cachedGroup != null) {
                    val group = cachedGroups.find { it.id == id }
                    withContext(Dispatchers.Main) { callback(GroupSuccess(group!!)) }
                } else {
                    val entity = groupDao.getGroupFromDb(id)
                    entity?.let {
                        val group = mapper.mapToFirebaseModel(it)
                        cachedGroups.add(group)
                        withContext(Dispatchers.Main) { callback(GroupSuccess(group)) }
                    } ?: withContext(Dispatchers.Main) { callback(GroupFailure(GroupError.NETWORK_ERROR)) }
                }
            }
        }
    }

    override fun getGroupByLink(link: String, callback: (GroupState) -> Unit) {
        ioCoroutineScope.launch(getGroupByLinkThreadExecutor) {
            //check if group is in cache
            val groupIsCached = cachedGroups.any { it.invitationLink == link }
            //if group is not in cache & connection status true -> load from server
            if (!groupIsCached && firebaseConnectionStatus.getConnectionStatus()) {
                firebaseGroupsDataSource.getGroupByLink(link) { state ->
                    ioCoroutineScope.launch {
                        if (state is GroupSuccess) {
                            //
                            if (cachedGroups.find { it.id == state.group.id } != state.group) {
                                cachedGroups.add(state.group)
                                groupDao.insertGroupToDb(mapper.mapToEntity(state.group))
                            }
                        } else if (state is GroupFailure && state.error == GroupError.UNEXISTING_GROUP) {
                            val group = cachedGroups.find { it.invitationLink == link }
                            if (group != null) {
                                cachedGroups.remove(group)
                            }
                        }
                        withContext(Dispatchers.Main) { callback(state) }
                    }
                }
            } else {
                //if group is in cache -> check if invitationLink is in, if true -> success, otherwise -> unexisting group
                if (groupIsCached) {
                    cachedGroups.find { it.invitationLink == link }?.let {
                        callback(GroupSuccess(it))
                    } ?: callback(GroupFailure(GroupError.UNEXISTING_GROUP))
                } else {
                    val entity = groupDao.getGroupFromDbByLink(link)
                    entity?.let {
                        val group = mapper.mapToFirebaseModel(it)
                        cachedGroups.add(group)
                        withContext(Dispatchers.Main) { callback(GroupSuccess(group)) }
                    } ?: withContext(Dispatchers.Main) { callback(GroupFailure(GroupError.NETWORK_ERROR)) }
                }
            }
        }
    }

    override fun getGroups(groupIds: List<String>, loadFlag: DataLoadFlag, callback: (List<DatabaseGroup>) -> Unit) {
        ioCoroutineScope.launch {
            val conditionVariable = ConditionVariable()
            val result = mutableListOf<DatabaseGroup>()

            groupIds.forEach { id ->
                conditionVariable.close()
                getGroup(id, loadFlag) { state ->
                    when (state) {
                        is GroupFailure -> {
                        }
                        is GroupOfflineState -> {
                        }
                        is GroupSuccess -> {
                            result.add(state.group)
                        }
                    }
                    conditionVariable.open()
                }
                conditionVariable.block()
            }
            withContext(Dispatchers.Main) { callback(result) }
        }
    }

    override fun updateGroupImage(groupId: String, bytes: ByteArray, callback: (GroupState) -> Unit) {
        firebaseGroupsDataSource.updateGroupImage(groupId, bytes) { state ->
            ioCoroutineScope.launch {
                if (state is GroupSuccess) {
                    val newImageUrl = state.group.photoURL ?: ""
                    groupDao.updateGroupImage(groupId, newImageUrl)
                    cachedGroups.find { groupId == it.id }?.photoURL = newImageUrl
                }
                withContext(Dispatchers.Main) { callback(state) }
            }
        }
    }

    override fun updateCachedGroupName(groupId: String, name: String) {
        ioCoroutineScope.launch {
            groupDao.updateGroupName(groupId, name)
            cachedGroups.find { groupId == it.id }?.name = name
        }
    }
}
