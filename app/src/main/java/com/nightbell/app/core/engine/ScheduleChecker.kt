package com.nightbell.app.core.engine

import com.nightbell.app.domain.model.Schedule
import java.util.Calendar

object ScheduleChecker {

    fun isInsideActiveWindow(schedule: Schedule, currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        // 1. Quick 8-hour override takes precedence
        if (currentTimeMillis < schedule.quickOverrideUntilMillis) {
            return true
        }

        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1=Sun, 7=Sat
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

        val start = schedule.startMinute
        val end = schedule.endMinute

        val isInTimeWindow = if (start <= end) {
            // Normal interval during the same day (e.g., 09:00 to 17:00)
            currentMinutes in start..end
        } else {
            // Spans midnight (e.g., 23:00 to 08:00)
            currentMinutes >= start || currentMinutes <= end
        }

        if (!isInTimeWindow) return false

        // Determine effective day of week:
        // If window spans midnight and current time is post-midnight (00:00 to end),
        // the shift window originated on the previous calendar day.
        val effectiveDay = if (start > end && currentMinutes <= end) {
            if (currentDayOfWeek == Calendar.SUNDAY) Calendar.SATURDAY else currentDayOfWeek - 1
        } else {
            currentDayOfWeek
        }

        return schedule.activeDays.contains(effectiveDay)
    }
}
