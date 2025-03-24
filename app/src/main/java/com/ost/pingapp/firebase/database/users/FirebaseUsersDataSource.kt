package com.khrd.pingapp.firebase.database.users

import android.os.ConditionVariable
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.khrd.pingapp.constants.Constants.PROFILE_PATH
import com.khrd.pingapp.constants.DbConstants
import com.khrd.pingapp.constants.DbConstants.FCMTOKENS
import com.khrd.pingapp.data.users.DatabaseUser
import com.khrd.pingapp.data.users.Online
import com.khrd.pingapp.data.users.UserRequestState
import com.khrd.pingapp.data.users.UsersDataSource
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.listeners.UsersListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseUsersDataSource @Inject constructor(var firebaseConnectionStatus: ConnectionStatus, val ioCoroutineScope: CoroutineScope) :
    UsersDataSource {
    private val database = Firebase.database.reference
    private val usersDatabase = Firebase.database.getReference("users")
    private val storage = FirebaseStorage.getInstance().reference
    private val dbUsers = database.child(DbConstants.USERS)
    private val cachedUsersValueListeners = mutableSetOf<GroupsEventListener>()
    private val currentOnline: Queue<Online> = LinkedList()
    private val onlineMutex = Any()

    @DelicateCoroutinesApi
    private val onlineHandlerScope = newSingleThreadContext("onlineHandlerScope")

    private var usersListener: UsersListener? = null


    inner class GroupsEventListener(val groupId: String) : ValueEventListener {

        override fun onDataChange(snapshot: DataSnapshot) {
            val users = mutableListOf<DatabaseUser>()
            snapshot.children.forEach { dataSnapshot ->
                dataSnapshot.getValue(DatabaseUser::class.java)?.let { databaseUser -> users.add(databaseUser) }
            }
            usersListener?.onUsersChanged(groupId, users)
        }

        override fun onCancelled(error: DatabaseError) {
            usersListener?.onUsersChanged(groupId, null)
        }
    }

    private fun getListener(groupId: String): GroupsEventListener {
        var listener = cachedUsersValueListeners.find { it.groupId == groupId }
        if (listener == null) {
            listener = GroupsEventListener(groupId)
            cachedUsersValueListeners.add(listener)
        }
        return listener
    }

    override suspend fun createUser(id: String, email: String, username: String): UserRequestState {
        return suspendCoroutine { cont ->
            val newUser = DatabaseUser(id, email, username)
            val callback = OnCompleteListener<Void> { task ->
                if (task.isSuccessful) {
                    cont.resume(UserRequestState.UserRequestSuccess(newUser))
                } else {
                    cont.resume(UserRequestState.UserRequestFail)
                }
            }
            usersDatabase.child(id).setValue(newUser).addOnCompleteListener(callback)
        }
    }

    override fun getUser(id: String, callback: (UserRequestState) -> Unit) {
        usersDatabase.child(id).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(UserRequestState.UserRequestSuccess(task.result.getValue(DatabaseUser::class.java)))
            } else {
                callback(UserRequestState.UserRequestFail)
            }
        }
    }

    override fun updateUsername(id: String, username: String, callback: (UserRequestState) -> Unit) {
        val task = usersDatabase.child(id).child(DbConstants.USERNAME).setValue(username)
        if (firebaseConnectionStatus.getConnectionStatus()) {
            task.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(UserRequestState.UserRequestSuccess(DatabaseUser(id = id, username = username)))
                } else {
                    callback(UserRequestState.UserRequestFail)
                }
            }
        } else {
            callback(UserRequestState.UserRequestOffline(DatabaseUser(id = id, username = username)))
        }
    }

    override fun updateUserJob(id: String, job: String, callback: (UserRequestState) -> Unit) {
        val task = usersDatabase.child(id).child(DbConstants.JOB).setValue(job)
        task.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(UserRequestState.UserRequestSuccess(DatabaseUser(id = id, job = job)))
            } else {
                callback(UserRequestState.UserRequestFail)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun updateUserOnlineStatus(id: String, deviceId: String, status: Online) {
        putOnline(status)
        ioCoroutineScope.launch(onlineHandlerScope) {
            val conditionVariable = ConditionVariable()
            val obtainedStatus = obtainOnline()
            if (obtainedStatus != null) {
                usersDatabase.child(id).child(DbConstants.ONLINE).child(deviceId).setValue(obtainedStatus).addOnCompleteListener {
                    conditionVariable.open()
                }
                conditionVariable.block()
            }
        }
    }

    private fun putOnline(online: Online) {
        synchronized(onlineMutex) {
            currentOnline.add(online)
        }
    }

    private fun obtainOnline(): Online? {
        synchronized(onlineMutex) {
            val online = currentOnline.lastOrNull()
            currentOnline.clear()
            return online
        }
    }

    override fun deleteUser(id: String, callback: (UserRequestState) -> Unit) {
        val deletedUser = DatabaseUser(id = id, isDeleted = true)
        // deleting users storage
        storage.child(DbConstants.USERS).child(id).listAll().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result.items.forEach { it.delete() }
            } else {
                callback(UserRequestState.UserRequestFail)
            }
        }
        // deleting users data, saving his id
        usersDatabase.child(id).setValue(deletedUser).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(UserRequestState.UserRequestSuccess(deletedUser))
            } else {
                callback(UserRequestState.UserRequestFail)
            }
        }
    }

    override fun deleteUserGroup(id: String, groupId: String, callback: (UserRequestState) -> Unit) {
        val task = usersDatabase.child(id).child(DbConstants.GROUPS).child(groupId).removeValue()
        if (firebaseConnectionStatus.getConnectionStatus()) {
            task.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(UserRequestState.UserRequestSuccess(DatabaseUser(id = id)))
                } else {
                    callback(UserRequestState.UserRequestFail)
                }
            }
        } else {
            callback(UserRequestState.UserRequestOffline(DatabaseUser(id = id)))
        }
    }

    override fun addUserGroup(id: String, groupId: String, callback: (UserRequestState) -> Unit) {
        val timestamp = System.currentTimeMillis().toString()
        val groupValue = "${groupId}_${timestamp}"
        usersDatabase.child("$id/${DbConstants.GROUPS}/$groupId").setValue(groupValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(UserRequestState.UserRequestSuccess(DatabaseUser(id = id, groups = hashMapOf(groupId to groupValue))))
                } else {
                    callback(UserRequestState.UserRequestFail)
                }
            }
    }

    override fun updateEmail(id: String, email: String, callback: (UserRequestState) -> Unit) {
        usersDatabase.child(id).child(DbConstants.EMAIL).setValue(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(UserRequestState.UserRequestSuccess(DatabaseUser(id = id, email = email)))
            } else {
                callback(UserRequestState.UserRequestFail)
            }
        }
    }

    override fun updateProfilePhoto(id: String, bytes: ByteArray, callback: (UserRequestState) -> Unit) {
        val ref = storage.child(DbConstants.USERS).child(id).child(PROFILE_PATH)
        ref.putBytes(bytes).addOnCompleteListener { task1 ->
            if (task1.isSuccessful) {
                ref.downloadUrl.addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        usersDatabase.child(id).child(DbConstants.PHOTOURL).setValue(task2.result.toString())
                            .addOnCompleteListener { task3 ->
                                if (task3.isSuccessful) {
                                    callback(UserRequestState.UserRequestSuccess(DatabaseUser(id = id, photoURL = task2.result.toString())))
                                } else {
                                    callback(UserRequestState.UserRequestFail)
                                }
                            }
                    } else {
                        callback(UserRequestState.UserRequestFail)
                    }
                }
            } else {
                callback(UserRequestState.UserRequestFail)
            }
        }
    }

    override fun deleteProfilePhoto(id: String, callback: (UserRequestState) -> Unit) {
        val profilePhotoRealtimeDBReference = usersDatabase.child(id).child(DbConstants.PHOTOURL).removeValue()
        val profilePhotoStorageReference = storage.child(DbConstants.USERS).child(id).child(PROFILE_PATH)
        profilePhotoStorageReference.delete().addOnCompleteListener { deleteFromStorage ->
            if (deleteFromStorage.isSuccessful) {
                profilePhotoRealtimeDBReference.addOnCompleteListener { deleteFromRealtimeDB ->
                    if (deleteFromRealtimeDB.isSuccessful) {
                        callback(UserRequestState.UserRequestSuccess(DatabaseUser(id = id)))
                    } else {
                        callback(UserRequestState.UserRequestFail)
                    }
                }
            } else {
                callback(UserRequestState.UserRequestFail)
            }
        }
    }


    override fun getGroups(id: String, callback: (UserRequestState) -> Unit) {
        usersDatabase.child("$id/groups").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val t = object : GenericTypeIndicator<HashMap<String, String>>() {}
                val groups = task.result.getValue(t)
                callback(
                    UserRequestState.UserRequestSuccess(
                        DatabaseUser(id = id, groups = groups)
                    )
                )
            } else {
                callback(UserRequestState.UserRequestFail)
                Log.d("*****", task.exception.toString())
            }
        }
    }

    override fun getGroupMembers(groupId: String) {
        val query = dbUsers.orderByChild(DbConstants.GROUPS + "/${groupId}").startAt(groupId)
        query.addValueEventListener(getListener(groupId))
    }

    override fun setListener(listener: UsersListener) {
        usersListener = listener
    }

    override fun removeUsersValueListeners() {
        cachedUsersValueListeners.forEach {
            dbUsers.removeEventListener(it)
        }
    }

// TODO add method to remove listener when group is removed

    override fun updateFcmToken(id: String, token: String) {
        usersDatabase.child(id).child(FCMTOKENS).child(token).setValue(token)
    }

    override fun removeFcmToken(id: String, token: String) {
        usersDatabase.child(id).child(FCMTOKENS).child(token).removeValue()
    }

    override fun muteItem(currentUserId: String, userId: String, callback: (UserRequestState) -> Unit) {
        val timestamp = System.currentTimeMillis().toString()
        val muteValue = "${userId}_${timestamp}"
        usersDatabase.child("$currentUserId/${DbConstants.MUTED_ITEMS}/$userId").setValue(muteValue)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(

                        UserRequestState.UserRequestSuccess(
                            DatabaseUser(
                                id = currentUserId,
                                mutedItems = hashMapOf(userId to muteValue)
                            )
                        )
                    )
                } else {
                    callback(UserRequestState.UserRequestFail)
                }
            }
    }

    override fun unMuteItem(currentUserId: String, userId: String, callback: (UserRequestState) -> Unit) {
        val task = usersDatabase.child(currentUserId).child(DbConstants.MUTED_ITEMS).child(userId).removeValue()
        if (firebaseConnectionStatus.getConnectionStatus()) {
            task.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(UserRequestState.UserRequestSuccess(DatabaseUser(id = currentUserId)))
                } else {
                    callback(UserRequestState.UserRequestFail)
                }
            }
        } else {
            callback(UserRequestState.UserRequestFail)
        }
    }

    override fun hideUserInfo(currentUserId: String, isHide: Boolean, callback: (UserRequestState) -> Unit) {
        val task = usersDatabase.child(currentUserId).child(DbConstants.HIDE_INFO).setValue(isHide)
        if (firebaseConnectionStatus.getConnectionStatus()) {
            task.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(
                        UserRequestState.UserRequestSuccess(
                            DatabaseUser(
                                id = currentUserId,
                                hideInfo = isHide
                            )
                        )
                    )
                } else {
                    callback(UserRequestState.UserRequestFail)
                }

            }
        } else {
            callback(UserRequestState.UserRequestFail)
        }
    }
}