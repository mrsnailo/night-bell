package com.nightbell.app.core.engine

import com.nightbell.app.core.common.Constants
import com.nightbell.app.domain.model.AppRule
import com.nightbell.app.domain.model.Schedule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RuleEngineTest {

    @Test
    fun testScheduleCheckerMidnightSpanning() {
        // Quiet window: 23:00 (1380m) to 08:00 (480m)
        val schedule = Schedule(
            startMinute = 23 * 60,
            endMinute = 8 * 60
        )

        // 2:00 AM should be active
        val cal2am = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
        }
        assertTrue("2:00 AM should be inside active window", ScheduleChecker.isInsideActiveWindow(schedule, cal2am.timeInMillis))

        // 23:30 PM should be active
        val cal1130pm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 30)
        }
        assertTrue("23:30 PM should be inside active window", ScheduleChecker.isInsideActiveWindow(schedule, cal1130pm.timeInMillis))

        // 14:00 (2:00 PM) should NOT be active
        val cal2pm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
        }
        assertFalse("14:00 should NOT be inside active window", ScheduleChecker.isInsideActiveWindow(schedule, cal2pm.timeInMillis))
    }

    @Test
    fun testScheduleCheckerQuickOverride() {
        val overrideUntil = 100_000_000L
        val schedule = Schedule(
            startMinute = 23 * 60,
            endMinute = 8 * 60,
            quickOverrideUntilMillis = overrideUntil
        )
        // Time before override expiration is active regardless of schedule
        assertTrue(ScheduleChecker.isInsideActiveWindow(schedule, overrideUntil - 5000L))

        // Time after override expiration is not active if outside quiet hours
        val calOutside = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14) // 2:00 PM
            set(Calendar.MINUTE, 0)
        }
        val scheduleExpired = Schedule(
            startMinute = 23 * 60,
            endMinute = 8 * 60,
            quickOverrideUntilMillis = calOutside.timeInMillis - 60000L
        )
        assertFalse(ScheduleChecker.isInsideActiveWindow(scheduleExpired, calOutside.timeInMillis))
    }

    @Test
    fun testRuleBlockKeywords() {
        val rule = AppRule(
            packageName = Constants.PACKAGE_FIVERR,
            blockKeywords = listOf("discount", "sale", "webinar")
        )

        val messageBody = "Huge 50% discount on web development gigs"
        val isBlocked = rule.blockKeywords.any { messageBody.lowercase().contains(it) }
        assertTrue(isBlocked)

        val clientMessage = "Can you start working on my app logo today?"
        val isClientMessageBlocked = rule.blockKeywords.any { clientMessage.lowercase().contains(it) }
        assertFalse(isClientMessageBlocked)
    }
}
