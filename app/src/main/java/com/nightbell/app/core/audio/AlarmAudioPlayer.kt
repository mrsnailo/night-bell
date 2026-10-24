package com.nightbell.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.nightbell.app.R
import com.nightbell.app.core.common.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AlarmAudioPlayer(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private var rampJob: Job? = null

    fun play(
        toneName: String = Constants.TONE_RISING_SIREN,
        targetVolume: Float = Constants.DEFAULT_VOLUME,
        enableRamp: Boolean = true
    ) {
        stop()

        requestAlarmAudioFocus()

        val soundRes = resolveSoundResource(toneName)
        try {
            val player = MediaPlayer.create(
                context,
                soundRes,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
                audioManager.generateAudioSessionId()
            ) ?: run {
                Log.e(TAG, "Failed to create MediaPlayer for resource $soundRes")
                return
            }

            mediaPlayer = player
            player.isLooping = true

            val clampedTarget = targetVolume.coerceIn(0.1f, 1.0f)

            if (enableRamp) {
                val startVolume = (clampedTarget * 0.4f).coerceAtLeast(0.1f)
                player.setVolume(startVolume, startVolume)
                player.start()
                startVolumeRamp(startVolume, clampedTarget, 20_000L)
            } else {
                player.setVolume(clampedTarget, clampedTarget)
                player.start()
            }
            Log.i(TAG, "Alarm audio playback started (tone=$toneName, targetVolume=$clampedTarget, ramp=$enableRamp)")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting alarm audio player", e)
        }
    }

    private fun startVolumeRamp(startVol: Float, targetVol: Float, durationMs: Long) {
        rampJob?.cancel()
        rampJob = audioScope.launch {
            val steps = 20
            val interval = durationMs / steps
            val increment = (targetVol - startVol) / steps
            var current = startVol

            for (i in 1..steps) {
                delay(interval)
                if (!isActive || mediaPlayer == null) break
                current += increment
                val vol = current.coerceAtMost(targetVol)
                mediaPlayer?.setVolume(vol, vol)
            }
        }
    }

    fun stop() {
        rampJob?.cancel()
        rampJob = null

        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping MediaPlayer", e)
            }
        }
        mediaPlayer = null
        abandonAlarmAudioFocus()
    }

    private fun requestAlarmAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setOnAudioFocusChangeListener { /* Keep playing */ }
                .build()
            focusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    private fun abandonAlarmAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun resolveSoundResource(toneName: String): Int {
        return when (toneName) {
            Constants.TONE_DIGITAL_URGENCY -> R.raw.digital_urgency
            Constants.TONE_EMERGENCY_BEACON -> R.raw.emergency_beacon
            else -> R.raw.rising_siren
        }
    }

    companion object {
        private const val TAG = "AlarmAudioPlayer"
    }
}
