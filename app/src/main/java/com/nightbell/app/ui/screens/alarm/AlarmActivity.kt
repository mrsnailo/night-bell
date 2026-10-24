package com.nightbell.app.ui.screens.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nightbell.app.core.common.Constants
import com.nightbell.app.service.AlarmService
import com.nightbell.app.ui.theme.NightBellTheme

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureLockScreenBypass()

        val packageName = intent.getStringExtra(Constants.EXTRA_PACKAGE_NAME) ?: Constants.PACKAGE_FIVERR
        val senderName = intent.getStringExtra(Constants.EXTRA_SENDER_NAME) ?: "Client"
        val messagePreview = intent.getStringExtra(Constants.EXTRA_MESSAGE_PREVIEW) ?: ""

        setContent {
            NightBellTheme {
                AlarmScreen(
                    packageName = packageName,
                    senderName = senderName,
                    messagePreview = messagePreview,
                    onOpenApp = {
                        openTargetApp(packageName)
                    },
                    onSnooze = {
                        snoozeAlarm(packageName, senderName, messagePreview)
                    },
                    onStop = {
                        stopAlarm()
                    }
                )
            }
        }
    }

    private fun configureLockScreenBypass() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun openTargetApp(packageName: String) {
        // 1. Stop the alarm service
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = Constants.ACTION_STOP_ALARM
            putExtra(Constants.EXTRA_STOP_REASON, "APP_OPENED")
        }
        startService(stopIntent)

        // 2. Launch target app if installed
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        }
        finish()
    }

    private fun snoozeAlarm(packageName: String, senderName: String, messagePreview: String) {
        val snoozeIntent = Intent(this, AlarmService::class.java).apply {
            action = Constants.ACTION_SNOOZE_ALARM
            putExtra(Constants.EXTRA_PACKAGE_NAME, packageName)
            putExtra(Constants.EXTRA_SENDER_NAME, senderName)
            putExtra(Constants.EXTRA_MESSAGE_PREVIEW, messagePreview)
        }
        startService(snoozeIntent)
        finish()
    }

    private fun stopAlarm() {
        val stopIntent = Intent(this, AlarmService::class.java).apply {
            action = Constants.ACTION_STOP_ALARM
            putExtra(Constants.EXTRA_STOP_REASON, "USER_STOPPED")
        }
        startService(stopIntent)
        finish()
    }
}
