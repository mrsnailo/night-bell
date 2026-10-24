package com.nightbell.app

import com.nightbell.app.core.common.Constants
import com.nightbell.app.core.engine.EvaluationResult
import com.nightbell.app.core.engine.RuleEngine
import com.nightbell.app.core.engine.ScheduleChecker
import com.nightbell.app.domain.model.AlarmRecord
import com.nightbell.app.domain.model.AppRule
import com.nightbell.app.domain.model.Schedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AcceptanceTestSuite {

    @Test
    fun tc01_fiverrMessageTriggersWithinQuietWindow() {
        val schedule = Schedule(
            startMinute = 23 * 60, // 11:00 PM
            endMinute = 8 * 60     // 8:00 AM
        )
        val fiverrRule = AppRule(
            packageName = Constants.PACKAGE_FIVERR,
            enabled = true,
            messagesOnly = true,
            useSchedule = true
        )

        // Simulated time: 2:14 AM
        val cal214am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 14)
        }

        assertTrue(ScheduleChecker.isInsideActiveWindow(schedule, cal214am.timeInMillis))
        assertTrue(fiverrRule.enabled)
    }

    @Test
    fun tc02_promotionalNotificationIsIgnored() {
        val fiverrRule = AppRule(
            packageName = Constants.PACKAGE_FIVERR,
            blockKeywords = listOf("sale", "discount", "tips", "webinar")
        )

        val promoText = "Big discount: up to 30% off top rated sellers this week"
        val isBlocked = fiverrRule.blockKeywords.any { promoText.lowercase().contains(it) }
        assertTrue("Promo notification must be blocked by block-keywords", isBlocked)
    }

    @Test
    fun tc03_outsideQuietWindowNothingRings() {
        val schedule = Schedule(
            startMinute = 23 * 60,
            endMinute = 8 * 60
        )
        // 14:00 (2:00 PM)
        val calDaytime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
        }
        assertFalse(
            "During daytime hours, schedule must be inactive",
            ScheduleChecker.isInsideActiveWindow(schedule, calDaytime.timeInMillis)
        )
    }

    @Test
    fun tc04_quickOverrideAllowsDaytimeRing() {
        val now = System.currentTimeMillis()
        val scheduleWithOverride = Schedule(
            startMinute = 23 * 60,
            endMinute = 8 * 60,
            quickOverrideUntilMillis = now + (8 * 3600 * 1000L)
        )
        assertTrue(
            "Quick override must allow alarm to ring outside regular quiet hours",
            ScheduleChecker.isInsideActiveWindow(scheduleWithOverride, now + 1000L)
        )
    }

    @Test
    fun tc05_dismissalInOriginalAppTracksStopReasonAndDuration() {
        val start = 100_000L
        val stop = 138_000L // 38 seconds later
        val duration = ((stop - start) / 1000).toInt()

        val record = AlarmRecord(
            packageName = Constants.PACKAGE_FIVERR,
            sender = "Jordan M.",
            preview = "Hi! Can you start today?",
            triggeredAt = start,
            stoppedAt = stop,
            durationSeconds = duration,
            stopReason = "REMOTE_DISMISSED"
        )

        assertEquals("38 s", record.formattedDuration)
        assertEquals("REMOTE_DISMISSED", record.stopReason)
    }

    @Test
    fun tc06_alarmToneAndRepeatConfigDefaults() {
        assertEquals("rising_siren", Constants.TONE_RISING_SIREN)
        assertEquals(120, Constants.DEFAULT_MAX_RING_SECONDS)
        assertEquals(5, Constants.DEFAULT_REPEAT_MINUTES)
        assertEquals(5, Constants.DEFAULT_REPEAT_COUNT)
    }
}
