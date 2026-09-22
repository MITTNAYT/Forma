package com.habitflow.app.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin

@Singleton
class ZenFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Triggers a gentle mindful completion effect:
     * 1. Resonant Tibetan singing bowl acoustic chime tone (7-harmonic series).
     * 2. Crisp tactile haptic pulse.
     */
    fun onHabitCompleted(playChime: Boolean = true) {
        performHaptic(HapticType.SUCCESS_CLICK)
        if (playChime) {
            playTibetanBowlChime()
        }
    }

    fun onTaskToggled() {
        performHaptic(HapticType.LIGHT_TICK)
    }

    fun onMilestoneReached() {
        performHaptic(HapticType.DOUBLE_PULSE)
        playTibetanBowlChime(pitchMultiplier = 1.2)
    }

    private fun performHaptic(type: HapticType) {
        try {
            vibrator?.let { v ->
                if (!v.hasVibrator()) return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effect = when (type) {
                        HapticType.LIGHT_TICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        HapticType.SUCCESS_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.DOUBLE_PULSE -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                    }
                    v.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(25)
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Synthesizes a high-definition Tibetan Singing Bowl 7-harmonic acoustic chime.
     */
    fun playTibetanBowlChime(pitchMultiplier: Double = 1.0) {
        scope.launch {
            try {
                val sampleRate = 44100
                val durationSeconds = 1.6
                val numSamples = (sampleRate * durationSeconds).toInt()
                val buffer = ShortArray(numSamples)

                val partials = doubleArrayOf(144.0, 288.0, 432.0, 528.0, 720.0, 864.0, 1296.0).map { it * pitchMultiplier }
                val weights = doubleArrayOf(0.35, 0.25, 0.20, 0.35, 0.15, 0.10, 0.05)
                val phases = DoubleArray(partials.size)
                val twoPi = 2.0 * PI

                var envelope = 1.0
                val decayRate = Math.pow(0.0005, 1.0 / numSamples)

                for (i in 0 until numSamples) {
                    var sampleVal = 0.0
                    for (p in partials.indices) {
                        val step = (twoPi * partials[p]) / sampleRate
                        phases[p] += step
                        if (phases[p] > twoPi) phases[p] -= twoPi
                        sampleVal += sin(phases[p]) * weights[p]
                    }

                    val finalSample = (sampleVal * envelope * 14000.0).toInt().coerceIn(-32768, 32767).toShort()
                    buffer[i] = finalSample
                    envelope *= decayRate
                }

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(buffer, 0, buffer.size)
                track.play()

                kotlinx.coroutines.delay((durationSeconds * 1000).toLong() + 200)
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }

    companion object {
        fun triggerTactileTick(context: Context) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(20)
                }
            } catch (_: Exception) {}
        }

        fun triggerGentleHaptic(context: Context) {
            try {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            } catch (_: Exception) {}
        }

        fun playTibetanBowl(context: Context, pitchMultiplier: Double = 1.0) {
            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val sampleRate = 44100
                    val durationSeconds = 1.6
                    val numSamples = (sampleRate * durationSeconds).toInt()
                    val buffer = ShortArray(numSamples)

                    val partials = doubleArrayOf(144.0, 288.0, 432.0, 528.0, 720.0, 864.0, 1296.0).map { it * pitchMultiplier }
                    val weights = doubleArrayOf(0.35, 0.25, 0.20, 0.35, 0.15, 0.10, 0.05)
                    val phases = DoubleArray(partials.size)
                    val twoPi = 2.0 * PI

                    var envelope = 1.0
                    val decayRate = Math.pow(0.0005, 1.0 / numSamples)

                    for (i in 0 until numSamples) {
                        var sampleVal = 0.0
                        for (p in partials.indices) {
                            val step = (twoPi * partials[p]) / sampleRate
                            phases[p] += step
                            if (phases[p] > twoPi) phases[p] -= twoPi
                            sampleVal += sin(phases[p]) * weights[p]
                        }

                        val finalSample = (sampleVal * envelope * 14000.0).toInt().coerceIn(-32768, 32767).toShort()
                        buffer[i] = finalSample
                        envelope *= decayRate
                    }

                    val track = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(buffer.size * 2)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()

                    track.write(buffer, 0, buffer.size)
                    track.play()

                    kotlinx.coroutines.delay((durationSeconds * 1000).toLong() + 200)
                    track.stop()
                    track.release()
                } catch (_: Exception) {}
            }
        }
    }

    enum class HapticType {
        LIGHT_TICK,
        SUCCESS_CLICK,
        DOUBLE_PULSE
    }
}
