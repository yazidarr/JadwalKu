package com.example.data.repository

import com.example.data.local.ScheduleDao
import com.example.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val dao: ScheduleDao) {

    fun getAllSchedules(): Flow<List<ScheduleItem>> = dao.getAllSchedules()

    fun getSchedulesByDate(date: String): Flow<List<ScheduleItem>> = dao.getSchedulesByDate(date)

    fun getSchedulesBetweenDates(startDate: String, endDate: String): Flow<List<ScheduleItem>> =
        dao.getSchedulesBetweenDates(startDate, endDate)

    suspend fun getScheduleById(id: Long): ScheduleItem? = dao.getScheduleById(id)

    suspend fun insertSchedule(schedule: ScheduleItem): Long = dao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: ScheduleItem) = dao.updateSchedule(schedule)

    suspend fun toggleCompleted(schedule: ScheduleItem) {
        dao.updateCompletionStatus(schedule.id, !schedule.isCompleted)
    }

    suspend fun setCompleted(id: Long, completed: Boolean) {
        dao.updateCompletionStatus(id, completed)
    }

    suspend fun shiftScheduleTime(schedule: ScheduleItem, minutesToAdd: Int) {
        val startHour = schedule.startHour
        val startMin = schedule.startMinute
        val duration = schedule.durationInMinutes

        var newStartTotal = (startHour * 60 + startMin + minutesToAdd)
        if (newStartTotal < 0) newStartTotal = 0
        if (newStartTotal > 23 * 60 + 45) newStartTotal = 23 * 60 + 45

        val newStartH = newStartTotal / 60
        val newStartM = newStartTotal % 60

        val newEndTotal = (newStartTotal + duration).coerceAtMost(23 * 60 + 59)
        val newEndH = newEndTotal / 60
        val newEndM = newEndTotal % 60

        val newStartStr = String.format("%02d:%02d", newStartH, newStartM)
        val newEndStr = String.format("%02d:%02d", newEndH, newEndM)

        dao.updateTime(schedule.id, newStartStr, newEndStr)
    }

    suspend fun deleteSchedule(schedule: ScheduleItem) = dao.deleteSchedule(schedule)

    suspend fun deleteScheduleById(id: Long) = dao.deleteScheduleById(id)
}
