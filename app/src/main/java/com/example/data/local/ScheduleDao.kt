package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules ORDER BY date ASC, startTime ASC")
    fun getAllSchedules(): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules WHERE date = :date ORDER BY startTime ASC")
    fun getSchedulesByDate(date: String): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, startTime ASC")
    fun getSchedulesBetweenDates(startDate: String, endDate: String): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ScheduleItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<ScheduleItem>)

    @Update
    suspend fun updateSchedule(schedule: ScheduleItem)

    @Query("UPDATE schedules SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Query("UPDATE schedules SET startTime = :startTime, endTime = :endTime WHERE id = :id")
    suspend fun updateTime(id: Long, startTime: String, endTime: String)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleItem)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("SELECT COUNT(*) FROM schedules")
    suspend fun getCount(): Int
}
