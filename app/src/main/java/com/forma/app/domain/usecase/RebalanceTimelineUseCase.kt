package com.forma.app.domain.usecase

import com.forma.app.core.util.DateUtils
import com.forma.app.domain.model.TimelineItem
import com.forma.app.domain.repository.TimelineRepository
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RebalanceTimelineUseCase @Inject constructor(
    private val timelineRepository: TimelineRepository
) {
    /**
     * Rebalances incomplete timeline items for the specified date starting from [fromTime]
     * (or the current time). Inserts a serene 5-minute breathing gap between consecutive blocks.
     */
    suspend operator fun invoke(
        date: String = DateUtils.formatDateIso(DateUtils.today()),
        fromTime: LocalTime = LocalTime.now()
    ): Int {
        val items = timelineRepository.getTimelineItemsForDate(date).first()
        val incompleteItems = items.filter { !it.completed }
            .sortedBy { it.startTime }

        if (incompleteItems.isEmpty()) return 0

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        var currentCursor = fromTime
        var rebalancedCount = 0

        for (item in incompleteItems) {
            val itemStart = try {
                LocalTime.parse(item.startTime, timeFormatter)
            } catch (_: Exception) {
                currentCursor
            }

            val itemEnd = try {
                LocalTime.parse(item.endTime, timeFormatter)
            } catch (_: Exception) {
                itemStart.plusMinutes(30)
            }

            // Calculate original item duration in minutes
            val durationMinutes = java.time.Duration.between(itemStart, itemEnd).toMinutes()
                .coerceAtLeast(15)

            // If the item starts in the past or conflicts with current cursor, shift it forward
            val newStart = if (itemStart.isBefore(currentCursor)) {
                currentCursor
            } else {
                itemStart
            }

            val newEnd = newStart.plusMinutes(durationMinutes)

            if (newStart != itemStart || newEnd != itemEnd) {
                val updatedItem = item.copy(
                    startTime = newStart.format(timeFormatter),
                    endTime = newEnd.format(timeFormatter),
                    updatedAt = System.currentTimeMillis()
                )
                timelineRepository.updateTimelineItem(updatedItem)
                rebalancedCount++
            }

            // Advance cursor by item duration + 5 minute mindful buffer
            currentCursor = newEnd.plusMinutes(5)
        }

        return rebalancedCount
    }
}
