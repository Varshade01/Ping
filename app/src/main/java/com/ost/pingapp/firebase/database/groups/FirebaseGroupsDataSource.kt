package com.khrd.pingapp.firebase.database.groups

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.khrd.pingapp.constants.Constants
import com.khrd.pingapp.constants.DbConstants
import com.khrd.pingapp.data.groups.*
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import javax.inject.Inject

class FirebaseGroupsDataSource @Inject constructor(var firebaseConnectionStatus: ConnectionStatus) : GroupsDataSource {
    private val database = Firebase.database.reference
    private val groups = database.child(DbConstants.GROUPS)
    private val storage = FirebaseStorage.getInstance().reference

    override fun createGroup(userId: String, name: String?, callback: (GroupState) -> Unit) {
        val key = groups.push().key
        if (key == null) {
            callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            return
        }
        val group = DatabaseGroup(id = key, name = name)
        val task = groups.child(key).setValue(group)
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                callback(GroupSuccess(group))
            } else {
                callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            }
        }
    }

    override fun removeGroup(id: String, callback: (GroupState) -> Unit) {
        val ref = groups.child(id)
        ref.keepSynced(true)
        val task = ref.removeValue()
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                callback(GroupSuccess(DatabaseGroup(id = id)))
            } else {
                callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            }
        }
    }

    override fun updateGroupName(id: String, name: String, callback: (GroupState) -> Unit) {
        val task = groups.child(id).child(DbConstants.NAME).setValue(name)
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                callback(GroupSuccess(DatabaseGroup(id = id, name = name)))
            } else {
                Log.d("*****", it.exception.toString())
                callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            }
        }
    }

    override fun addInvitationLinkToGroup(id: String, invitationLink: String, callback: (GroupState) -> Unit) {
        val task = groups.child(id).child(DbConstants.INVITATION_LINK).setValue(invitationLink)
        task.addOnCompleteListener {
            if (it.isSuccessful) {
                callback(GroupSuccess(DatabaseGroup(id = id, invitationLink = invitationLink)))
            } else {
                Log.d("*****", it.exception.toString())
                callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            }
        }
    }

    override fun getGroup(id: String, callback: (GroupState) -> Unit) {
        val ref = groups.child(id)
        ref.keepSynced(true)
        ref.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val result = it.result.getValue<DatabaseGroup>()
                if (result != null) {
                    callback(GroupSuccess(result))
                } else {
                    callback(GroupFailure(GroupError.UNEXISTING_GROUP))
                }
            } else {
                Log.d("*****", it.exception.toString())
                callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            }
        }
    }

    override fun getGroupByLink(link: String, callback: (GroupState) -> Unit) {
        val ref = groups.orderByChild(DbConstants.INVITATION_LINK).equalTo(link)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var group = DatabaseGroup()
                for (sp in snapshot.children) {
                    group = sp.getValue<DatabaseGroup>()!!
                }

                if (snapshot.value != null) {
                    callback(GroupSuccess(group))
                } else {
                    callback(GroupFailure(GroupError.UNEXISTING_GROUP))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(GroupFailure(GroupError.UNEXISTING_GROUP))
            }
        })
    }

    override fun updateGroupImage(groupId: String, bytes: ByteArray, callback: (GroupState) -> Unit) {
        val storageReference = storage.child(DbConstants.GROUPS).child(groupId).child(Constants.GROUP_PATH)
        storageReference.putBytes(bytes).addOnCompleteListener { snapshot ->
            if (snapshot.isSuccessful) {
                downloadUrl(storageReference, groupId, callback)
            } else {
                callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            }
        }
    }

    private fun downloadUrl(storageReference: StorageReference, groupId: String, callback: (GroupState) -> Unit) {
        storageReference.downloadUrl.addOnCompleteListener { taskUri ->
            if (taskUri.isSuccessful) {
                setImage(groupId, taskUri, callback)
            } else {
                callback(GroupFailure(GroupError.UNKNOWN_ERROR))
            }
        }
    }

    private fun setImage(id: String, taskUri: Task<Uri>, callback: (GroupState) -> Unit) {
        groups.child(id).child(DbConstants.PHOTOURL).setValue(taskUri.result.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(GroupSuccess(DatabaseGroup(id = id, photoURL = taskUri.result.toString())))
                } else {
                    callback(GroupFailure(GroupError.UNKNOWN_ERROR))
                }
            }
    }
}