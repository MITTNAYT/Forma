package com.habitflow.app.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class BinauralMode(
    val title: String,
    val targetHz: Float,
    val carrierHz: Float,
    val description: String
) {
    ALPHA("Alpha Flow", 10.0f, 210.0f, "10 Hz — Deep learning, calm alertness, and effortless flow state"),
    THETA("Theta Meditation", 6.0f, 150.0f, "6 Hz — Deep inner peace, memory consolidation, and tranquility"),
    GAMMA("Gamma Insight", 40.0f, 250.0f, "40 Hz — High-level information processing and peak cognitive synthesis"),
    DELTA("Delta Renewal", 2.5f, 100.0f, "2.5 Hz — Restorative deep rest and tension release")
}

class BinauralBeatsEngine {

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var synthesisJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var isPlaying = false

    fun start(mode: BinauralMode, volume: Float = 0.5f) {
        stop()
        isPlaying = true

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBufferSize.coerceAtLeast(sampleRate / 4 * 4)

        audioTrack = AudioTrack.Builder()
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
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.setVolume(volume.coerceIn(0f, 1f))
        audioTrack?.play()

        val leftFreq = mode.carrierHz
        val rightFreq = mode.carrierHz + mode.targetHz

        synthesisJob = scope.launch {
            val numSamples = 2048
            val buffer = ShortArray(numSamples * 2) // Stereo interleaved [L, R, L, R...]
            var leftPhase = 0.0
            var rightPhase = 0.0
            val leftInc = 2.0 * PI * leftFreq / sampleRate
            val rightInc = 2.0 * PI * rightFreq / sampleRate

            while (isActive && isPlaying) {
                for (i in 0 until numSamples) {
                    val leftSample = (sin(leftPhase) * 16000).toInt().toShort()
                    val rightSample = (sin(rightPhase) * 16000).toInt().toShort()

                    buffer[i * 2] = leftSample
                    buffer[i * 2 + 1] = rightSample

                    leftPhase += leftInc
                    if (leftPhase >= 2.0 * PI) leftPhase -= 2.0 * PI

                    rightPhase += rightInc
                    if (rightPhase >= 2.0 * PI) rightPhase -= 2.0 * PI
                }

                audioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stop() {
        isPlaying = false
        synthesisJob?.cancel()
        synthesisJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
