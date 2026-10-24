package com.nightbell.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.nightbell.app.core.common.Constants

class AlarmRepeatReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra(Constants.EXTRA_PACKAGE_NAME) ?: return
        val sender = intent.getStringExtra(Constants.EXTRA_SENDER_NAME) ?: "Client"
        val preview = intent.getStringExtra(Constants.EXTRA_MESSAGE_PREVIEW) ?: ""
        val notificationKey = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_KEY) ?: ""
        val currentRepeat = intent.getIntExtra(EXTRA_REPEAT_INDEX, 1)

        Log.i(TAG, "AlarmRepeatReceiver firing repeat iteration #$currentRepeat for $packageName")

        val alarmIntent = Intent(context, AlarmService::class.java).apply {
            action = Constants.ACTION_START_ALARM
            putExtra(Constants.EXTRA_PACKAGE_NAME, packageName)
            putExtra(Constants.EXTRA_SENDER_NAME, sender)
            putExtra(Constants.EXTRA_MESSAGE_PREVIEW, preview)
            putExtra(Constants.EXTRA_NOTIFICATION_KEY, notificationKey)
            putExtra(EXTRA_REPEAT_INDEX, currentRepeat)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(alarmIntent)
        } else {
            context.startService(alarmIntent)
        }
    }

    companion object {
        private const val TAG = "AlarmRepeatReceiver"
        const val EXTRA_REPEAT_INDEX = "extra_repeat_index"
    }
}
