package com.habitflow.app.core.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTaskResult(
    val cleanTitle: String,
    val date: LocalDate?,
    val startTime: String?, // HH:mm
    val endTime: String?,   // HH:mm
    val category: String?,
    val isEstimatedDurationMinutes: Int? = null
)

object SmartTaskParser {

    // Regex patterns for time expressions
    // 1. "8am to 9:30am" or "8:00 - 9:30" or "8am - 9am"
    private val TIME_RANGE_PATTERN = Pattern.compile(
        """\b(\d{1,2}(?::\d{2})?\s*(?:am|pm)?)\s*(?:to|-|until)\s*(\d{1,2}(?::\d{2})?\s*(?:am|pm)?)\b""",
        Pattern.CASE_INSENSITIVE
    )

    // 2. "at 4pm for 45m" or "at 14:00 for 1h"
    private val TIME_DURATION_PATTERN = Pattern.compile(
        """\bat\s+(\d{1,2}(?::\d{2})?\s*(?:am|pm)?)\s+(?:for\s+)?(\d+)\s*(m|min|mins|h|hr|hrs|hour|hours)\b""",
        Pattern.CASE_INSENSITIVE
    )

    // 3. Single time: "at 8:30am" or "at 14:00"
    private val SINGLE_TIME_PATTERN = Pattern.compile(
        """\bat\s+(\d{1,2}(?::\d{2})?\s*(?:am|pm)?)\b""",
        Pattern.CASE_INSENSITIVE
    )

    // Regex for hashtags e.g. #work, #focus, #health, #wellness, #learning
    private val HASHTAG_PATTERN = Pattern.compile("""#([a-zA-Z0-9_-]+)""")

    // Regex for relative days: "today", "tomorrow", "tonight"
    private val DAY_PATTERN = Pattern.compile("""\b(today|tomorrow|tonight)\b""", Pattern.CASE_INSENSITIVE)

    fun parse(input: String): ParsedTaskResult {
        var workingText = input.trim()
        var extractedDate: LocalDate? = null
        var extractedStartTime: String? = null
        var extractedEndTime: String? = null
        var extractedCategory: String? = null
        var extractedDuration: Int? = null

        // 1. Match Hashtag Category
        val hashtagMatcher = HASHTAG_PATTERN.matcher(workingText)
        if (hashtagMatcher.find()) {
            val tag = hashtagMatcher.group(1)?.lowercase(Locale.ROOT)
            extractedCategory = tag
            val matchedFull = hashtagMatcher.group(0)
            if (matchedFull != null) {
                workingText = workingText.replace(matchedFull, "").trim()
            }
        }

        // 2. Match Date
        val dayMatcher = DAY_PATTERN.matcher(workingText)
        if (dayMatcher.find()) {
            val matchedDay = dayMatcher.group(1)?.lowercase(Locale.ROOT)
            extractedDate = when (matchedDay) {
                "tomorrow" -> LocalDate.now().plusDays(1)
                else -> LocalDate.now()
            }
            val matchedFull = dayMatcher.group(0)
            if (matchedFull != null) {
                workingText = workingText.replace(matchedFull, "").trim()
            }
        }

        // 3. Match Time Range (e.g. "8am to 9:30am")
        val rangeMatcher = TIME_RANGE_PATTERN.matcher(workingText)
        if (rangeMatcher.find()) {
            val rawStart = rangeMatcher.group(1) ?: ""
            val rawEnd = rangeMatcher.group(2) ?: ""
            val parsedStart = parseTimeString(rawStart)
            val parsedEnd = parseTimeString(rawEnd)

            if (parsedStart != null) {
                extractedStartTime = parsedStart.format(DateTimeFormatter.ofPattern("HH:mm"))
                if (parsedEnd != null) {
                    extractedEndTime = parsedEnd.format(DateTimeFormatter.ofPattern("HH:mm"))
                } else {
                    extractedEndTime = parsedStart.plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm"))
                }
                val matchedFull = rangeMatcher.group(0)
                if (matchedFull != null) {
                    workingText = workingText.replace(matchedFull, "").trim()
                }
            }
        }

        // 4. Match Time + Duration (e.g. "at 4pm for 45m")
        if (extractedStartTime == null) {
            val durationMatcher = TIME_DURATION_PATTERN.matcher(workingText)
            if (durationMatcher.find()) {
                val rawTime = durationMatcher.group(1) ?: ""
                val durationVal = durationMatcher.group(2)?.toIntOrNull() ?: 30
                val durationUnit = durationMatcher.group(3)?.lowercase(Locale.ROOT) ?: "m"

                val minutes = if (durationUnit.startsWith("h")) durationVal * 60 else durationVal
                extractedDuration = minutes
                val parsedStart = parseTimeString(rawTime)
                if (parsedStart != null) {
                    extractedStartTime = parsedStart.format(DateTimeFormatter.ofPattern("HH:mm"))
                    extractedEndTime = parsedStart.plusMinutes(minutes.toLong()).format(DateTimeFormatter.ofPattern("HH:mm"))
                    val matchedFull = durationMatcher.group(0)
                    if (matchedFull != null) {
                        workingText = workingText.replace(matchedFull, "").trim()
                    }
                }
            }
        }

        // 5. Match Single Time (e.g. "at 3:30pm")
        if (extractedStartTime == null) {
            val singleTimeMatcher = SINGLE_TIME_PATTERN.matcher(workingText)
            if (singleTimeMatcher.find()) {
                val rawTime = singleTimeMatcher.group(1) ?: ""
                val parsedStart = parseTimeString(rawTime)
                if (parsedStart != null) {
                    extractedStartTime = parsedStart.format(DateTimeFormatter.ofPattern("HH:mm"))
                    extractedEndTime = parsedStart.plusMinutes(30).format(DateTimeFormatter.ofPattern("HH:mm"))
                    val matchedFull = singleTimeMatcher.group(0)
                    if (matchedFull != null) {
                        workingText = workingText.replace(matchedFull, "").trim()
                    }
                }
            }
        }

        // Clean extra punctuation or trailing prepositions
        var cleanTitle = workingText
            .replace(Regex("""\s+at\s*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+for\s*$""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s{2,}"""), " ")
            .trim()

        if (cleanTitle.isBlank()) {
            cleanTitle = input.trim()
        }

        return ParsedTaskResult(
            cleanTitle = cleanTitle,
            date = extractedDate,
            startTime = extractedStartTime,
            endTime = extractedEndTime,
            category = extractedCategory,
            isEstimatedDurationMinutes = extractedDuration
        )
    }

    private fun parseTimeString(raw: String): LocalTime? {
        val str = raw.trim().lowercase(Locale.ROOT)
        val isPm = str.endsWith("pm")
        val isAm = str.endsWith("am")
        val digitsPart = str.replace("am", "").replace("pm", "").trim()

        return try {
            if (digitsPart.contains(":")) {
                val parts = digitsPart.split(":")
                var hour = parts[0].toInt()
                val minute = parts[1].toInt()
                if (isPm && hour < 12) hour += 12
                if (isAm && hour == 12) hour = 0
                LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            } else {
                var hour = digitsPart.toInt()
                if (isPm && hour < 12) hour += 12
                if (isAm && hour == 12) hour = 0
                LocalTime.of(hour.coerceIn(0, 23), 0)
            }
        } catch (_: Exception) {
            null
        }
    }
}
