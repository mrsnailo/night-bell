package com.nightbell.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NightBellNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }
}
