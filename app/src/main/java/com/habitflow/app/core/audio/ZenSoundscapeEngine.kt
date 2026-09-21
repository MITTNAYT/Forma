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
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class SoundscapeType(val displayName: String, val description: String) {
    NONE("Silence", "Silent mindful awareness"),
    SINGING_BOWL("Tibetan Bowl", "432Hz harmonic singing bowl resonance"),
    RAIN_BROWN_NOISE("Soft Rain", "Low-pass filtered soothing rain & brown noise"),
    BINAURAL_ALPHA("Alpha Waves", "10Hz binaural flow state (432Hz - 442Hz)"),
    BAMBOO_WATER("Shishi-Odoshi", "Rhythmic bamboo fountain and gentle water stream")
}

@Singleton
class ZenSoundscapeEngine @Inject constructor() {

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var currentVolume = 0.65f

    @Volatile
    var activeSoundscape: SoundscapeType = SoundscapeType.NONE
        private set

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        try {
            audioTrack?.setVolume(currentVolume)
        } catch (_: Exception) {}
    }

    fun startSoundscape(type: SoundscapeType) {
        if (type == activeSoundscape && playbackJob?.isActive == true) return
        stopSoundscape()
        if (type == SoundscapeType.NONE) return

        activeSoundscape = type
        playbackJob = scope.launch {
            runSoundscapeLoop(type)
        }
    }

    fun stopSoundscape() {
        activeSoundscape = SoundscapeType.NONE
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    fun playSingingBowlChime(durationSeconds: Float = 3.5f) {
        scope.launch {
            playOneShotChime(durationSeconds)
        }
    }

    private fun runSoundscapeLoop(type: SoundscapeType) {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(sampleRate / 4)

        val track = try {
            AudioTrack.Builder()
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
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            return
        }

        audioTrack = track
        track.setVolume(currentVolume)
        track.play()

        val pcmBuffer = ShortArray(bufferSize)
        var phaseL = 0.0
        var phaseR = 0.0
        val random = Random()
        var brownL = 0.0
        var brownR = 0.0
        var tickCounter = 0L

        while (playbackJob?.isActive == true) {
            val numFrames = pcmBuffer.size / 2
            for (i in 0 until numFrames) {
                var sampleLeft = 0.0
                var sampleRight = 0.0

                when (type) {
                    SoundscapeType.SINGING_BOWL -> {
                        // Continuous resonant bowl drone at 432Hz fundamental + 864Hz harmonic + slow LFO tremolo
                        val lfo = 0.85 + 0.15 * sin(2.0 * PI * 0.2 * (tickCounter.toDouble() / sampleRate))
                        val s1 = sin(phaseL) * 0.5
                        val s2 = sin(phaseL * 2.0) * 0.25
                        val s3 = sin(phaseL * 3.0) * 0.12
                        sampleLeft = (s1 + s2 + s3) * lfo * 0.4
                        sampleRight = sampleLeft

                        phaseL += 2.0 * PI * 432.0 / sampleRate
                        if (phaseL > 2.0 * PI * 1000) phaseL -= 2.0 * PI * 1000
                    }

                    SoundscapeType.RAIN_BROWN_NOISE -> {
                        // Low-pass filtered brown noise (integrated white noise)
                        val whiteL = (random.nextDouble() * 2.0 - 1.0)
                        val whiteR = (random.nextDouble() * 2.0 - 1.0)
                        brownL = (brownL + (0.02 * whiteL)) / 1.02
                        brownR = (brownR + (0.02 * whiteR)) / 1.02
                        sampleLeft = brownL * 1.5
                        sampleRight = brownR * 1.5
                    }

                    SoundscapeType.BINAURAL_ALPHA -> {
                        // 432 Hz Left Ear, 442 Hz Right Ear = 10Hz Alpha differential wave
                        sampleLeft = sin(phaseL) * 0.35
                        sampleRight = sin(phaseR) * 0.35

                        phaseL += 2.0 * PI * 432.0 / sampleRate
                        phaseR += 2.0 * PI * 442.0 / sampleRate
                        if (phaseL > 2.0 * PI * 1000) phaseL -= 2.0 * PI * 1000
                        if (phaseR > 2.0 * PI * 1000) phaseR -= 2.0 * PI * 1000
                    }

                    SoundscapeType.BAMBOO_WATER -> {
                        // Gentle stream noise + periodic resonant bamboo strike
                        val whiteL = (random.nextDouble() * 2.0 - 1.0)
                        val whiteR = (random.nextDouble() * 2.0 - 1.0)
                        brownL = (brownL + (0.03 * whiteL)) / 1.03
                        brownR = (brownR + (0.03 * whiteR)) / 1.03

                        // Stream baseline
                        var streamL = brownL * 0.6
                        var streamR = brownR * 0.6

                        // Bamboo clack every 4 seconds (4 * sampleRate ticks)
                        val period = sampleRate * 4
                        val posInPeriod = (tickCounter % period).toInt()
                        if (posInPeriod < sampleRate * 0.4) {
                            val decay = exp(-posInPeriod.toDouble() / (sampleRate * 0.06))
                            val strike = sin(2.0 * PI * 580.0 * posInPeriod / sampleRate) * decay * 0.7
                            streamL += strike
                            streamR += strike
                        }

                        sampleLeft = streamL
                        sampleRight = streamR
                    }

                    SoundscapeType.NONE -> {
                        sampleLeft = 0.0
                        sampleRight = 0.0
                    }
                }

                tickCounter++

                val intLeft = (sampleLeft.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                val intRight = (sampleRight.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()

                pcmBuffer[i * 2] = intLeft
                pcmBuffer[i * 2 + 1] = intRight
            }

            try {
                track.write(pcmBuffer, 0, pcmBuffer.size)
            } catch (e: Exception) {
                break
            }
        }
    }

    private fun playOneShotChime(durationSeconds: Float) {
        val totalSamples = (sampleRate * durationSeconds).toInt()
        val pcmBuffer = ShortArray(totalSamples * 2)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 1.8) // Smooth natural exponential decay
            val f0 = 432.0
            val s1 = sin(2.0 * PI * f0 * t) * 0.6
            val s2 = sin(2.0 * PI * (f0 * 2.008) * t) * 0.3 // Harmonic dissonance shimmer
            val s3 = sin(2.0 * PI * (f0 * 3.012) * t) * 0.15

            val sample = (s1 + s2 + s3) * decay * currentVolume
            val intSample = (sample.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()

            pcmBuffer[i * 2] = intSample
            pcmBuffer[i * 2 + 1] = intSample
        }

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(pcmBuffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmBuffer, 0, pcmBuffer.size)
            track.play()
            // Track static mode releases automatically or when done
        } catch (_: Exception) {}
    }
}
