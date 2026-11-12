package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val date: String, // "yyyy-MM-dd"
    val startTime: String, // "HH:mm" (24-hour format)
    val endTime: String, // "HH:mm" (24-hour format)
    val category: String = ScheduleCategory.TASK.id,
    val isCompleted: Boolean = false,
    val isReminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 15, // 0 = at time of event, 15 = 15 min before
    val createdAt: Long = System.currentTimeMillis()
) {
    val categoryEnum: ScheduleCategory
        get() = ScheduleCategory.fromId(category)

    val startHour: Int
        get() = startTime.split(":").getOrNull(0)?.toIntOrNull() ?: 0

    val startMinute: Int
        get() = startTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0

    val endHour: Int
        get() = endTime.split(":").getOrNull(0)?.toIntOrNull() ?: (startHour + 1)

    val endMinute: Int
        get() = endTime.split(":").getOrNull(1)?.toIntOrNull() ?: startMinute

    val durationInMinutes: Int
        get() {
            val totalStart = startHour * 60 + startMinute
            val totalEnd = endHour * 60 + endMinute
            return if (totalEnd > totalStart) totalEnd - totalStart else 60
        }
}
