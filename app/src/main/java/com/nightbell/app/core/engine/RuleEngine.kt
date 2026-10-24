package com.nightbell.app.core.engine

import android.app.Notification
import android.service.notification.StatusBarNotification
import com.nightbell.app.domain.model.AppRule
import com.nightbell.app.domain.model.Schedule

sealed interface EvaluationResult {
    data class Trigger(
        val packageName: String,
        val sender: String,
        val messagePreview: String,
        val notificationKey: String
    ) : EvaluationResult

    data class Ignored(val reason: String) : EvaluationResult
}

object RuleEngine {

    fun evaluate(
        sbn: StatusBarNotification,
        rule: AppRule,
        schedule: Schedule,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): EvaluationResult {
        // 1. App rule enabled check
        if (!rule.enabled) {
            return EvaluationResult.Ignored("Rule disabled for package: ${rule.packageName}")
        }

        // 2. Package matching
        if (sbn.packageName != rule.packageName) {
            return EvaluationResult.Ignored("Package mismatch: expected ${rule.packageName}, got ${sbn.packageName}")
        }

        // 3. Schedule verification
        if (rule.useSchedule && !ScheduleChecker.isInsideActiveWindow(schedule, currentTimeMillis)) {
            return EvaluationResult.Ignored("Outside active schedule window")
        }

        // 4. Extract notification extras
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim() ?: ""

        val combinedContent = "$title $text $bigText $subText".lowercase()

        // 5. Messages-only heuristic
        if (rule.messagesOnly) {
            val category = sbn.notification.category
            if (category == Notification.CATEGORY_PROMO ||
                category == Notification.CATEGORY_RECOMMENDATION ||
                category == Notification.CATEGORY_SYSTEM
            ) {
                return EvaluationResult.Ignored("Ignored promo/system notification category: $category")
            }

            // Fiverr and Upwork client messages always feature a sender title
            if (title.isBlank()) {
                return EvaluationResult.Ignored("Missing sender title; likely marketing or system broadcast")
            }
        }

        // 6. Keyword Blocklist check (case-insensitive substring)
        for (blocked in rule.blockKeywords) {
            val trimmedBlock = blocked.trim().lowercase()
            if (trimmedBlock.isNotEmpty() && combinedContent.contains(trimmedBlock)) {
                return EvaluationResult.Ignored("Notification content blocked by keyword: '$blocked'")
            }
        }

        // 7. Keyword Allowlist check (if specified)
        if (rule.allowKeywords.isNotEmpty()) {
            val matchesAllowlist = rule.allowKeywords.any {
                val trimmedAllow = it.trim().lowercase()
                trimmedAllow.isNotEmpty() && combinedContent.contains(trimmedAllow)
            }
            if (!matchesAllowlist) {
                return EvaluationResult.Ignored("Notification does not contain any allowlist keywords")
            }
        }

        // 8. Determine best message preview
        val messagePreview = when {
            bigText.isNotBlank() -> bigText
            text.isNotBlank() -> text
            else -> "New client message"
        }

        return EvaluationResult.Trigger(
            packageName = sbn.packageName,
            sender = if (title.isNotBlank()) title else "Client",
            messagePreview = messagePreview,
            notificationKey = sbn.key
        )
    }
}
