package com.khrd.pingapp.repository.pings

import androidx.annotation.WorkerThread
import com.khrd.pingapp.constants.DbConstants
import com.khrd.pingapp.data.database.pings.PingDao
import com.khrd.pingapp.data.database.pings.PingMapper
import com.khrd.pingapp.data.pings.*
import com.khrd.pingapp.di.IoCoroutineScope
import com.khrd.pingapp.firebase.authentication.FirebaseAuthAPI
import com.khrd.pingapp.firebase.connection.ConnectionStatus
import com.khrd.pingapp.homescreen.listeners.NewPingsListener
import com.khrd.pingapp.homescreen.listeners.NewPingsSentListener
import com.khrd.pingapp.homescreen.listeners.ScheduledPingsListener
import com.khrd.pingapp.homescreen.states.PingsType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class PingsRepositoryImpl @Inject constructor(
    private val pingDao: PingDao,
    private val firebasePingsDataSource: PingsDataSource,
    private val connectionStatus: ConnectionStatus,
    private val firebaseAuth: FirebaseAuthAPI,
    private val mapper: PingMapper,
    @IoCoroutineScope private val ioCoroutineScope: CoroutineScope
) : PingsRepository {

    // ======= Scheduled Pings

    private var scheduledOffset: String = ""
    var scheduledHasNextPage = true

    // ======= Received Pings
    private var lastReceivedOffset: String = ""
    private var nextReceivedPageAvailable: Boolean = true
    private var sentHasNextPage = true
    private var unscheduledOffset: String = ""

    private var _sentPingsFlow = MutableStateFlow<List<DatabasePing>?>(null)
    private val _receivedPingsFlow = MutableStateFlow<ReceivedPingsData?>(null)
    private var _scheduledPingsFlow = MutableStateFlow<List<DatabasePing>?>(null)


    private var neverScheduledPingsLoadedFromServer = true

    private var subscribedToReceivedPings = false
    private var subscribedToSentPings = false
    private var subscribedToScheduledPings = false

    private val receivedPingsDataSourceListener = object : NewPingsListener() {
        override fun onNewPingsReceived(pings: List<DatabasePing>) {
            ioCoroutineScope.launch {
                handleReceivedPing(pings)
            }
        }
    }

    @Synchronized
    @WorkerThread
    private fun handleReceivedPing(pings: List<DatabasePing>) {
        if (nextReceivedPageAvailable) {
            nextReceivedPageAvailable = pings.size == DbConstants.PINGS_LOAD_LIMIT
        }
        val offset = if (pings.isNotEmpty()) pings.first().receivers.values.find { it.contains(firebaseAuth.currentUserId()!!) }
            ?: "" else ""
        // determining if new or pagination pings were received
        val comparison = if (lastReceivedOffset.isBlank()) -1 else offset.compareTo(lastReceivedOffset)
        // pagination, saving new offset
        if (comparison < 0) lastReceivedOffset = offset
//         new ping
        var newPing: DatabasePing? = null
        if (comparison > 0) {
            val cachedPingsIds = _receivedPingsFlow.value?.listOfPings?.map { it.id } ?: emptyList()
            newPing = pings.find { it.id !in cachedPingsIds }

        }
        updateReceivedPingsList(pings, newPing?.groupId)
    }

    private val sentPingsDataSourceListener = object : NewPingsSentListener {
        override fun onNewSentPings(pings: List<DatabasePing>) {
            ioCoroutineScope.launch {
                handleSentPings(pings)
            }
        }
    }

    @Synchronized
    @WorkerThread
    private fun handleSentPings(pings: List<DatabasePing>) {
        if (sentHasNextPage) {
            sentHasNextPage = pings.size == DbConstants.PINGS_LOAD_LIMIT
        }

        val offset = if (pings.isNotEmpty()) pings.first().from.values.first() else ""

        val comparison = if (unscheduledOffset.isBlank()) -1 else offset.compareTo(unscheduledOffset)
        updateSentPingsList(pings)
        if (comparison < 0) {
            unscheduledOffset = offset
        }
    }

    private val scheduledPingsDataSourceListener = object : ScheduledPingsListener {
        override fun onNewPingsScheduledOrChanged(pings: List<DatabasePing>) {
            ioCoroutineScope.launch {
                handleScheduledPing(pings)
            }
        }
    }

    @Synchronized
    @WorkerThread
    private fun handleScheduledPing(pings: List<DatabasePing>) {
        if (scheduledHasNextPage) {
            scheduledHasNextPage = pings.size == DbConstants.PINGS_LOAD_LIMIT
        }
        val cachedScheduledPings = _scheduledPingsFlow.value?.toMutableList() ?: mutableListOf()
        /* Clear cached data when pings first time loaded */
        if (neverScheduledPingsLoadedFromServer) {
            cachedScheduledPings.clear()
            pingDao.deleteAllScheduledPings()
            neverScheduledPingsLoadedFromServer = false
        }

        val offset = if (pings.isNotEmpty()) pings.first().from.values.first() else ""
        val comparison = if (scheduledOffset.isBlank()) -1 else offset.compareTo(scheduledOffset)

        removeNonExistentScheduledPings(cachedScheduledPings, pings)
        updateScheduledPingsList(cachedScheduledPings, pings)

        if (comparison < 0) {
            scheduledOffset = offset
        }
    }
// =====================

    init {
        firebasePingsDataSource.addListener(receivedPingsDataSourceListener, PingsType.RECEIVED)
        firebasePingsDataSource.addSentPingsListener(sentPingsDataSourceListener)
        firebasePingsDataSource.addScheduledPingsListener(scheduledPingsDataSourceListener)
    }

    override fun createPing(pingData: PingData, callback: (PingState) -> Unit) {
        firebasePingsDataSource.createPing(pingData) { state -> callback(state) }
    }

    override fun deleteScheduledPing(id: String, callback: (PingState) -> Unit) {
        firebasePingsDataSource.deleteScheduledPing(id) { state -> callback(state) }
    }


    override fun getSentPings(fromUserId: String?): StateFlow<List<DatabasePing>?> {
        if (!subscribedToSentPings && fromUserId != null) {
            loadSentPings(fromUserId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
        }
        return _sentPingsFlow
    }

    override fun loadMoreSentPings(fromUserId: String?) {
        if (fromUserId != null) {
            loadSentPings(fromUserId, DataLoadFlag.LOAD_FROM_SERVER)
        }
    }

    @Synchronized
    private fun loadSentPings(fromUserId: String, dataLoadFlag: DataLoadFlag) {
        if (subscribedToSentPings && dataLoadFlag != DataLoadFlag.LOAD_FROM_SERVER) {
            return
        }
        subscribedToSentPings = true
        if (sentHasNextPage) {
            firebasePingsDataSource.getSentPings(fromUserId, unscheduledOffset)
        }

        // If there is no connection
        if (!connectionStatus.getConnectionStatus() && _sentPingsFlow.value == null) {
            ioCoroutineScope.launch {
                val pings = pingDao.getSentPingsFromDb().map { mapper.mapSentPingToFirebaseEntity(it) }
                _sentPingsFlow.emit(pings)
            }
        }
    }

    override fun getScheduledPings(fromUserId: String?, dataLoadFlag: DataLoadFlag): StateFlow<List<DatabasePing>?> {
        if (!subscribedToScheduledPings && fromUserId != null) {
            getScheduledPingsWithPagination(fromUserId, dataLoadFlag)
        }
        return _scheduledPingsFlow
    }

    override fun loadMoreScheduledPings(fromUserId: String?) {
        if (fromUserId != null) {
            getScheduledPingsWithPagination(fromUserId, DataLoadFlag.LOAD_FROM_SERVER)
        }
    }

    override fun getScheduledPingsWithPagination(fromUserId: String, dataLoadFlag: DataLoadFlag) {
        if (subscribedToScheduledPings && dataLoadFlag != DataLoadFlag.LOAD_FROM_SERVER) return
        subscribedToScheduledPings = true
        if (scheduledHasNextPage) {
            firebasePingsDataSource.getScheduledPingsWithPagination(fromUserId, scheduledOffset)
        }

        // If there is no connection
        if (!connectionStatus.getConnectionStatus() && _scheduledPingsFlow.value == null && dataLoadFlag != DataLoadFlag.LOAD_FROM_SERVER) {
            ioCoroutineScope.launch {
                val pings = pingDao.getScheduledPingsFromDb().map { mapper.mapScheduledPingToFirebaseEntity(it) }
                _scheduledPingsFlow.emit(pings)
            }
        }
    }

    override fun loadMoreReceivedPings(userId: String?) {
        if (userId != null) {
            getReceivedPings(userId, DataLoadFlag.LOAD_FROM_SERVER)
        }
    }


    override fun loadReceivedPings(userId: String?): StateFlow<ReceivedPingsData?> {
        if (!subscribedToReceivedPings && userId != null) {
            getReceivedPings(userId, DataLoadFlag.LOAD_FROM_CACHE_IF_AVAILABLE)
        }
        return _receivedPingsFlow
    }

    @Synchronized
    private fun getReceivedPings(userId: String, loadFlag: DataLoadFlag) {
        if (subscribedToReceivedPings && loadFlag != DataLoadFlag.LOAD_FROM_SERVER) {
            return
        }
        subscribedToReceivedPings = true
        if (nextReceivedPageAvailable) {
            firebasePingsDataSource.loadReceivedPings(userId, lastReceivedOffset)
        }
        if (!connectionStatus.getConnectionStatus() && _receivedPingsFlow.value == null && loadFlag != DataLoadFlag.LOAD_FROM_SERVER) {
            ioCoroutineScope.launch {
                val pings = pingDao.getReceivedPingsFromDb().map { mapper.mapReceivedPingToFirebaseEntity(it) }
                _receivedPingsFlow.emit(ReceivedPingsData(pings))
            }
        }
    }

    override fun changePingSeenStatus(pingId: String) {
        firebaseAuth.currentUserId()?.let { firebasePingsDataSource.changePingSeenStatus(it, pingId) }
    }

    override fun clearReceivedPingsCache() {
        _receivedPingsFlow.tryEmit(null)
        subscribedToReceivedPings = false
        firebasePingsDataSource.removeReceivedPingsValueListeners()
        lastReceivedOffset = ""
        nextReceivedPageAvailable = true
        ioCoroutineScope.launch {
            pingDao.deleteAllReceivedPings()
        }
    }

    override fun clearSentPingsCache() {
        _sentPingsFlow.tryEmit(null)
        subscribedToSentPings = false
        firebasePingsDataSource.removeSentPingsValueListeners()
        unscheduledOffset = ""
        sentHasNextPage = true
        ioCoroutineScope.launch {
            pingDao.deleteAllSentPings()
        }
    }

    override fun clearScheduledPingsCache() {
        _scheduledPingsFlow.tryEmit(null)
        subscribedToScheduledPings = false
        firebasePingsDataSource.removeScheduledPingsValueListeners()
        scheduledOffset = ""
        scheduledHasNextPage = true
        ioCoroutineScope.launch {
            pingDao.deleteAllScheduledPings()
        }
    }

    override fun cacheSentPingOffline(offlinePing: DatabasePing) {
        ioCoroutineScope.launch {
            updateSentPingsList(listOf(offlinePing))
        }
    }

    override fun removeScheduledPingFromDB(scheduledPingId: String) {
        ioCoroutineScope.launch {
            pingDao.deleteScheduledPing(scheduledPingId)
        }
    }

    @Synchronized
    @WorkerThread
    private fun updateReceivedPingsList(newList: List<DatabasePing>, newPingId: String?) {
        val currentPings = _receivedPingsFlow.value?.listOfPings?.toMutableList() ?: mutableListOf()
        newList.forEach { newPing ->
            val cachePing = currentPings.find { it.id == newPing.id }
            if (cachePing == null) {
                currentPings.add(newPing)
                pingDao.insertReceivedPing(mapper.mapFirebaseResponseToReceivedPing(newPing))

            } else if (cachePing != newPing) {
                currentPings.remove(cachePing)
                currentPings.add(newPing)
                pingDao.insertReceivedPing(mapper.mapFirebaseResponseToReceivedPing(newPing))
            }
        }
        currentPings.sortByDescending { it.timestamp }
        _receivedPingsFlow.tryEmit(ReceivedPingsData(currentPings, newPingId))
    }


    @Synchronized
    @WorkerThread
    private fun updateSentPingsList(list: List<DatabasePing>) {
        val currentPings = _sentPingsFlow.value?.toMutableList() ?: mutableListOf()

        list.forEach { newPing ->
            val cachePing = currentPings.find { it.id == newPing.id }
            if (cachePing == null) {
                currentPings.add(newPing)
                pingDao.insertSentPing(mapper.mapFirebaseResponseToSentPing(newPing))
            } else if (cachePing != newPing) {
                currentPings.remove(cachePing)
                currentPings.add(newPing)
                pingDao.insertSentPing(mapper.mapFirebaseResponseToSentPing(newPing))
            }
        }
        currentPings.sortByDescending { it.timestamp }
        _sentPingsFlow.tryEmit(currentPings)
    }

    @Synchronized
    @WorkerThread
    private fun updateScheduledPingsList(cachedScheduledPings: MutableList<DatabasePing>, list: List<DatabasePing>) {
        list.forEach { newPing ->
            val cachePing = cachedScheduledPings.find { it.id == newPing.id }
            if (cachePing == null || cachePing != newPing) {
                cachedScheduledPings.add(newPing)
                pingDao.insertScheduledPing(mapper.mapFirebaseResponseToScheduledPing(newPing))
            }
        }
        cachedScheduledPings.sortByDescending { it.timestamp }
        _scheduledPingsFlow.tryEmit(cachedScheduledPings)
    }

    private fun getPrevItem(cachedScheduledPings: MutableList<DatabasePing>, item: DatabasePing): DatabasePing? {
        val indexOfPrevious = cachedScheduledPings.indexOf(item) - 1
        val prevItem: DatabasePing? =
            if (indexOfPrevious < 0) null else cachedScheduledPings.toList()[indexOfPrevious]
        return prevItem
    }

    private fun getNextItem(cachedScheduledPings: MutableList<DatabasePing>, item: DatabasePing): DatabasePing? {
        val indexOfNext = cachedScheduledPings.indexOf(item) + 1
        return if (indexOfNext < cachedScheduledPings.size) cachedScheduledPings.toList()[indexOfNext] else null
    }

    private fun removeNonExistentScheduledPings(cachedScheduledPings: MutableList<DatabasePing>, list: List<DatabasePing>) {
        cachedScheduledPings.forEach { sentPingScheduledItem ->
            if (!list.contains(sentPingScheduledItem)) {
                val prevItem = getPrevItem(cachedScheduledPings, sentPingScheduledItem)
                val nextItem = getNextItem(cachedScheduledPings, sentPingScheduledItem)
                if ((list.contains(prevItem) || prevItem == null) && (list.contains(nextItem) || nextItem == null)) {
                    cachedScheduledPings.remove(sentPingScheduledItem)
                    pingDao.deleteScheduledPing(sentPingScheduledItem.id)
                    return
                }
            }
        }
    }
}