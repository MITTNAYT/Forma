package com.forma.app.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    val DATE_FORMATTER_ISO: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val TIME_FORMATTER_SHORT: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val TIME_FORMATTER_24H: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val DAY_MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.getDefault())
    val MONTH_DAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())

    fun today(): LocalDate = LocalDate.now()

    fun formatDateIso(date: LocalDate): String = date.format(DATE_FORMATTER_ISO)

    fun parseDateIso(dateString: String): LocalDate = LocalDate.parse(dateString, DATE_FORMATTER_ISO)

    fun formatTime(time: LocalTime?): String {
        return time?.format(TIME_FORMATTER_SHORT) ?: ""
    }

    fun formatTimeRange(startTime: LocalTime?, endTime: LocalTime?): String {
        return when {
            startTime != null && endTime != null -> "${startTime.format(TIME_FORMATTER_SHORT)} – ${endTime.format(TIME_FORMATTER_SHORT)}"
            startTime != null -> startTime.format(TIME_FORMATTER_SHORT)
            else -> "Anytime"
        }
    }

    fun getDayOfWeekInt(date: LocalDate): Int {
        // Monday = 1, ..., Sunday = 7
        return date.dayOfWeek.value
    }

    fun getFiveWeekPastDates(endDate: LocalDate = today()): List<LocalDate> {
        // 5 weeks = 35 days ending at endDate
        val result = mutableListOf<LocalDate>()
        for (i in 34 downTo 0) {
            result.add(endDate.minusDays(i.toLong()))
        }
        return result
    }

    fun getWeekDates(anchorDate: LocalDate = today()): List<LocalDate> {
        val startOfWeek = anchorDate.minusDays(anchorDate.dayOfWeek.value.toLong() - 1)
        return (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }
}
