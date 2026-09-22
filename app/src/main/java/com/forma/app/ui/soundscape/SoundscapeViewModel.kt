package com.forma.app.ui.soundscape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.core.audio.AcousticSoundscapeEngine
import com.forma.app.core.audio.SoundscapeTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundscapeViewModel @Inject constructor(
    private val engine: AcousticSoundscapeEngine
) : ViewModel() {

    val currentTrack: StateFlow<SoundscapeTrack> = engine.currentTrack
    val isPlaying: StateFlow<Boolean> = engine.isPlaying
    val volume: StateFlow<Float> = engine.volume

    private val _timerRemainingSeconds = MutableStateFlow<Int?>(null)
    val timerRemainingSeconds: StateFlow<Int?> = _timerRemainingSeconds.asStateFlow()

    private var timerJob: Job? = null

    fun selectTrack(track: SoundscapeTrack) {
        if (track == currentTrack.value && isPlaying.value) {
            engine.stop()
            cancelTimer()
        } else {
            engine.play(track)
        }
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            engine.stop()
            cancelTimer()
        } else {
            val trackToPlay = if (currentTrack.value == SoundscapeTrack.SILENT) SoundscapeTrack.TEMPLE_RAIN else currentTrack.value
            engine.play(trackToPlay)
        }
    }

    fun setVolume(vol: Float) {
        engine.setVolume(vol)
    }

    fun setTimerMinutes(minutes: Int?) {
        cancelTimer()
        if (minutes == null || minutes <= 0) {
            _timerRemainingSeconds.value = null
            return
        }

        _timerRemainingSeconds.value = minutes * 60
        timerJob = viewModelScope.launch {
            while ((_timerRemainingSeconds.value ?: 0) > 0) {
                delay(1000)
                _timerRemainingSeconds.value = (_timerRemainingSeconds.value ?: 0) - 1
            }
            // Stop sound when timer ends
            engine.stop()
            _timerRemainingSeconds.value = null
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerRemainingSeconds.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Soundscape can keep playing in background or managed by foreground service if needed
    }
}
