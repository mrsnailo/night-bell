package com.nightbell.app.service

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "BootReceiver triggered with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Re-bind notification listener service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    NotificationListenerService.requestRebind(
                        ComponentName(context, NightBellNotificationListener::class.java)
                    )
                    Log.i(TAG, "Requested rebind for NightBellNotificationListener")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to request rebind", e)
                }
            } else {
                // Pre-N workaround: toggle component enabled setting to force framework to rebind
                val component = ComponentName(context, NightBellNotificationListener::class.java)
                val pm = context.packageManager
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
