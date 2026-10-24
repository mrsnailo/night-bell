package com.nightbell.app.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nightbell.app.NightBellApp
import com.nightbell.app.core.common.Constants
import com.nightbell.app.core.engine.ScheduleChecker
import com.nightbell.app.data.repository.HealthRepository
import com.nightbell.app.domain.model.AlarmRecord
import com.nightbell.app.domain.model.AppRule
import com.nightbell.app.domain.model.Schedule
import com.nightbell.app.service.AlarmService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val isListening: Boolean = true,
    val scheduleSubtitle: String = "Alarm is on until 8:00 AM",
    val healthText: String = "Listening for notifications",
    val fiverrRule: AppRule = AppRule(packageName = Constants.PACKAGE_FIVERR),
    val upworkRule: AppRule = AppRule(packageName = Constants.PACKAGE_UPWORK),
    val lastAlarm: AlarmRecord? = null
)

class HomeViewModel : ViewModel() {

    private val dataStore = NightBellApp.instance.settingsDataStore
    private val healthRepo = NightBellApp.instance.healthRepository
    private val historyRepo = NightBellApp.instance.alarmHistoryRepository

    val uiState: StateFlow<HomeUiState> = combine(
        dataStore.fiverrRuleFlow,
        dataStore.upworkRuleFlow,
        dataStore.scheduleFlow,
        healthRepo.healthStatusFlow,
        historyRepo.allEventsFlow
    ) { fiverr, upwork, schedule, health, events ->
        val isListening = (fiverr.enabled || upwork.enabled) &&
                ScheduleChecker.isInsideActiveWindow(schedule)

        val endHour = schedule.endMinute / 60
        val endMin = schedule.endMinute % 60
        val amPm = if (endHour >= 12) "PM" else "AM"
        val displayHour = if (endHour % 12 == 0) 12 else endHour % 12
        val formattedTime = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, endMin, amPm)

        val scheduleSubtitle = if (schedule.isOverrideActive) {
            "Quick 8-hour override is active"
        } else if (isListening) {
            "Alarm is on until $formattedTime"
        } else {
            "Scheduled off until 11:00 PM"
        }

        val healthText = when (health) {
            is HealthRepository.HealthStatus.Healthy -> health.message
            is HealthRepository.HealthStatus.Warning -> health.message
            HealthRepository.HealthStatus.NoNotificationsYet -> "No notifications captured yet"
        }

        HomeUiState(
            isListening = isListening,
            scheduleSubtitle = scheduleSubtitle,
            healthText = healthText,
            fiverrRule = fiverr,
            upworkRule = upwork,
            lastAlarm = events.firstOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun toggleApp(packageName: String, currentEnabled: Boolean) {
        viewModelScope.launch {
            dataStore.setAppEnabled(packageName, !currentEnabled)
        }
    }

    fun triggerTestAlarm(context: Context) {
        val testIntent = Intent(context, AlarmService::class.java).apply {
            action = Constants.ACTION_TEST_ALARM
        }
        context.startService(testIntent)
    }
}
