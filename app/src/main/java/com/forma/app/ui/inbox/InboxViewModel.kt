package com.forma.app.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.TimelineItem
import com.forma.app.domain.repository.TimelineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val timelineRepository: TimelineRepository
) : ViewModel() {

    // Unscheduled backlog items (startTime == null or date == "INBOX")
    val inboxItems: StateFlow<List<TimelineItem>> = timelineRepository.getAllTimelineItems()
        .map { all ->
            all.filter { it.startTime == null || it.date == "INBOX" }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleComplete(item: TimelineItem) {
        viewModelScope.launch {
            timelineRepository.toggleTimelineItemCompletion(item.id, !item.completed)
        }
    }

    fun scheduleToToday(item: TimelineItem) {
        viewModelScope.launch {
            val todayStr = DateUtils.formatDateIso(DateUtils.today())
            timelineRepository.updateTimelineItem(
                item.copy(
                    date = todayStr,
                    startTime = "12:00",
                    endTime = "13:00"
                )
            )
        }
    }

    fun deleteItem(item: TimelineItem) {
        viewModelScope.launch {
            timelineRepository.deleteTimelineItem(item)
        }
    }
}
