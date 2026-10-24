package com.nightbell.app.domain.model

import com.nightbell.app.core.common.Constants

data class AppRule(
    val packageName: String,
    val enabled: Boolean = true,
    val messagesOnly: Boolean = true,
    val allowKeywords: List<String> = emptyList(),
    val blockKeywords: List<String> = Constants.DEFAULT_BLOCK_KEYWORDS,
    val useSchedule: Boolean = true
)
