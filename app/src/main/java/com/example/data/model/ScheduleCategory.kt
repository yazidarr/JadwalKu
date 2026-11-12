package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CatImportant
import com.example.ui.theme.CatMeeting
import com.example.ui.theme.CatPersonal
import com.example.ui.theme.CatRoutine
import com.example.ui.theme.CatTask

enum class ScheduleCategory(
    val id: String,
    val displayName: String,
    val color: Color,
    val iconName: String
) {
    TASK("task", "Tugas", CatTask, "task"),
    MEETING("meeting", "Rapat / Kuliah", CatMeeting, "group"),
    PERSONAL("personal", "Pribadi", CatPersonal, "person"),
    IMPORTANT("important", "Penting / Deadline", CatImportant, "priority_high"),
    ROUTINE("routine", "Rutinitas", CatRoutine, "schedule");

    companion object {
        fun fromId(id: String): ScheduleCategory {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: TASK
        }
    }
}
