package com.habitflow.app.core.audio

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

enum class AmbientSound(val displayName: String, val icon: String) {
    OFF("Silent", "volume_off"),
    RAIN("Forest Rain", "grain"),
    ZEN_WAVES("Zen Waves", "waves"),
    WHITE_NOISE("White Flow", "air")
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
            var phase1 = 0.0
            var phase2 = 0.0
            val twoPi = 2.0 * Math.PI

            while (isActive && audioTrack != null) {
                when (sound) {
                    AmbientSound.RAIN -> {
                        // Pink noise approximation for soothing rain
                        for (i in buffer.indices) {
                            val white = random.nextDouble() * 2.0 - 1.0
                            b0 = 0.99886 * b0 + white * 0.0555179
                            b1 = 0.99332 * b1 + white * 0.0750759
                            b2 = 0.96900 * b2 + white * 0.1538520
                            val pink = (b0 + b1 + b2 + white * 0.5362) * 0.08
                            buffer[i] = (pink.coerceIn(-1.0, 1.0) * 8000.0).toInt().toShort()
                        }
                    }
                    AmbientSound.ZEN_WAVES -> {
                        // Gentle ambient 136.1 Hz Om / Zen wave meditation harmonics
                        val freq1 = 136.1
                        val freq2 = 272.2
                        val step1 = (twoPi * freq1) / sampleRate
                        val step2 = (twoPi * freq2) / sampleRate

                        for (i in buffer.indices) {
                            val s1 = sin(phase1)
                            val s2 = sin(phase2) * 0.35
                            val combined = (s1 + s2) * 0.25
                            buffer[i] = (combined.coerceIn(-1.0, 1.0) * 9000.0).toInt().toShort()

                            phase1 += step1
                            if (phase1 > twoPi) phase1 -= twoPi
                            phase2 += step2
                            if (phase2 > twoPi) phase2 -= twoPi
                        }
                    }
                    AmbientSound.WHITE_NOISE -> {
                        for (i in buffer.indices) {
                            val white = (random.nextDouble() * 2.0 - 1.0) * 0.12
                            buffer[i] = (white.coerceIn(-1.0, 1.0) * 8000.0).toInt().toShort()
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
