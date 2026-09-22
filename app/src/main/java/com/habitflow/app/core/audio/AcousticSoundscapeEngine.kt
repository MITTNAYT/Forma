package com.habitflow.app.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

enum class SoundscapeTrack(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val colorTag: String
) {
    SILENT("silent", "Silent Stillness", "Quiet mindful focus", "volume_off", "#4A5568"),
    TEMPLE_RAIN("temple_rain", "Rain on Cedar", "Acoustic raindrops on timber deck", "grain", "#3B82F6"),
    SINGING_BOWL("singing_bowl", "Tibetan Singing Bowl", "528 Hz Solfeggio & 7-harmonic resonance", "notifications_active", "#D97706"),
    ALPINE_STREAM("alpine_stream", "Alpine Forest Stream", "Bubbling hydrodynamic mountain water", "water", "#059669"),
    ZEN_CAMPFIRE("zen_campfire", "Zen Campfire Embers", "Warm timber crackle & gentle embers", "local_fire_department", "#DC2626"),
    BINAURAL_THETA("binaural_theta", "432 Hz Binaural Rest", "Theta 6 Hz harmonic brainwave balance", "spa", "#7C3AED")
}

@Singleton
class AcousticSoundscapeEngine @Inject constructor() {

    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _currentTrack = MutableStateFlow(SoundscapeTrack.SILENT)
    val currentTrack: StateFlow<SoundscapeTrack> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.75f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            audioTrack?.setVolume(clamped)
        } catch (_: Exception) {}
    }

    fun play(track: SoundscapeTrack) {
        if (track == _currentTrack.value && _isPlaying.value) return
        stop()

        _currentTrack.value = track
        if (track == SoundscapeTrack.SILENT) {
            _isPlaying.value = false
            return
        }

        val sampleRate = 44100
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(sampleRate)

        val trackObj = AudioTrack.Builder()
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

        audioTrack = trackObj
        trackObj.setVolume(_volume.value)
        trackObj.play()
        _isPlaying.value = true

        playbackJob = scope.launch {
            val numFrames = bufferSize / 4 // 2 channels, 2 bytes per sample
            val buffer = ShortArray(numFrames * 2)
            val random = Random()

            // State variables for physical acoustic models
            val twoPi = 2.0 * PI
            var phaseL = 0.0
            var phaseR = 0.0
            var bowlStrikeTimer = 0
            var bowlIntensity = 0.0

            // Multi-harmonic phase accumulators for Tibetan bowl (7 partials)
            val bowlPartials = doubleArrayOf(144.0, 288.0, 432.0, 528.0, 720.0, 864.0, 1296.0)
            val bowlWeights = doubleArrayOf(0.40, 0.25, 0.20, 0.35, 0.12, 0.08, 0.05)
            val bowlPhases = DoubleArray(bowlPartials.size)

            // Rain model acoustic states
            var rainL1 = 0.0
            var rainR1 = 0.0
            var rainL2 = 0.0
            var rainR2 = 0.0

            // Stream model states
            var streamL = 0.0
            var streamR = 0.0
            var bubbleTimer = 0
            var bubbleFreq = 400.0
            var bubblePhase = 0.0
            var bubbleAmp = 0.0

            // Campfire crackle states
            var fireL = 0.0
            var fireR = 0.0

            while (isActive && audioTrack != null) {
                val currentVol = _volume.value

                when (track) {
                    SoundscapeTrack.TEMPLE_RAIN -> {
                        // High-fidelity binaural rain on cedar timber: Low rumble of distant atmosphere + micro-impacts
                        for (i in 0 until numFrames) {
                            val wL = random.nextDouble() * 2.0 - 1.0
                            val wR = random.nextDouble() * 2.0 - 1.0

                            // 2-pole resonant low-pass filter (cedar wood body)
                            rainL1 = 0.985 * rainL1 + wL * 0.015
                            rainR1 = 0.985 * rainR1 + wR * 0.015

                            // Gentle raindrop impacts
                            var impactL = 0.0
                            var impactR = 0.0
                            if (random.nextInt(1200) == 0) {
                                impactL = (random.nextDouble() * 0.5 - 0.25)
                            }
                            if (random.nextInt(1200) == 0) {
                                impactR = (random.nextDouble() * 0.5 - 0.25)
                            }

                            val outL = ((rainL1 * 0.7 + impactL) * 14000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()
                            val outR = ((rainR1 * 0.7 + impactR) * 14000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()

                            buffer[i * 2] = outL
                            buffer[i * 2 + 1] = outR
                        }
                    }

                    SoundscapeTrack.SINGING_BOWL -> {
                        // Authentic 7-Harmonic series Tibetan bowl with continuous warm rim hum and periodic mindful strikes
                        for (i in 0 until numFrames) {
                            if (bowlStrikeTimer <= 0) {
                                bowlIntensity = 1.0
                                bowlStrikeTimer = sampleRate * 9 // Strike chime every 9 seconds
                            }
                            bowlStrikeTimer--
                            bowlIntensity *= 0.999975 // Slow organic acoustic ring decay

                            var leftTone = 0.0
                            var rightTone = 0.0

                            for (p in bowlPartials.indices) {
                                val freq = bowlPartials[p]
                                val step = (twoPi * freq) / sampleRate
                                bowlPhases[p] += step
                                if (bowlPhases[p] > twoPi) bowlPhases[p] -= twoPi

                                val partialSine = sin(bowlPhases[p]) * bowlWeights[p]
                                // Spatial panning across partials for deep wide acoustic staging
                                val pan = 0.5 + 0.3 * sin(p.toDouble() * 1.2)
                                leftTone += partialSine * pan
                                rightTone += partialSine * (1.0 - pan)
                            }

                            // Warm continuous undertone + resonant strike
                            val outSampleL = ((leftTone * (0.25 + 0.75 * bowlIntensity)) * 16000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()
                            val outSampleR = ((rightTone * (0.25 + 0.75 * bowlIntensity)) * 16000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()

                            buffer[i * 2] = outSampleL
                            buffer[i * 2 + 1] = outSampleR
                        }
                    }

                    SoundscapeTrack.ALPINE_STREAM -> {
                        // Hydrodynamic water bubbling physics with stochastic bubble cavitation
                        for (i in 0 until numFrames) {
                            val wL = random.nextDouble() * 2.0 - 1.0
                            val wR = random.nextDouble() * 2.0 - 1.0

                            streamL = 0.975 * streamL + wL * 0.025
                            streamR = 0.975 * streamR + wR * 0.025

                            if (bubbleTimer <= 0) {
                                bubbleTimer = random.nextInt(sampleRate / 4) + (sampleRate / 10)
                                bubbleFreq = 300.0 + random.nextDouble() * 500.0
                                bubbleAmp = 0.4
                                bubblePhase = 0.0
                            }
                            bubbleTimer--

                            var bubbleSound = 0.0
                            if (bubbleAmp > 0.001) {
                                bubblePhase += (twoPi * bubbleFreq) / sampleRate
                                bubbleSound = sin(bubblePhase) * bubbleAmp
                                bubbleAmp *= 0.9985
                            }

                            val outL = ((streamL * 0.8 + bubbleSound * 0.3) * 15000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()
                            val outR = ((streamR * 0.8 + bubbleSound * 0.2) * 15000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()

                            buffer[i * 2] = outL
                            buffer[i * 2 + 1] = outR
                        }
                    }

                    SoundscapeTrack.ZEN_CAMPFIRE -> {
                        // Warm wood crackles and low ember warmth
                        for (i in 0 until numFrames) {
                            val wL = random.nextDouble() * 2.0 - 1.0
                            val wR = random.nextDouble() * 2.0 - 1.0

                            fireL = 0.992 * fireL + wL * 0.008
                            fireR = 0.992 * fireR + wR * 0.008

                            var crackleL = 0.0
                            var crackleR = 0.0
                            if (random.nextInt(2800) == 0) {
                                crackleL = (random.nextDouble() * 0.8 - 0.4)
                            }
                            if (random.nextInt(3200) == 0) {
                                crackleR = (random.nextDouble() * 0.8 - 0.4)
                            }

                            val outL = ((fireL * 0.6 + crackleL) * 14000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()
                            val outR = ((fireR * 0.6 + crackleR) * 14000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()

                            buffer[i * 2] = outL
                            buffer[i * 2 + 1] = outR
                        }
                    }

                    SoundscapeTrack.BINAURAL_THETA -> {
                        // 432 Hz Left / 438 Hz Right (6 Hz Theta brainwave entrainment) + 216 Hz sub-bass
                        val stepL = (twoPi * 432.0) / sampleRate
                        val stepR = (twoPi * 438.0) / sampleRate
                        val stepSub = (twoPi * 216.0) / sampleRate

                        for (i in 0 until numFrames) {
                            phaseL += stepL
                            if (phaseL > twoPi) phaseL -= twoPi

                            phaseR += stepR
                            if (phaseR > twoPi) phaseR -= twoPi

                            val sub = sin(phaseL * 0.5) * 0.35
                            val sL = sin(phaseL) * 0.65 + sub
                            val sR = sin(phaseR) * 0.65 + sub

                            val outL = (sL * 13000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()
                            val outR = (sR * 13000.0 * currentVol).toInt().coerceIn(-32768, 32767).toShort()

                            buffer[i * 2] = outL
                            buffer[i * 2 + 1] = outR
                        }
                    }

                    SoundscapeTrack.SILENT -> break
                }

                trackObj.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        _isPlaying.value = false
        _currentTrack.value = SoundscapeTrack.SILENT
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
