package com.example.parser

import com.example.data.model.ScheduleCategory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ParsedScheduleResult(
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val category: ScheduleCategory,
    val isReminderEnabled: Boolean,
    val reminderMinutesBefore: Int,
    val detectedDateLabel: String,
    val detectedTimeLabel: String
)

object NaturalLanguageParser {

    fun parse(input: String, baseDate: LocalDate = LocalDate.now()): ParsedScheduleResult {
        val cleanInput = input.trim()
        val lower = cleanInput.lowercase(Locale.ROOT)

        // 1. Detect Reminder
        var isReminderEnabled = false
        var reminderMinutes = 15

        val reminderPatterns = listOf(
            Regex("(?:ingatkan|remind|ingat)\\s+(\\d+)\\s*(?:menit|min|m)(?:\\s+sebelumnya|\\s+sebelum|\\s+before)?"),
            Regex("(?:ingatkan|remind|ingat)\\s+(\\d+)\\s*(?:jam|hour|h)(?:\\s+sebelumnya|\\s+sebelum|\\s+before)?"),
            Regex("(?:ingatkan|ingat|notifikasi|remind)(?:\\s+sebelumnya|\\s+sebelum|\\s+before)?")
        )

        val remMatchMin = reminderPatterns[0].find(lower)
        val remMatchHour = reminderPatterns[1].find(lower)
        val remMatchGeneral = reminderPatterns[2].find(lower)

        var textWithoutReminder = cleanInput
        if (remMatchMin != null) {
            isReminderEnabled = true
            reminderMinutes = remMatchMin.groupValues[1].toIntOrNull() ?: 15
            textWithoutReminder = textWithoutReminder.removeRange(remMatchMin.range).trim()
        } else if (remMatchHour != null) {
            isReminderEnabled = true
            val hours = remMatchHour.groupValues[1].toIntOrNull() ?: 1
            reminderMinutes = hours * 60
            textWithoutReminder = textWithoutReminder.removeRange(remMatchHour.range).trim()
        } else if (remMatchGeneral != null) {
            isReminderEnabled = true
            reminderMinutes = 15
            textWithoutReminder = textWithoutReminder.removeRange(remMatchGeneral.range).trim()
        }

        // 2. Detect Date
        var targetDate = baseDate
        var detectedDateLabel = "Hari Ini"
        var textWithoutDate = textWithoutReminder

        // Check relative words
        val relativeDateRegex = Regex("\\b(hari ini|besok lusa|lusa|besok|kemarin|today|tomorrow)\\b", RegexOption.IGNORE_CASE)
        val relMatch = relativeDateRegex.find(textWithoutDate)

        if (relMatch != null) {
            val matchedWord = relMatch.value.lowercase()
            when {
                matchedWord.contains("besok lusa") || matchedWord == "lusa" -> {
                    targetDate = baseDate.plusDays(2)
                    detectedDateLabel = "Lusa (${formatShortDate(targetDate)})"
                }
                matchedWord == "besok" || matchedWord == "tomorrow" -> {
                    targetDate = baseDate.plusDays(1)
                    detectedDateLabel = "Besok (${formatShortDate(targetDate)})"
                }
                matchedWord == "kemarin" || matchedWord == "yesterday" -> {
                    targetDate = baseDate.minusDays(1)
                    detectedDateLabel = "Kemarin (${formatShortDate(targetDate)})"
                }
                else -> {
                    targetDate = baseDate
                    detectedDateLabel = "Hari Ini (${formatShortDate(targetDate)})"
                }
            }
            textWithoutDate = textWithoutDate.removeRange(relMatch.range).trim()
        } else {
            // Check day of week
            val daysMap = mapOf(
                "senin" to DayOfWeek.MONDAY,
                "selasa" to DayOfWeek.TUESDAY,
                "rabu" to DayOfWeek.WEDNESDAY,
                "kamis" to DayOfWeek.THURSDAY,
                "jumat" to DayOfWeek.FRIDAY,
                "jum'at" to DayOfWeek.FRIDAY,
                "sabtu" to DayOfWeek.SATURDAY,
                "minggu" to DayOfWeek.SUNDAY,
                "monday" to DayOfWeek.MONDAY,
                "tuesday" to DayOfWeek.TUESDAY,
                "wednesday" to DayOfWeek.WEDNESDAY,
                "thursday" to DayOfWeek.THURSDAY,
                "friday" to DayOfWeek.FRIDAY,
                "saturday" to DayOfWeek.SATURDAY,
                "sunday" to DayOfWeek.SUNDAY
            )

            val dayRegex = Regex("\\b(?:hari\\s+)?(senin|selasa|rabu|kamis|jumat|jum'at|sabtu|minggu|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b", RegexOption.IGNORE_CASE)
            val dayMatch = dayRegex.find(textWithoutDate)
            if (dayMatch != null) {
                val matchedDay = dayMatch.groupValues[1].lowercase()
                val targetDayOfWeek = daysMap[matchedDay]
                if (targetDayOfWeek != null) {
                    var candidate = baseDate
                    var daysAdded = 0
                    while (candidate.dayOfWeek != targetDayOfWeek || daysAdded == 0) {
                        candidate = candidate.plusDays(1)
                        daysAdded++
                        if (daysAdded > 7) break
                    }
                    targetDate = candidate
                    val dayNameIndo = matchedDay.replaceFirstChar { it.uppercase() }
                    detectedDateLabel = "$dayNameIndo (${formatShortDate(targetDate)})"
                    textWithoutDate = textWithoutDate.removeRange(dayMatch.range).trim()
                }
            }
        }

        // 3. Detect Time
        var startTime = LocalTime.of(9, 0)
        var durationMinutes = 60
        var detectedTimeLabel = "09:00 - 10:00"
        var textWithoutTime = textWithoutDate

        // Time patterns:
        // "jam 19.00", "jam 19:30", "jam 7 malam", "jam 9 pagi", "pukul 14.00", "19:00", "08.30"
        val timeRegex = Regex("\\b(?:jam|pukul|at)?\\s*(\\d{1,2})[:.](\\d{2})\\s*(pagi|siang|sore|malam|am|pm)?\\b", RegexOption.IGNORE_CASE)
        val hourOnlyRegex = Regex("\\b(?:jam|pukul|at)\\s*(\\d{1,2})\\s*(pagi|siang|sore|malam|am|pm)?\\b", RegexOption.IGNORE_CASE)

        val timeMatch = timeRegex.find(textWithoutTime)
        val hourMatch = if (timeMatch == null) hourOnlyRegex.find(textWithoutTime) else null

        if (timeMatch != null) {
            var hour = timeMatch.groupValues[1].toIntOrNull() ?: 9
            val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
            val modifier = timeMatch.groupValues[3].lowercase()

            hour = adjustHourWithModifier(hour, modifier)
            hour = hour.coerceIn(0, 23)
            val validMinute = minute.coerceIn(0, 59)

            startTime = LocalTime.of(hour, validMinute)
            val endTime = startTime.plusMinutes(durationMinutes.toLong())
            detectedTimeLabel = String.format("%02d:%02d - %02d:%02d", startTime.hour, startTime.minute, endTime.hour, endTime.minute)
            textWithoutTime = textWithoutTime.removeRange(timeMatch.range).trim()
        } else if (hourMatch != null) {
            var hour = hourMatch.groupValues[1].toIntOrNull() ?: 9
            val modifier = hourMatch.groupValues[2].lowercase()

            hour = adjustHourWithModifier(hour, modifier)
            hour = hour.coerceIn(0, 23)

            startTime = LocalTime.of(hour, 0)
            val endTime = startTime.plusMinutes(durationMinutes.toLong())
            detectedTimeLabel = String.format("%02d:%02d - %02d:%02d", startTime.hour, startTime.minute, endTime.hour, endTime.minute)
            textWithoutTime = textWithoutTime.removeRange(hourMatch.range).trim()
        }

        val endTime = startTime.plusMinutes(durationMinutes.toLong())

        // 4. Clean Title
        // Strip trailing/leading prepositions, commas, or leftover words
        var parsedTitle = textWithoutTime
            .replace(Regex("\\bsebelumnya\\b", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^(?:pada|di|tentang|jadwal|agenda)\\s+", RegexOption.IGNORE_CASE), "")
            .replace(Regex("^[,;\\-\\.\\s]+"), "")
            .replace(Regex("[,;\\-\\.\\s]+$"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (parsedTitle.isBlank()) {
            parsedTitle = "Agenda Baru"
        }

        // Capitalize first letter
        parsedTitle = parsedTitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

        // 5. Detect Category from Title and Original Text
        val category = detectCategory(cleanInput)

        return ParsedScheduleResult(
            title = parsedTitle,
            date = targetDate,
            startTime = startTime,
            endTime = endTime,
            category = category,
            isReminderEnabled = isReminderEnabled,
            reminderMinutesBefore = reminderMinutes,
            detectedDateLabel = detectedDateLabel,
            detectedTimeLabel = detectedTimeLabel
        )
    }

    private fun adjustHourWithModifier(hour: Int, modifier: String): Int {
        return when {
            modifier in listOf("malam", "pm") && hour in 1..11 -> hour + 12
            modifier in listOf("sore") && hour in 1..6 -> hour + 12
            modifier in listOf("siang") && hour in 1..3 -> hour + 12
            modifier in listOf("pagi", "am") && hour == 12 -> 0
            else -> hour
        }
    }

    private fun detectCategory(text: String): ScheduleCategory {
        val lower = text.lowercase(Locale.ROOT)
        return when {
            listOf("ujian", "uts", "uas", "deadline", "penting", "urgent", "prioritas", "sidang").any { lower.contains(it) } -> {
                ScheduleCategory.IMPORTANT
            }
            listOf("tugas", "proyek", "project", "pr", "koding", "skripsi", "laporan", "evaluasi", "revisi", "submit").any { lower.contains(it) } -> {
                ScheduleCategory.TASK
            }
            listOf("rapat", "meeting", "kerja kelompok", "diskusi", "zoom", "meet", "kuliah", "presentasi", "webinar", "kelas", "briefing").any { lower.contains(it) } -> {
                ScheduleCategory.MEETING
            }
            listOf("olahraga", "gym", "lari", "jogging", "rutin", "workout", "sepeda", "senam").any { lower.contains(it) } -> {
                ScheduleCategory.ROUTINE
            }
            listOf("makan", "lunch", "dinner", "nonton", "keluarga", "ngopi", "belanja", "santai", "tidur", "jalan", "libur").any { lower.contains(it) } -> {
                ScheduleCategory.PERSONAL
            }
            else -> ScheduleCategory.TASK
        }
    }

    private fun formatShortDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("id"))
        return date.format(formatter)
    }
}
