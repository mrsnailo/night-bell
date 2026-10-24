package com.nightbell.app.service

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.nightbell.app.NightBellApp
import com.nightbell.app.core.common.Constants
import com.nightbell.app.core.engine.EvaluationResult
import com.nightbell.app.core.engine.RuleEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NightBellNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected successfully")
        NightBellApp.instance.healthRepository.let { repo ->
            serviceScope.launch {
                repo.recordNotificationObserved()
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected; requesting rebind")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestRebind(ComponentName(this, NightBellNotificationListener::class.java))
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Record health timestamp for ANY incoming notification seen on device
        serviceScope.launch {
            try {
                NightBellApp.instance.healthRepository.recordNotificationObserved()
            } catch (e: Exception) {
                Log.e(TAG, "Error recording health timestamp", e)
            }
        }

        val pkg = sbn.packageName
        if (pkg != Constants.PACKAGE_FIVERR && pkg != Constants.PACKAGE_UPWORK) {
            return
        }

        Log.d(TAG, "Intercepted notification from target package: $pkg")

        serviceScope.launch {
            try {
                val dataStore = NightBellApp.instance.settingsDataStore
                val rule = if (pkg == Constants.PACKAGE_FIVERR) {
                    dataStore.fiverrRuleFlow.first()
                } else {
                    dataStore.upworkRuleFlow.first()
                }
                val schedule = dataStore.scheduleFlow.first()

                when (val result = RuleEngine.evaluate(sbn, rule, schedule)) {
                    is EvaluationResult.Trigger -> {
                        Log.i(TAG, "Rule matched! Triggering alarm for $pkg from sender: ${result.sender}")
                        startAlarm(result)
                    }
                    is EvaluationResult.Ignored -> {
                        Log.d(TAG, "Notification ignored: ${result.reason}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error evaluating incoming notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName
        if (pkg == Constants.PACKAGE_FIVERR || pkg == Constants.PACKAGE_UPWORK) {
            Log.i(TAG, "Target notification removed for $pkg with key: ${sbn.key}")
            // Signal AlarmService to stop if this matches the ringing notification
            val stopIntent = Intent(this, AlarmService::class.java).apply {
                action = Constants.ACTION_STOP_ALARM
                putExtra(Constants.EXTRA_NOTIFICATION_KEY, sbn.key)
                putExtra(Constants.EXTRA_STOP_REASON, "REMOTE_DISMISSED")
            }
            startService(stopIntent)
        }
    }

    private fun startAlarm(trigger: EvaluationResult.Trigger) {
        val alarmIntent = Intent(this, AlarmService::class.java).apply {
            action = Constants.ACTION_START_ALARM
            putExtra(Constants.EXTRA_PACKAGE_NAME, trigger.packageName)
            putExtra(Constants.EXTRA_SENDER_NAME, trigger.sender)
            putExtra(Constants.EXTRA_MESSAGE_PREVIEW, trigger.messagePreview)
            putExtra(Constants.EXTRA_NOTIFICATION_KEY, trigger.notificationKey)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(alarmIntent)
        } else {
            startService(alarmIntent)
        }
    }

    companion object {
        private const val TAG = "NightBellListener"
    }
}
