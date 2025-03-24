package com.khrd.pingapp.data.database.pings

import com.khrd.pingapp.data.pings.DatabasePing
import javax.inject.Inject

class PingMapper @Inject constructor() {
    fun mapFirebaseResponseToReceivedPing(firebaseResponse: DatabasePing) = ReceivedPingEntity(
        receivedPingEntityId = firebaseResponse.id ?: "",
        timestamp = firebaseResponse.timestamp,
        from = firebaseResponse.from,
        receivers = firebaseResponse.receivers,
        message = firebaseResponse.message,
        views = firebaseResponse.views,
        groupId = firebaseResponse.groupId,
        scheduledTime = firebaseResponse.scheduledTime,
        groupFrom = firebaseResponse.groupFrom,
        recurringTime = firebaseResponse.recurringTime
    )

    fun mapReceivedPingToFirebaseEntity(receivedPingEntity: ReceivedPingEntity) = DatabasePing(
        id = receivedPingEntity.receivedPingEntityId,
        timestamp = receivedPingEntity.timestamp,
        from = receivedPingEntity.from,
        receivers = receivedPingEntity.receivers,
        message = receivedPingEntity.message,
        views = receivedPingEntity.views,
        groupId = receivedPingEntity.groupId,
        scheduledTime = receivedPingEntity.scheduledTime,
        groupFrom = receivedPingEntity.groupFrom,
        recurringTime = receivedPingEntity.recurringTime
    )

    fun mapFirebaseResponseToSentPing(firebaseResponse: DatabasePing) = SentPingEntity(
        sentPingEntityId = firebaseResponse.id ?: "",
        timestamp = firebaseResponse.timestamp,
        from = firebaseResponse.from,
        receivers = firebaseResponse.receivers,
        message = firebaseResponse.message,
        views = firebaseResponse.views,
        groupId = firebaseResponse.groupId,
        scheduledTime = firebaseResponse.scheduledTime,
        groupFrom = firebaseResponse.groupFrom,
        recurringTime = firebaseResponse.recurringTime
    )

    fun mapSentPingToFirebaseEntity(sentPingEntity: SentPingEntity) = DatabasePing(
        id = sentPingEntity.sentPingEntityId,
        timestamp = sentPingEntity.timestamp,
        from = sentPingEntity.from,
        receivers = sentPingEntity.receivers,
        message = sentPingEntity.message,
        views = sentPingEntity.views,
        groupId = sentPingEntity.groupId,
        scheduledTime = sentPingEntity.scheduledTime,
        groupFrom = sentPingEntity.groupFrom,
        recurringTime = sentPingEntity.recurringTime
    )

    fun mapFirebaseResponseToScheduledPing(firebaseResponse: DatabasePing) = ScheduledPingEntity(
        scheduledPingEntityId = firebaseResponse.id ?: "",
        timestamp = firebaseResponse.timestamp,
        from = firebaseResponse.from,
        receivers = firebaseResponse.receivers,
        message = firebaseResponse.message,
        views = firebaseResponse.views,
        groupId = firebaseResponse.groupId,
        scheduledTime = firebaseResponse.scheduledTime,
        groupFrom = firebaseResponse.groupFrom,
        recurringTime = firebaseResponse.recurringTime
    )

    fun mapScheduledPingToFirebaseEntity(sentPingEntity: ScheduledPingEntity) = DatabasePing(
        id = sentPingEntity.scheduledPingEntityId,
        timestamp = sentPingEntity.timestamp,
        from = sentPingEntity.from,
        receivers = sentPingEntity.receivers,
        message = sentPingEntity.message,
        views = sentPingEntity.views,
        groupId = sentPingEntity.groupId,
        scheduledTime = sentPingEntity.scheduledTime,
        groupFrom = sentPingEntity.groupFrom,
        recurringTime = sentPingEntity.recurringTime
    )
}