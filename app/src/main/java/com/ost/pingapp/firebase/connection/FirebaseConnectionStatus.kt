package com.khrd.pingapp.firebase.connection

import android.os.ConditionVariable
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.khrd.pingapp.di.IoCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseConnectionStatus @Inject constructor(@IoCoroutineScope private val ioCoroutineScope: CoroutineScope) : ConnectionStatus {
    private var connected = false
    private var retrieveStatusCondition: ConditionVariable = ConditionVariable()

    init {
        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("***** onDataChange", "$snapshot")
                connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    retrieveStatusCondition.open()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                connected = false
                retrieveStatusCondition.open()
            }
        })
    }

    override fun getConnectionStatus() = connected
    override fun retrieveConnectionStatus(callback: (Boolean) -> Unit) {
        ioCoroutineScope.launch {
            retrieveStatusCondition.block(3000)
            withContext(Dispatchers.Main) {
                callback(connected)
            }
        }
    }

    //waiting util connection status would be established
    override fun retrieveConnectionStatus(): Boolean {
        retrieveStatusCondition.block(3000)
        return connected
    }
}