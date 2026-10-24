package com.nightbell.app.domain.model

import com.nightbell.app.core.common.Constants
import java.util.Calendar

data class Schedule(
    val startMinute: Int = Constants.DEFAULT_START_MINUTE,
    val endMinute: Int = Constants.DEFAULT_END_MINUTE,
    val activeDays: Set<Int> = setOf(
        Calendar.SUNDAY,
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY
    ),
    val quickOverrideUntilMillis: Long = 0L
) {
    val isOverrideActive: Boolean
        get() = System.currentTimeMillis() < quickOverrideUntilMillis
}
