package com.nightbell.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nightbell.app.core.common.Constants
import com.nightbell.app.domain.model.AlarmSettings
import com.nightbell.app.domain.model.AppRule
import com.nightbell.app.domain.model.Schedule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nightbell_settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val FIVERR_ENABLED = booleanPreferencesKey("fiverr_enabled")
        val FIVERR_MESSAGES_ONLY = booleanPreferencesKey("fiverr_messages_only")
        val FIVERR_USE_SCHEDULE = booleanPreferencesKey("fiverr_use_schedule")
        val FIVERR_IGNORE_WORDS = stringSetPreferencesKey("fiverr_ignore_words")

        val UPWORK_ENABLED = booleanPreferencesKey("upwork_enabled")
        val UPWORK_MESSAGES_ONLY = booleanPreferencesKey("upwork_messages_only")
        val UPWORK_USE_SCHEDULE = booleanPreferencesKey("upwork_use_schedule")
        val UPWORK_IGNORE_WORDS = stringSetPreferencesKey("upwork_ignore_words")

        val SCHEDULE_START_MINUTE = intPreferencesKey("schedule_start_minute")
        val SCHEDULE_END_MINUTE = intPreferencesKey("schedule_end_minute")
        val SCHEDULE_ACTIVE_DAYS = stringSetPreferencesKey("schedule_active_days")
        val SCHEDULE_OVERRIDE_UNTIL = longPreferencesKey("schedule_override_until")

        val ALARM_TONE_URI = stringPreferencesKey("alarm_tone_uri")
        val ALARM_VOLUME = floatPreferencesKey("alarm_volume")
        val ALARM_VOLUME_RAMP = booleanPreferencesKey("alarm_volume_ramp")
        val ALARM_VIBRATE = booleanPreferencesKey("alarm_vibrate")
        val ALARM_MAX_RING_SECONDS = intPreferencesKey("alarm_max_ring_seconds")
        val ALARM_REPEAT_MINUTES = intPreferencesKey("alarm_repeat_minutes")
        val ALARM_REPEAT_COUNT = intPreferencesKey("alarm_repeat_count")

        val LAST_NOTIFICATION_SEEN = longPreferencesKey("last_notification_seen")
    }

    val fiverrRuleFlow: Flow<AppRule> = context.dataStore.data.map { prefs ->
        AppRule(
            packageName = Constants.PACKAGE_FIVERR,
            enabled = prefs[PreferencesKeys.FIVERR_ENABLED] ?: true,
            messagesOnly = prefs[PreferencesKeys.FIVERR_MESSAGES_ONLY] ?: true,
            blockKeywords = (prefs[PreferencesKeys.FIVERR_IGNORE_WORDS] ?: Constants.DEFAULT_BLOCK_KEYWORDS.toSet()).toList(),
            useSchedule = prefs[PreferencesKeys.FIVERR_USE_SCHEDULE] ?: true
        )
    }

    val upworkRuleFlow: Flow<AppRule> = context.dataStore.data.map { prefs ->
        AppRule(
            packageName = Constants.PACKAGE_UPWORK,
            enabled = prefs[PreferencesKeys.UPWORK_ENABLED] ?: true,
            messagesOnly = prefs[PreferencesKeys.UPWORK_MESSAGES_ONLY] ?: true,
            blockKeywords = (prefs[PreferencesKeys.UPWORK_IGNORE_WORDS] ?: Constants.DEFAULT_BLOCK_KEYWORDS.toSet()).toList(),
            useSchedule = prefs[PreferencesKeys.UPWORK_USE_SCHEDULE] ?: true
        )
    }

    val scheduleFlow: Flow<Schedule> = context.dataStore.data.map { prefs ->
        val defaultDays = setOf("1", "2", "3", "4", "5", "6", "7")
        val savedDays = prefs[PreferencesKeys.SCHEDULE_ACTIVE_DAYS] ?: defaultDays
        val intDays = savedDays.mapNotNull { it.toIntOrNull() }.toSet()

        Schedule(
            startMinute = prefs[PreferencesKeys.SCHEDULE_START_MINUTE] ?: Constants.DEFAULT_START_MINUTE,
            endMinute = prefs[PreferencesKeys.SCHEDULE_END_MINUTE] ?: Constants.DEFAULT_END_MINUTE,
            activeDays = if (intDays.isEmpty()) defaultDays.map { it.toInt() }.toSet() else intDays,
            quickOverrideUntilMillis = prefs[PreferencesKeys.SCHEDULE_OVERRIDE_UNTIL] ?: 0L
        )
    }

    val alarmSettingsFlow: Flow<AlarmSettings> = context.dataStore.data.map { prefs ->
        AlarmSettings(
            toneUri = prefs[PreferencesKeys.ALARM_TONE_URI] ?: Constants.TONE_RISING_SIREN,
            volume = prefs[PreferencesKeys.ALARM_VOLUME] ?: Constants.DEFAULT_VOLUME,
            volumeRamp = prefs[PreferencesKeys.ALARM_VOLUME_RAMP] ?: true,
            vibrate = prefs[PreferencesKeys.ALARM_VIBRATE] ?: true,
            maxRingSeconds = prefs[PreferencesKeys.ALARM_MAX_RING_SECONDS] ?: Constants.DEFAULT_MAX_RING_SECONDS,
            repeatEveryMinutes = prefs[PreferencesKeys.ALARM_REPEAT_MINUTES] ?: Constants.DEFAULT_REPEAT_MINUTES,
            repeatCount = prefs[PreferencesKeys.ALARM_REPEAT_COUNT] ?: Constants.DEFAULT_REPEAT_COUNT
        )
    }

    val lastNotificationSeenFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.LAST_NOTIFICATION_SEEN] ?: 0L
    }

    suspend fun setAppEnabled(packageName: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            if (packageName == Constants.PACKAGE_FIVERR) {
                prefs[PreferencesKeys.FIVERR_ENABLED] = enabled
            } else if (packageName == Constants.PACKAGE_UPWORK) {
                prefs[PreferencesKeys.UPWORK_ENABLED] = enabled
            }
        }
    }

    suspend fun setMessagesOnly(packageName: String, messagesOnly: Boolean) {
        context.dataStore.edit { prefs ->
            if (packageName == Constants.PACKAGE_FIVERR) {
                prefs[PreferencesKeys.FIVERR_MESSAGES_ONLY] = messagesOnly
            } else if (packageName == Constants.PACKAGE_UPWORK) {
                prefs[PreferencesKeys.UPWORK_MESSAGES_ONLY] = messagesOnly
            }
        }
    }

    suspend fun setUseSchedule(packageName: String, useSchedule: Boolean) {
        context.dataStore.edit { prefs ->
            if (packageName == Constants.PACKAGE_FIVERR) {
                prefs[PreferencesKeys.FIVERR_USE_SCHEDULE] = useSchedule
            } else if (packageName == Constants.PACKAGE_UPWORK) {
                prefs[PreferencesKeys.UPWORK_USE_SCHEDULE] = useSchedule
            }
        }
    }

    suspend fun addIgnoreWord(packageName: String, word: String) {
        val trimmed = word.trim().lowercase()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            val key = if (packageName == Constants.PACKAGE_FIVERR) {
                PreferencesKeys.FIVERR_IGNORE_WORDS
            } else {
                PreferencesKeys.UPWORK_IGNORE_WORDS
            }
            val current = prefs[key]?.toMutableSet() ?: Constants.DEFAULT_BLOCK_KEYWORDS.toMutableSet()
            current.add(trimmed)
            prefs[key] = current
        }
    }

    suspend fun removeIgnoreWord(packageName: String, word: String) {
        val trimmed = word.trim().lowercase()
        context.dataStore.edit { prefs ->
            val key = if (packageName == Constants.PACKAGE_FIVERR) {
                PreferencesKeys.FIVERR_IGNORE_WORDS
            } else {
                PreferencesKeys.UPWORK_IGNORE_WORDS
            }
            val current = prefs[key]?.toMutableSet() ?: Constants.DEFAULT_BLOCK_KEYWORDS.toMutableSet()
            current.remove(trimmed)
            prefs[key] = current
        }
    }

    suspend fun setScheduleHours(startMinute: Int, endMinute: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SCHEDULE_START_MINUTE] = startMinute
            prefs[PreferencesKeys.SCHEDULE_END_MINUTE] = endMinute
        }
    }

    suspend fun setQuickOverrideHours(hours: Int) {
        context.dataStore.edit { prefs ->
            val overrideTime = System.currentTimeMillis() + (hours * 3600 * 1000L)
            prefs[PreferencesKeys.SCHEDULE_OVERRIDE_UNTIL] = overrideTime
        }
    }

    suspend fun clearQuickOverride() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SCHEDULE_OVERRIDE_UNTIL] = 0L
        }
    }

    suspend fun setAlarmTone(toneUri: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ALARM_TONE_URI] = toneUri
        }
    }

    suspend fun setAlarmVolume(volume: Float) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ALARM_VOLUME] = volume.coerceIn(0.1f, 1.0f)
        }
    }

    suspend fun setRepeatConfig(everyMin: Int, count: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ALARM_REPEAT_MINUTES] = everyMin
            prefs[PreferencesKeys.ALARM_REPEAT_COUNT] = count
        }
    }

    suspend fun recordNotificationSeen(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.LAST_NOTIFICATION_SEEN] = timestamp
        }
    }
}
