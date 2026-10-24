package com.nightbell.app.domain.model

import com.nightbell.app.core.common.Constants

data class AlarmSettings(
    val toneUri: String = Constants.TONE_RISING_SIREN,
    val volume: Float = Constants.DEFAULT_VOLUME,
    val volumeRamp: Boolean = true,
    val vibrate: Boolean = true,
    val maxRingSeconds: Int = Constants.DEFAULT_MAX_RING_SECONDS,
    val repeatEveryMinutes: Int = Constants.DEFAULT_REPEAT_MINUTES,
    val repeatCount: Int = Constants.DEFAULT_REPEAT_COUNT
)
