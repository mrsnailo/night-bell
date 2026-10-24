package com.nightbell.app.data.repository

import com.nightbell.app.core.database.NightBellDatabase
import com.nightbell.app.domain.model.AlarmRecord
import kotlinx.coroutines.flow.Flow

class AlarmHistoryRepository(private val database: NightBellDatabase) {

    val allEventsFlow: Flow<List<AlarmRecord>> = database.eventsFlow

    suspend fun logAlarmTrigger(
        packageName: String,
        sender: String,
        preview: String,
        triggeredAt: Long = System.currentTimeMillis()
    ): Long {
        val record = AlarmRecord(
            packageName = packageName,
            sender = sender,
            preview = preview,
            triggeredAt = triggeredAt,
            stoppedAt = 0,
            durationSeconds = 0,
            stopReason = "ACTIVE"
        )
        return database.insertEvent(record)
    }

    suspend fun logAlarmStopped(
        id: Long,
        stoppedAt: Long = System.currentTimeMillis(),
        stopReason: String
    ) {
        database.updateEventStop(id, stoppedAt, stopReason)
    }

    suspend fun getAllEvents(): List<AlarmRecord> {
        return database.getAllEvents()
    }

    suspend fun getEventsForPackage(packageName: String): List<AlarmRecord> {
        return database.getEventsForPackage(packageName)
    }

    suspend fun getLastAlarm(): AlarmRecord? {
        return database.getLastAlarm()
    }
}
