package com.nightbell.app.service

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nightbell.app.NightBellApp
import com.nightbell.app.R
import com.nightbell.app.core.audio.AlarmAudioPlayer
import com.nightbell.app.core.audio.VibratorController
import com.nightbell.app.core.common.Constants
import com.nightbell.app.ui.screens.alarm.AlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var audioPlayer: AlarmAudioPlayer
    private lateinit var vibratorController: VibratorController
    private var wakeLock: PowerManager.WakeLock? = null

    private var activeEventId: Long = 0L
    private var activeNotificationKey: String = ""
    private var timeoutJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        audioPlayer = AlarmAudioPlayer(this)
        vibratorController = VibratorController(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "NightBell:AlarmWakeLock").apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY

        when (intent.action) {
            Constants.ACTION_START_ALARM -> {
                handleStartAlarm(intent)
            }
            Constants.ACTION_STOP_ALARM -> {
                val key = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_KEY)
                val reason = intent.getStringExtra(Constants.EXTRA_STOP_REASON) ?: "USER_STOPPED"
                // If key is supplied, only stop if it matches the current ringing notification
                if (key == null || key == activeNotificationKey) {
                    handleStopAlarm(reason)
                }
            }
            Constants.ACTION_SNOOZE_ALARM -> {
                handleSnoozeAlarm(intent)
            }
            Constants.ACTION_TEST_ALARM -> {
                handleTestAlarm()
            }
        }

        return START_NOT_STICKY
    }

    private fun handleStartAlarm(intent: Intent) {
        val packageName = intent.getStringExtra(Constants.EXTRA_PACKAGE_NAME) ?: Constants.PACKAGE_FIVERR
        val sender = intent.getStringExtra(Constants.EXTRA_SENDER_NAME) ?: "Client"
        val preview = intent.getStringExtra(Constants.EXTRA_MESSAGE_PREVIEW) ?: ""
        val notificationKey = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_KEY) ?: ""
        val repeatIndex = intent.getIntExtra(AlarmRepeatReceiver.EXTRA_REPEAT_INDEX, 1)

        activeNotificationKey = notificationKey

        wakeLock?.acquire(3 * 60 * 1000L) // 3 minutes max wake lock

        // 1. Build Foreground Notification
        val notification = buildAlarmNotification(packageName, sender, preview)
        startForeground(NOTIFICATION_ID, notification)

        // 2. Launch Full Screen AlarmActivity
        launchAlarmActivity(packageName, sender, preview)

        // 3. Play Audio & Haptics
        serviceScope.launch {
            val settings = NightBellApp.instance.settingsDataStore.alarmSettingsFlow.first()
            audioPlayer.play(
                toneName = settings.toneUri,
                targetVolume = settings.volume,
                enableRamp = settings.volumeRamp
            )
            if (settings.vibrate) {
                vibratorController.startVibration()
            }

            // Log event in database if not already tracked
            if (activeEventId == 0L) {
                activeEventId = NightBellApp.instance.alarmHistoryRepository.logAlarmTrigger(
                    packageName = packageName,
                    sender = sender,
                    preview = preview
                )
            }

            // Start 2-minute auto timeout
            timeoutJob?.cancel()
            timeoutJob = serviceScope.launch {
                delay(settings.maxRingSeconds * 1000L)
                handleRingTimeout(packageName, sender, preview, notificationKey, repeatIndex, settings.repeatCount, settings.repeatEveryMinutes)
            }
        }
    }

    private fun handleTestAlarm() {
        val packageName = Constants.PACKAGE_FIVERR
        val sender = "Test Client"
        val preview = "This is a test notification from NightBell."

        wakeLock?.acquire(15 * 1000L)

        val notification = buildAlarmNotification(packageName, sender, preview)
        startForeground(NOTIFICATION_ID, notification)
        launchAlarmActivity(packageName, sender, preview)

        serviceScope.launch {
            val settings = NightBellApp.instance.settingsDataStore.alarmSettingsFlow.first()
            audioPlayer.play(toneName = settings.toneUri, targetVolume = settings.volume, enableRamp = false)
            vibratorController.startVibration()

            delay(5000L) // Ring for 5 seconds
            handleStopAlarm("TEST_COMPLETED")
        }
    }

    private fun handleStopAlarm(stopReason: String) {
        Log.i(TAG, "Stopping alarm with reason: $stopReason")
        timeoutJob?.cancel()
        timeoutJob = null

        audioPlayer.stop()
        vibratorController.stopVibration()

        serviceScope.launch {
            if (activeEventId != 0L) {
                NightBellApp.instance.alarmHistoryRepository.logAlarmStopped(
                    id = activeEventId,
                    stopReason = stopReason
                )
                activeEventId = 0L
            }
        }

        wakeLock?.let {
            if (it.isHeld) it.release()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleSnoozeAlarm(intent: Intent) {
        val packageName = intent.getStringExtra(Constants.EXTRA_PACKAGE_NAME) ?: Constants.PACKAGE_FIVERR
        val sender = intent.getStringExtra(Constants.EXTRA_SENDER_NAME) ?: "Client"
        val preview = intent.getStringExtra(Constants.EXTRA_MESSAGE_PREVIEW) ?: ""
        val notificationKey = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_KEY) ?: activeNotificationKey

        Log.i(TAG, "Snoozing alarm for 5 minutes")
        handleStopAlarm("SNOOZED")
        scheduleRepeat(packageName, sender, preview, notificationKey, repeatIndex = 1, delayMinutes = 5)
    }

    private fun handleRingTimeout(
        packageName: String,
        sender: String,
        preview: String,
        notificationKey: String,
        currentRepeat: Int,
        maxRepeats: Int,
        intervalMinutes: Int
    ) {
        Log.i(TAG, "Alarm ring timed out after 2 minutes")
        audioPlayer.stop()
        vibratorController.stopVibration()

        wakeLock?.let {
            if (it.isHeld) it.release()
        }

        stopForeground(STOP_FOREGROUND_REMOVE)

        if (currentRepeat < maxRepeats) {
            scheduleRepeat(packageName, sender, preview, notificationKey, currentRepeat + 1, intervalMinutes)
        } else {
            handleStopAlarm("TIMEOUT_MAX_REPEATS")
        }
    }

    private fun scheduleRepeat(
        packageName: String,
        sender: String,
        preview: String,
        notificationKey: String,
        repeatIndex: Int,
        delayMinutes: Int
    ) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmRepeatReceiver::class.java).apply {
            putExtra(Constants.EXTRA_PACKAGE_NAME, packageName)
            putExtra(Constants.EXTRA_SENDER_NAME, sender)
            putExtra(Constants.EXTRA_MESSAGE_PREVIEW, preview)
            putExtra(Constants.EXTRA_NOTIFICATION_KEY, notificationKey)
            putExtra(AlarmRepeatReceiver.EXTRA_REPEAT_INDEX, repeatIndex)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_ID + repeatIndex,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
        Log.i(TAG, "Scheduled repeat alarm #$repeatIndex in $delayMinutes minutes")
    }

    private fun buildAlarmNotification(packageName: String, sender: String, preview: String): Notification {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = Constants.ACTION_STOP_ALARM
            putExtra(Constants.EXTRA_STOP_REASON, "USER_STOPPED_NOTIFICATION")
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            101,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.EXTRA_PACKAGE_NAME, packageName)
            putExtra(Constants.EXTRA_SENDER_NAME, sender)
            putExtra(Constants.EXTRA_MESSAGE_PREVIEW, preview)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            102,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isFiverr = packageName.contains("fiverr")
        val appTitle = if (isFiverr) "Fiverr Message" else "Upwork Message"

        return NotificationCompat.Builder(this, NightBellApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Incoming $appTitle from $sender")
            .setContentText(preview.ifEmpty { "High urgency client alert" })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .build()
    }

    private fun launchAlarmActivity(packageName: String, sender: String, preview: String) {
        val activityIntent = Intent(this, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(Constants.EXTRA_PACKAGE_NAME, packageName)
            putExtra(Constants.EXTRA_SENDER_NAME, sender)
            putExtra(Constants.EXTRA_MESSAGE_PREVIEW, preview)
        }
        startActivity(activityIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.stop()
        vibratorController.stopVibration()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AlarmService"
        private const val NOTIFICATION_ID = 9001
    }
}
