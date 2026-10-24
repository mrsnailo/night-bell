package com.nightbell.app.core.common

object Constants {
    const val PACKAGE_FIVERR = "com.fiverr.fiverr"
    const val PACKAGE_UPWORK = "com.upwork.android.apps.main"

    val DEFAULT_BLOCK_KEYWORDS = listOf("sale", "discount", "tips", "webinar")

    const val DEFAULT_START_MINUTE = 23 * 60 // 11:00 PM
    const val DEFAULT_END_MINUTE = 8 * 60    // 8:00 AM

    const val DEFAULT_MAX_RING_SECONDS = 120 // 2 minutes
    const val DEFAULT_REPEAT_MINUTES = 5     // every 5 min
    const val DEFAULT_REPEAT_COUNT = 5       // up to 5 times
    const val DEFAULT_VOLUME = 0.85f         // 85%

    const val TONE_RISING_SIREN = "rising_siren"
    const val TONE_DIGITAL_URGENCY = "digital_urgency"
    const val TONE_EMERGENCY_BEACON = "emergency_beacon"

    const val ACTION_START_ALARM = "com.nightbell.app.ACTION_START_ALARM"
    const val ACTION_STOP_ALARM = "com.nightbell.app.ACTION_STOP_ALARM"
    const val ACTION_SNOOZE_ALARM = "com.nightbell.app.ACTION_SNOOZE_ALARM"
    const val ACTION_TEST_ALARM = "com.nightbell.app.ACTION_TEST_ALARM"

    const val EXTRA_PACKAGE_NAME = "extra_package_name"
    const val EXTRA_SENDER_NAME = "extra_sender_name"
    const val EXTRA_MESSAGE_PREVIEW = "extra_message_preview"
    const val EXTRA_NOTIFICATION_KEY = "extra_notification_key"
    const val EXTRA_STOP_REASON = "extra_stop_reason"
}
