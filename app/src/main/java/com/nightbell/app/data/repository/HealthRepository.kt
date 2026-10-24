package com.nightbell.app.data.repository

import com.nightbell.app.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HealthRepository(private val settingsDataStore: SettingsDataStore) {

    val lastSeenTimestampFlow: Flow<Long> = settingsDataStore.lastNotificationSeenFlow

    val healthStatusFlow: Flow<HealthStatus> = lastSeenTimestampFlow.map { timestamp ->
        if (timestamp <= 0L) {
            HealthStatus.NoNotificationsYet
        } else {
            val diffMillis = System.currentTimeMillis() - timestamp
            val diffMinutes = diffMillis / (60 * 1000)
            if (diffMinutes < 60) {
                HealthStatus.Healthy("Last notification seen ${diffMinutes.coerceAtLeast(1)} min ago")
            } else if (diffMinutes < 24 * 60) {
                val diffHours = diffMinutes / 60
                HealthStatus.Healthy("Last notification seen $diffHours hr ago")
            } else {
                HealthStatus.Warning("No notifications seen in >24 hours. Check notification access.")
            }
        }
    }

    suspend fun recordNotificationObserved(timestamp: Long = System.currentTimeMillis()) {
        settingsDataStore.recordNotificationSeen(timestamp)
    }

    sealed interface HealthStatus {
        data object NoNotificationsYet : HealthStatus
        data class Healthy(val message: String) : HealthStatus
        data class Warning(val message: String) : HealthStatus
    }
}
