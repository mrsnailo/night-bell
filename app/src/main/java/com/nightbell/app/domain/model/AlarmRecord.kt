package com.nightbell.app.domain.model

data class AlarmRecord(
    val id: Long = 0,
    val packageName: String,
    val sender: String,
    val preview: String,
    val triggeredAt: Long,
    val stoppedAt: Long = 0,
    val durationSeconds: Int = 0,
    val stopReason: String = "ACTIVE"
) {
    val isFiverr: Boolean get() = packageName.contains("fiverr")
    val isUpwork: Boolean get() = packageName.contains("upwork")

    val formattedDuration: String
        get() {
            if (durationSeconds <= 0) return "0 s"
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return if (mins > 0) {
                if (secs > 0) "${mins} m ${secs} s" else "${mins} m"
            } else {
                "${secs} s"
            }
        }
}
