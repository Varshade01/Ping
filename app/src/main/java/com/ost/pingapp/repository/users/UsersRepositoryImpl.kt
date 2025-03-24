package com.khrd.pingapp.repository.users

import android.os.Build
import android.os.ConditionVariable
import androidx.annotation.RequiresApi
import com.khrd.pingapp.data.database.users.UserDao
import com.khrd.pingapp.data.database.users.UserMapper
import com.khrd.pingapp.data.pings.DataLoadFlag
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.Online
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.data.users.UsersDataSource
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.listeners.UsersListener
import com.khrd.pingapp.utils.deviceIdHelper.DeviceIdHelper
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UsersRepositoryImpl @Inject constructor(
    private val firebaseUsersDatabase: UsersDataSource,
    private val userMapper: UserMapper,
    private val userDao: UserDao,
    private val firebaseConnectionStatus: ConnectionStatus,
    @IoCoroutineScope private val ioCoroutineScope: CoroutineScope,
    private val deviceIdHelper: DeviceIdHelper,
) : UsersRepository {

    private var cachedUsers: MutableList<DatabaseUser> = Collections.synchronizedList(mutableListOf())
    private var subscribedForServerChanges = hashMapOf<String, Boolean>()
    private var usersAreLoaded = hashMapOf<String, Boolean>()

    private val userConditionVariable = ConditionVariable()
    private val getUserThreadExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val getGroupMembersThreadExecutor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private var usersListeners = mutableMapOf<String, MutableSet<UsersListener>>()

    private val dataSourceUsersListener = object : UsersListener() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onUsersChanged(groupId: String, users: List<DatabaseUser>?) {
            ioCoroutineScope.launch {
                val changedUsers = mutableListOf<String?>()
                users?.let {
                    deleteRedundantGroupId(users, groupId)
                    updateCachedUsers(users, changedUsers)
                    usersAreLoaded[groupId] = true
                }
                withContext(Dispatchers.Main) {
                    fireChangedUsersData(groupId)
                }
                mapOfObservableUsers.forEach { listener, usersids ->
                    if (usersids.intersect(changedUsers).isNotEmpty()) {
                        listener.invoke()
                    }
                }
                //getting list of group ids which are the same for all users in input list
                userConditionVariable.open()

            }
        }
    }

    private var mapOfObservableUsers = mutableMapOf<() -> Unit, List<String>>()

    override fun subscribeForUsersChanges(usersIds: List<String>, listener: () -> Unit) {
        mapOfObservableUsers[listener] = usersIds
    }

    override fun unSubscribeUsersChanges(listener: () -> Unit) {
        mapOfObservableUsers.remove(listener)
    }

    init {
        firebaseUsersDatabase.setListener(dataSourceUsersListener)
    }

    override suspend fun createUser(id: String, email: String, username: String): UserRequestState {
        val state = getUserSuspend(id, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
        return if (state is UserRequestState.UserRequestSuccess && state.user != null) {
            UserRequestState.UserRequestSuccess(state.user)
        } else {
            firebaseUsersDatabase.createUser(id, email, username)
        }
    }

    override suspend fun getUserSuspend(userId: String, loadFlag: DataLoadFlag): UserRequestState {
        return suspendCoroutine { cont ->
            getUser(userId, loadFlag) {
                cont.resume(UserRequestState.UserRequestSuccess(cachedUsers.find { it.id == userId }))
            }
        }
    }

    // Returns user
    override fun getUser(userId: String, loadFlag: DataLoadFlag, callback: (UserRequestState) -> Unit) {
        ioCoroutineScope.launch(getUserThreadExecutor) {
            val loadFromServer = loadFlag == DataLoadFlag.LOAD_FROM_SERVER ||
                    (loadFlag == DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE && !cachedUsers.map { it.id }.contains(userId))

            val isNetworkConnectionAvailable = firebaseConnectionStatus.getConnectionStatus()
            val loadFromCache = cachedUsers.map { it.id }.contains(userId)

            if (isNetworkConnectionAvailable && loadFromServer) {
                firebaseUsersDatabase.getUser(userId) { state ->
                    ioCoroutineScope.launch {
                        if (state is UserRequestState.UserRequestSuccess) {
                            state.user?.let { updateCachedUsers(listOf(it)) }
                        }
                        withContext(Dispatchers.Main) { callback(state) }
                    }
                    userConditionVariable.open()
                }
                userConditionVariable.close()
                userConditionVariable.block()
            } else if (loadFromCache) {
                withContext(Dispatchers.Main) {
                    callback(UserRequestState.UserRequestSuccess(cachedUsers.find { it.id == userId }))
                }
            } else {
                userDao.getUsersByID(userId)?.let { userEntity ->
                    val databaseUser = userMapper.mapDatabaseEntityToFirebaseUser(userEntity)
                    updateCachedUsers(listOf(databaseUser), saveToDB = false)
                    withContext(Dispatchers.Main) { callback(UserRequestState.UserRequestSuccess(cachedUsers.find { it.id == userId })) }
                } ?: withContext(Dispatchers.Main) { callback(UserRequestState.UserRequestFail) }
            }
        }
    }

    override fun getUsers(userIds: List<String>, loadFlag: DataLoadFlag, callback: (List<DatabaseUser>) -> Unit) {
        ioCoroutineScope.launch {
            val conditionVariable = ConditionVariable()
            val result = mutableListOf<DatabaseUser>()

            userIds.forEach { userId ->
                conditionVariable.close()
                getUser(userId, loadFlag) { state ->
                    when (state) {
                        is UserRequestState.UserRequestFail -> {
                        }
                        is UserRequestState.UserRequestOffline -> {
                            state.user?.let { result.add(it) }
                        }
                        is UserRequestState.UserRequestSuccess -> {
                            state.user?.let { result.add(it) }
                        }
                    }
                    conditionVariable.open()
                }
                conditionVariable.block()
            }
            withContext(Dispatchers.Main) { callback(result) }
        }
    }

    // Returns user, but email is null
    override fun updateUsername(id: String, username: String, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.updateUsername(id, username) { state ->
            ioCoroutineScope.launch {
                when (state) {
                    is UserRequestState.UserRequestSuccess, is UserRequestState.UserRequestOffline -> {
                        userDao.updateName(
                            userId = id,
                            userName = username
                        )

                        //cached users update after user name changing
                        updateUsernameCache(id, username)

                    }
                    is UserRequestState.UserRequestFail -> {
                    }
                }
                withContext(Dispatchers.Main) { callback(state) }
            }

        }
    }

    override fun updateUserJob(id: String, job: String, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.updateUserJob(id, job) { state ->
            ioCoroutineScope.launch {
                if (state is UserRequestState.UserRequestSuccess) {
                    userDao.updateJob(
                        userId = id,
                        updatedJob = job
                    )
                    //cached users update after user name changing
                    updateUserJobInCache(id, job)
                }
                withContext(Dispatchers.Main) { callback(state) }
            }
        }
    }

    override fun updateUserOnlineStatus(id: String, status: Online) {
        ioCoroutineScope.launch {
            if (firebaseConnectionStatus.retrieveConnectionStatus()) {
                firebaseUsersDatabase.updateUserOnlineStatus(id, deviceIdHelper.getDeviceId(), status)
            }
        }
    }

    override fun updateUserJobInCache(id: String, job: String) {
        val tempUser = cachedUsers.find { it.id == id }
        if (tempUser != null) {
            tempUser.job = job
        }
    }

    override fun updateUsernameCache(id: String, username: String) {
        val tempUser = cachedUsers.find { it.id == id }
        if (tempUser != null) {
            tempUser.username = username
        }
    }

    // Returns null
    override fun deleteUser(id: String, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.deleteUser(id) { state ->
            ioCoroutineScope.launch {
                when (state) {
                    is UserRequestState.UserRequestSuccess -> {
                        val deletedUser = state.user?.let { userMapper.mapFirebaseResponseToDatabaseEntity(it) }
                        deletedUser?.let { userDao.addUser(it) }
                    }
                    is UserRequestState.UserRequestOffline -> {
                    }
                    is UserRequestState.UserRequestFail -> {
                    }
                }
                withContext(Dispatchers.Main) { callback(state) }
            }
        }
    }

    override fun deleteUserGroup(id: String, groupId: String, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.deleteUserGroup(id, groupId) { state ->
            if (state is UserRequestState.UserRequestSuccess) {
                cachedUsers.find { it.id == id }?.groups?.remove(groupId)
            }
            callback(state)
        }
    }

    override fun addUserGroup(id: String, groupId: String, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.addUserGroup(id, groupId) { state ->
            ioCoroutineScope.launch {
                if (state is UserRequestState.UserRequestSuccess) {
                    //find user in cache
                    val cachedUser = cachedUsers.find { it.id == id }
                    cachedUser?.let {
                        //new groups from state, if null -> return from let block
                        val newGroup = state.user?.groups ?: return@let
                        //update groups in cache , if null-> set hashmap with new groups
                        cachedUser.groups?.putAll(newGroup) ?: run {
                            cachedUser.groups = hashMapOf<String, String>().apply { putAll(newGroup) }
                        }
                        //add user to database
                        userDao.addUser(user = userMapper.mapFirebaseResponseToDatabaseEntity(cachedUser))
                    }
                }
                withContext(Dispatchers.Main) { callback(state) }
            }
        }
    }


    override fun updateEmail(id: String, email: String, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.updateEmail(id, email) { state -> callback(state) }
    }

    override fun getUserGroups(id: String, loadFlag: DataLoadFlag, callback: (UserRequestState) -> Unit) {
        ioCoroutineScope.launch(getGroupMembersThreadExecutor) {
            var cachedUser = cachedUsers.find { it.id == id }
            val loadFromServer = loadFlag == DataLoadFlag.LOAD_FROM_SERVER ||
                    (loadFlag == DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE && cachedUser == null)
            val isNetworkConnectionAvailable = firebaseConnectionStatus.getConnectionStatus()
            if (loadFromServer && isNetworkConnectionAvailable) {
                firebaseUsersDatabase.getGroups(id) { state ->
                    callback(state)
                }
            } else {
                if (cachedUser != null) {
                    callback(UserRequestState.UserRequestSuccess(cachedUser))
                } else {
                    userDao.getUsersByID(id)?.let {
                        val databaseUser = userMapper.mapDatabaseEntityToFirebaseUser(it)
                        withContext(Dispatchers.Main) { callback(UserRequestState.UserRequestSuccess(databaseUser)) }
                    } ?: withContext(Dispatchers.Main) { callback(UserRequestState.UserRequestFail) }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getGroupMembers(groupId: String, loadFlag: DataLoadFlag) {
        ioCoroutineScope.launch(getGroupMembersThreadExecutor) {
            val usersAreAlreadyLoaded = usersAreLoaded.getOrDefault(groupId, false)
            val loadFromServer = loadFlag == DataLoadFlag.LOAD_FROM_SERVER ||
                    (loadFlag == DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE && !usersAreAlreadyLoaded)

            val isNetworkConnectionAvailable = firebaseConnectionStatus.getConnectionStatus()

            if (loadFromServer && isNetworkConnectionAvailable) {
                firebaseUsersDatabase.getGroupMembers(groupId)
                subscribedForServerChanges[groupId] = true
                userConditionVariable.close()
                userConditionVariable.block()
            } else {
                if (!isNetworkConnectionAvailable && !subscribedForServerChanges.getOrDefault(groupId, false)) {
                    firebaseUsersDatabase.getGroupMembers(groupId)
                    subscribedForServerChanges[groupId] = true
                }
                if (usersAreAlreadyLoaded) {
                    fireChangedUsersData(groupId)
                } else { // Getting data from DB
                    ioCoroutineScope.launch {
                        val databaseUsers =
                            userDao.getUsersByGroupId(groupId).map { userMapper.mapDatabaseEntityToFirebaseUser(it) }
                        usersAreLoaded[groupId] = true
                        updateCachedUsers(databaseUsers, saveToDB = false)
                        withContext(Dispatchers.Main) {
                            fireChangedUsersData(groupId)
                        }
                    }
                }
            }
        }
    }

    override fun updateProfilePhoto(id: String, bytes: ByteArray, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.updateProfilePhoto(id, bytes) { state ->
            if (state is UserRequestState.UserRequestSuccess) {
                cachedUsers.find { it.id == id }?.photoURL = state.user?.photoURL
            }
            callback(state)
        }
    }

    override fun deleteProfilePhoto(id: String, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.deleteProfilePhoto(id) { state ->
            if (state is UserRequestState.UserRequestSuccess) {
                cachedUsers.find { it.id == id }?.photoURL = null
            }
            callback(state)
        }
    }

    override fun removeFcmToken(id: String, token: String) {
        firebaseUsersDatabase.removeFcmToken(id, token)
    }

    override fun updateFcmToken(id: String, token: String) {
        firebaseUsersDatabase.updateFcmToken(id, token)
    }

    private fun deleteRedundantGroupId(newUsers: List<DatabaseUser>, groupId: String) {
        val newUsersIds = newUsers.map { it.id }
        cachedUsers.filter { it.groups?.containsKey(groupId) ?: false }.forEach { cachedUser ->
            // if cached user id is not in new list, but this cached user contains group id -> delete this id
            if (cachedUser.id !in newUsersIds && cachedUser.groups?.containsKey(groupId) == true) {
                cachedUser.groups?.remove(groupId)
                val tempUser = cachedUser.id?.let { userDao.getUsersByID(it) }
                val updatedGroups = tempUser?.groups.also { it?.remove(groupId) }
                cachedUser.id?.let { userDao.updateGroups(it, updatedGroups) }
            }
        }
    }

    private fun updateCachedUsers(newUsers: List<DatabaseUser>, changedUsers: MutableList<String?>? = null, saveToDB: Boolean = true) {
        newUsers.forEach { newUser ->
            //checking if input list contains not-cached user
            val cachedUser = cachedUsers.find { it.id == newUser.id }
            //add new user to cache and to database
            if (cachedUser == null) {
                cachedUsers.add(newUser)
                //add new user database if input parameter saveToDB == true
                if (saveToDB) {
                    userDao.addUser(userMapper.mapFirebaseResponseToDatabaseEntity(newUser))
                }
            }
            //replace old cached user with updated user in case if user already exists in cache
            else if (cachedUser != newUser) {
                cachedUsers.remove(cachedUser)
                cachedUsers.add(newUser)
                changedUsers?.add(newUser.id)
                userDao.addUser(userMapper.mapFirebaseResponseToDatabaseEntity(newUser))
            }
        }
    }

    private fun fireChangedUsersData(groupId: String) {
        synchronized(usersListeners) {
            usersListeners[groupId]?.let {
                val iterator = it.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    item.setNewUsers(groupId, getCachedUsersByGroupId(groupId))
                }
            }
        }
    }

    override fun addListener(groupId: String, listener: UsersListener) {
        synchronized(usersListeners) {
            usersListeners[groupId]?.add(listener) ?: usersListeners.put(groupId, mutableSetOf(listener))
        }
    }

    override fun removeListener(listener: UsersListener) {
        synchronized(usersListeners) {
            usersListeners.values.forEach {
                it.remove(listener)
            }
        }
    }

    override fun muteItem(currentUserId: String, itemId: String, callback: ((UserRequestState) -> Unit)?) {
        firebaseUsersDatabase.muteItem(currentUserId, itemId) { state ->
            ioCoroutineScope.launch {
                when (state) {
                    is UserRequestState.UserRequestSuccess -> {
                        val cachedUser = cachedUsers.find { it.id == currentUserId }
                        cachedUser?.let {
                            val newMuted = state.user?.mutedItems ?: return@let
                            cachedUser.mutedItems?.putAll(newMuted) ?: run {
                                cachedUser.mutedItems = hashMapOf<String, String>().apply { putAll(newMuted) }
                            }
                            userDao.addUser(user = userMapper.mapFirebaseResponseToDatabaseEntity(cachedUser))
                        }
                        callback?.invoke(UserRequestState.UserRequestSuccess(cachedUser))
                    }
                    is UserRequestState.UserRequestFail -> {}
                }
            }
        }
    }

    override fun unMuteItem(currentUserId: String, itemId: String, callback: ((UserRequestState) -> Unit)?) {
        firebaseUsersDatabase.unMuteItem(currentUserId, itemId) { state ->
            ioCoroutineScope.launch {
                when (state) {
                    is UserRequestState.UserRequestSuccess -> {
                        val cachedUser = cachedUsers.find { it.id == currentUserId }
                        cachedUser?.let {
                            val newMuted = state.user?.mutedItems ?: return@let
                            cachedUser.mutedItems?.putAll(newMuted) ?: run {
                                cachedUser.mutedItems = hashMapOf<String, String>().apply { putAll(newMuted) }
                            }
                            userDao.addUser(user = userMapper.mapFirebaseResponseToDatabaseEntity(cachedUser))
                        }
                        callback?.invoke(UserRequestState.UserRequestSuccess(cachedUser))
                    }
                    is UserRequestState.UserRequestFail -> {}
                }
            }
        }
    }

    override fun hideUserInfo(currentUserId: String, isHide: Boolean, callback: (UserRequestState) -> Unit) {
        firebaseUsersDatabase.hideUserInfo(currentUserId, isHide) { state ->
            ioCoroutineScope.launch {
                when (state) {
                    is UserRequestState.UserRequestSuccess -> {
                        val cachedUser = cachedUsers.find { it.id == currentUserId }
                        if (cachedUser != null) {
                            cachedUser.hideInfo = isHide
                        }
                        userDao.hideUserInfo(currentUserId, isHide)
                    }
                    is UserRequestState.UserRequestFail -> {}
                }
                withContext(Dispatchers.Main) { callback(state) }
            }
        }
    }

    private fun getCachedUsersByGroupId(groupId: String): List<DatabaseUser> {
        return cachedUsers.filter { it.groups?.containsKey(groupId) ?: false }
    }
}
