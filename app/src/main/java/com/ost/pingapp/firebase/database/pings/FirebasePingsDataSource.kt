package com.khrd.pingapp.firebase.database.pings

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.khrd.pingapp.constants.DbConstants
import com.khrd.pingapp.data.pings.*
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.listeners.NewPingsListener
import com.khrd.pingapp.homescreen.listeners.NewPingsSentListener
import com.khrd.pingapp.homescreen.listeners.ScheduledPingsListener
import com.khrd.pingapp.homescreen.states.PingsType
import com.khrd.pingapp.utils.PingConverter
import javax.inject.Inject

class FirebasePingsDataSource @Inject constructor(var firebaseConnectionStatus: ConnectionStatus) : PingsDataSource {

    private val database = Firebase.database.reference
    private val pings = database.child(DbConstants.PINGS)
    private val scheduledPings = database.child(DbConstants.SHEDULED_PINGS)

    private val sentPingsListeners = mutableListOf<NewPingsSentListener>()
    private val scheduledPingsListeners = mutableListOf<ScheduledPingsListener>()
    private var receivedPingsListener: NewPingsListener? = null

    private val cachedReceivedPingsValueListeners = mutableSetOf<ValueEventListener>()
    private val cachedSentPingsValueListeners = mutableSetOf<ValueEventListener>()
    private val cachedScheduledPingsValueListeners = mutableSetOf<ValueEventListener>()

    private val pingsValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val pings = mutableListOf<DatabasePing>()
            snapshot.children.forEach { dataSnapshot ->
                dataSnapshot.getValue(DatabasePing::class.java)?.let { databasePing -> pings.add(databasePing) }
            }
            receivedPingsListener?.onNewPingsReceived(pings)
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    private val sentPingsValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            sentPingsListeners.forEach { listener ->
                val pings = mutableListOf<DatabasePing>()
                snapshot.children.forEach {
                    it.getValue(DatabasePing::class.java)?.let { it1 -> pings.add(it1) }
                }
                listener.onNewSentPings(pings)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    private val scheduledPingsValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            scheduledPingsListeners.forEach { listener ->
                val pings = mutableListOf<DatabasePing>()
                snapshot.children.forEach {
                    it.getValue(DatabasePing::class.java)?.let { it1 -> pings.add(it1) }
                }
                listener.onNewPingsScheduledOrChanged(pings)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    }

    override fun createPing(pingData: PingData, callback: (PingState) -> Unit) {

        val ping = PingConverter().convertDatabasePing(pingData)
        val ref = if (pingData.scheduledTime == null) pings else scheduledPings
        val task = ref.child(ping.id).setValue(ping)
        if (firebaseConnectionStatus.getConnectionStatus()) {
            task.addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(PingStateSuccess(listOf(ping)))
                } else {
                    callback(PingStateFailure)
                }
            }
        } else {
            callback(PingOfflineState)
        }

    }

    override fun getScheduledPings(fromUserId: String, callback: (PingState) -> Unit) {
        scheduledPings.orderByChild("${DbConstants.FROM}/$fromUserId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pings = mutableListOf<DatabasePing>()
                snapshot.children.forEach {
                    it.getValue(DatabasePing::class.java)?.let { it1 -> pings.add(it1) }
                }
                callback(PingStateSuccess(pings))
            }

            override fun onCancelled(error: DatabaseError) {
                callback(PingStateFailure)
            }

        })
    }

    override fun deleteScheduledPing(id: String, callback: (PingState) -> Unit) {
        val task = scheduledPings.child(id).removeValue()
        if (firebaseConnectionStatus.getConnectionStatus()) {
            task.addOnCompleteListener {
                if (it.isSuccessful) {
                    callback(PingStateSuccess(listOf()))
                } else {
                    callback(PingStateFailure)
                }
            }
        } else {
            callback(PingOfflineState)
        }
    }

    override fun getSentPings(fromUserId: String, offset: String?) {
        var query = pings.orderByChild("${DbConstants.FROM}/$fromUserId").limitToLast(DbConstants.PINGS_LOAD_LIMIT)
        query = if (offset.isNullOrBlank()) query.startAt(fromUserId) else query.startAt(fromUserId).endBefore(offset)
        val listener = query.addValueEventListener(sentPingsValueListener)
        cachedSentPingsValueListeners.add(listener)
    }

    @SuppressLint("LongLogTag")
    override fun loadReceivedPings(userId: String, offset: String) {
        Log.d("***** FirebasePingsDataSource loadReceivedPings", "userId: $userId, offset: $offset")
        var query = pings.orderByChild("${DbConstants.RECEIVERS}/$userId").limitToLast(DbConstants.PINGS_LOAD_LIMIT)
        query = if (offset.isBlank()) query.startAt(userId) else query.startAt(userId).endBefore(offset)
        val listener = query.addValueEventListener(pingsValueListener)
        cachedReceivedPingsValueListeners.add(listener)
    }

    override fun getScheduledPingsWithPagination(fromUserId: String, offset: String?) {
        var query = scheduledPings.orderByChild("${DbConstants.FROM}/$fromUserId").limitToLast(DbConstants.PINGS_LOAD_LIMIT)
        query = if (offset.isNullOrBlank()) query.startAt(fromUserId) else query.startAt(fromUserId).endBefore(offset)
        val listener = query.addValueEventListener(scheduledPingsValueListener)
        cachedScheduledPingsValueListeners.add(listener)
    }

    override fun addListener(listener: NewPingsListener, pingsType: PingsType) {
        when (pingsType) {
            PingsType.RECEIVED -> receivedPingsListener = listener
            PingsType.SENT -> {
            }
            PingsType.SCHEDULED -> {
            }
        }
    }

    override fun changePingSeenStatus(userId: String, pingId: String) {
        pings.child(pingId).child(DbConstants.VIEWS).child(userId).setValue(System.currentTimeMillis().toString())
    }

    override fun addSentPingsListener(listener: NewPingsSentListener) {
        sentPingsListeners.add(listener)
    }

    override fun removeSentPingsListener(listener: NewPingsSentListener) {
        sentPingsListeners.remove(listener)
    }

    override fun addScheduledPingsListener(listener: ScheduledPingsListener) {
        scheduledPingsListeners.add(listener)
    }

    override fun removeScheduledPingsListener(listener: ScheduledPingsListener) {
        scheduledPingsListeners.remove(listener)
    }

    override fun removeReceivedPingsValueListeners() {
        cachedReceivedPingsValueListeners.forEach {
            pings.removeEventListener(it)
        }
    }

    override fun removeSentPingsValueListeners() {
        cachedSentPingsValueListeners.forEach {
            pings.removeEventListener(it)
        }
    }

    override fun removeScheduledPingsValueListeners() {
        cachedScheduledPingsValueListeners.forEach {
            scheduledPings.removeEventListener(it)
        }
    }
}