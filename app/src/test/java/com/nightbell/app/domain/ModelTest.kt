package com.nightbell.app.domain

import com.nightbell.app.core.common.Constants
import com.nightbell.app.domain.model.AlarmRecord
import com.nightbell.app.domain.model.AppRule
import com.nightbell.app.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {

    @Test
    fun testAppRuleDefaultValues() {
        val fiverrRule = AppRule(packageName = Constants.PACKAGE_FIVERR)
        assertTrue(fiverrRule.enabled)
        assertTrue(fiverrRule.messagesOnly)
        assertTrue(fiverrRule.useSchedule)
        assertTrue(fiverrRule.blockKeywords.contains("sale"))
        assertTrue(fiverrRule.blockKeywords.contains("discount"))
    }

    @Test
    fun testAlarmRecordDurationFormatting() {
        val record1 = AlarmRecord(
            packageName = Constants.PACKAGE_FIVERR,
            sender = "Jordan M.",
            preview = "Hi!",
            triggeredAt = 1000L,
            stoppedAt = 39000L,
            durationSeconds = 38
        )
        assertEquals("38 s", record1.formattedDuration)

        val record2 = AlarmRecord(
            packageName = Constants.PACKAGE_UPWORK,
            sender = "Priya S.",
            preview = "Invitation",
            triggeredAt = 1000L,
            stoppedAt = 73000L,
            durationSeconds = 72
        )
        assertEquals("1 m 12 s", record2.formattedDuration)

        val record3 = AlarmRecord(
            packageName = Constants.PACKAGE_FIVERR,
            sender = "Alex R.",
            preview = "Hey",
            triggeredAt = 1000L,
            stoppedAt = 10000L,
            durationSeconds = 9
        )
        assertEquals("9 s", record3.formattedDuration)
    }

    @Test
    fun testScheduleQuickOverride() {
        val activeOverride = Schedule(quickOverrideUntilMillis = System.currentTimeMillis() + 3600000L)
        assertTrue(activeOverride.isOverrideActive)

        val expiredOverride = Schedule(quickOverrideUntilMillis = System.currentTimeMillis() - 1000L)
        assertFalse(expiredOverride.isOverrideActive)
    }
}
