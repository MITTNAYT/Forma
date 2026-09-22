package com.forma.app.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin

enum class AmbientSound(val displayName: String, val icon: String, val description: String) {
    OFF("Silent Flow", "volume_off", "Peaceful stillness"),
    RAIN("Rain on Tatami", "grain", "Soothing Japanese forest rainfall"),
    KYOTO_BELL("Kyoto Bell", "notifications_active", "528 Hz mindful temple chime"),
    ZEN_DRONE("Zen 432 Hz Drone", "spa", "Harmonic Theta wave resonance"),
    FOREST_STREAM("Forest Stream", "water", "Gentle bubbling mountain creek"),
    BROWN_NOISE("Brown Flow", "air", "Deep warm focus rumble"),
    WHITE_NOISE("White Air", "graphic_eq", "Crisp ambient masking")
}

@Singleton
class AmbientSoundManager @Inject constructor() {

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var currentSound: AmbientSound = AmbientSound.OFF

    fun play(sound: AmbientSound) {
        if (sound == currentSound && audioTrack != null) return
        stop()

        currentSound = sound
        if (sound == AmbientSound.OFF) return

        val sampleRate = 22050
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(sampleRate / 4)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.play()

        playbackJob = scope.launch {
            val buffer = ShortArray(bufferSize / 2)
            val random = Random()
            var b0 = 0.0
            var b1 = 0.0
            var b2 = 0.0
            var b3 = 0.0
            var phase1 = 0.0
            var phase2 = 0.0
            var phase3 = 0.0
            var bellEnvelope = 0.0
            var bellTimer = 0
            val twoPi = 2.0 * Math.PI

            while (isActive && audioTrack != null) {
                when (sound) {
                    AmbientSound.RAIN -> {
                        // Filtered pink noise with randomized droplet spikes
                        for (i in buffer.indices) {
                            val white = random.nextDouble() * 2.0 - 1.0
                            b0 = 0.99886 * b0 + white * 0.0555179
                            b1 = 0.99332 * b1 + white * 0.0750759
                            b2 = 0.96900 * b2 + white * 0.1538520
                            var pink = (b0 + b1 + b2 + white * 0.5362) * 0.075
                            // Occasional soft droplet click
                            if (random.nextInt(3500) == 0) {
                                pink += (random.nextDouble() * 0.4 - 0.2)
                            }
                            buffer[i] = (pink.coerceIn(-1.0, 1.0) * 8500.0).toInt().toShort()
                        }
                    }

                    AmbientSound.KYOTO_BELL -> {
                        // Periodic 528 Hz Solfeggio temple chime with long acoustic decay
                        val bellFreq1 = 528.0
                        val bellFreq2 = 1056.0
                        val bellFreq3 = 1584.0
                        val step1 = (twoPi * bellFreq1) / sampleRate
                        val step2 = (twoPi * bellFreq2) / sampleRate
                        val step3 = (twoPi * bellFreq3) / sampleRate

                        for (i in buffer.indices) {
                            if (bellTimer <= 0) {
                                bellEnvelope = 1.0
                                bellTimer = sampleRate * 7 // Ring every 7 seconds
                            }
                            bellTimer--
                            bellEnvelope *= 0.99994 // Smooth organic decay

                            val tone = (sin(phase1) * 0.6 + sin(phase2) * 0.25 + sin(phase3) * 0.15) * bellEnvelope * 0.35
                            buffer[i] = (tone.coerceIn(-1.0, 1.0) * 11000.0).toInt().toShort()

                            phase1 += step1
                            if (phase1 > twoPi) phase1 -= twoPi
                            phase2 += step2
                            if (phase2 > twoPi) phase2 -= twoPi
                            phase3 += step3
                            if (phase3 > twoPi) phase3 -= twoPi
                        }
                    }

                    AmbientSound.ZEN_DRONE -> {
                        // 432 Hz healing Om drone with 6 Hz Theta binaural pulsing
                        val baseFreq = 432.0
                        val beatFreq = 438.0
                        val subFreq = 216.0
                        val step1 = (twoPi * baseFreq) / sampleRate
                        val step2 = (twoPi * beatFreq) / sampleRate
                        val step3 = (twoPi * subFreq) / sampleRate

                        for (i in buffer.indices) {
                            val s1 = sin(phase1) * 0.4
                            val s2 = sin(phase2) * 0.35
                            val s3 = sin(phase3) * 0.25
                            val combined = (s1 + s2 + s3) * 0.28
                            buffer[i] = (combined.coerceIn(-1.0, 1.0) * 8500.0).toInt().toShort()

                            phase1 += step1
                            if (phase1 > twoPi) phase1 -= twoPi
                            phase2 += step2
                            if (phase2 > twoPi) phase2 -= twoPi
                            phase3 += step3
                            if (phase3 > twoPi) phase3 -= twoPi
                        }
                    }

                    AmbientSound.FOREST_STREAM -> {
                        // Modulated bubbling stream with low-pass gentle water flow
                        for (i in buffer.indices) {
                            val white = random.nextDouble() * 2.0 - 1.0
                            b0 = 0.98 * b0 + white * 0.02
                            b1 = 0.95 * b1 + white * 0.05
                            val modulation = (sin(phase1) + 1.0) * 0.5
                            val water = (b0 * 0.6 + b1 * 0.4) * (0.12 + 0.06 * modulation)
                            buffer[i] = (water.coerceIn(-1.0, 1.0) * 9000.0).toInt().toShort()

                            phase1 += (twoPi * 0.75) / sampleRate
                            if (phase1 > twoPi) phase1 -= twoPi
                        }
                    }

                    AmbientSound.BROWN_NOISE -> {
                        // True 6dB/octave brown noise for deep warm focus
                        for (i in buffer.indices) {
                            val white = random.nextDouble() * 2.0 - 1.0
                            b3 = (b3 + (0.02 * white)) / 1.02
                            val brown = b3 * 3.5 * 0.08
                            buffer[i] = (brown.coerceIn(-1.0, 1.0) * 8500.0).toInt().toShort()
                        }
                    }

                    AmbientSound.WHITE_NOISE -> {
                        for (i in buffer.indices) {
                            val white = (random.nextDouble() * 2.0 - 1.0) * 0.11
                            buffer[i] = (white.coerceIn(-1.0, 1.0) * 7500.0).toInt().toShort()
                        }
                    }

                    AmbientSound.OFF -> break
                }

                track.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        currentSound = AmbientSound.OFF
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}

