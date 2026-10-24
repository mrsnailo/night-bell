package com.nightbell.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.nightbell.app.core.database.NightBellDatabase
import com.nightbell.app.data.datastore.SettingsDataStore
import com.nightbell.app.data.repository.AlarmHistoryRepository
import com.nightbell.app.data.repository.HealthRepository

class NightBellApp : Application() {

    lateinit var settingsDataStore: SettingsDataStore
        private set

    lateinit var database: NightBellDatabase
        private set

    lateinit var alarmHistoryRepository: AlarmHistoryRepository
        private set

    lateinit var healthRepository: HealthRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        settingsDataStore = SettingsDataStore(this)
        database = NightBellDatabase(this)
        alarmHistoryRepository = AlarmHistoryRepository(database)
        healthRepository = HealthRepository(settingsDataStore)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "NightBell Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Foreground alerts and persistent ringing notifications"
                setSound(null, null)
                enableVibration(true)
            }

            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "NightBell Background Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays status while an alarm is ringing or monitoring"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        lateinit var instance: NightBellApp
            private set

        const val ALARM_CHANNEL_ID = "nightbell_alarm_channel"
        const val SERVICE_CHANNEL_ID = "nightbell_service_channel"
    }
}
