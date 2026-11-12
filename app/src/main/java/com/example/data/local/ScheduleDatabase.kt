package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ScheduleCategory
import com.example.data.model.ScheduleItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(entities = [ScheduleItem::class], version = 1, exportSchema = false)
abstract class ScheduleDatabase : RoomDatabase() {

    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: ScheduleDatabase? = null

        fun getInstance(context: Context): ScheduleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleDatabase::class.java,
                    "jadwalku_database.db"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getInstance(context).scheduleDao()
                seedInitialSchedules(dao)
            }
        }

        private suspend fun seedInitialSchedules(dao: ScheduleDao) {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val todayStr = today.format(formatter)
            val tomorrowStr = today.plusDays(1).format(formatter)
            val dayAfterTomorrowStr = today.plusDays(2).format(formatter)
            val nextWeekStr = today.plusDays(4).format(formatter)

            val seedData = listOf(
                // Jadwal Hari Ini
                ScheduleItem(
                    title = "Standup Tim Pengembang",
                    description = "Sinkronisasi progres fitur jadwal dan sprint review di Google Meet",
                    date = todayStr,
                    startTime = "09:00",
                    endTime = "10:00",
                    category = ScheduleCategory.MEETING.id,
                    isCompleted = true,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 10
                ),
                ScheduleItem(
                    title = "Review Desain UI & Prototipe",
                    description = "Cek hierarki visual dark mode, warna aksen hijau dan biru",
                    date = todayStr,
                    startTime = "11:00",
                    endTime = "12:30",
                    category = ScheduleCategory.TASK.id,
                    isCompleted = false,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 15
                ),
                ScheduleItem(
                    title = "Makan Siang & Istirahat",
                    description = "Makan siang bersama tim di kantin",
                    date = todayStr,
                    startTime = "12:30",
                    endTime = "13:30",
                    category = ScheduleCategory.PERSONAL.id,
                    isCompleted = false,
                    isReminderEnabled = false
                ),
                ScheduleItem(
                    title = "Sesi Koding & Integrasi Database",
                    description = "Menyelesaikan implementasi Room dan gesture swipe interaktif",
                    date = todayStr,
                    startTime = "14:00",
                    endTime = "16:00",
                    category = ScheduleCategory.TASK.id,
                    isCompleted = false,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 15
                ),
                ScheduleItem(
                    title = "Olahraga Sore / Gym",
                    description = "Lari 30 menit dan peregangan di fitness center",
                    date = todayStr,
                    startTime = "17:30",
                    endTime = "18:45",
                    category = ScheduleCategory.ROUTINE.id,
                    isCompleted = false,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 30
                ),

                // Jadwal Besok
                ScheduleItem(
                    title = "Kerja Kelompok Proyek Akhir",
                    description = "Diskusi bab implementasi dan persiapan demo aplikasi",
                    date = tomorrowStr,
                    startTime = "10:00",
                    endTime = "12:00",
                    category = ScheduleCategory.MEETING.id,
                    isCompleted = false,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 15
                ),
                ScheduleItem(
                    title = "Deadline Pengumpulan Laporan",
                    description = "Unggah dokumen PDF ke portal akademik sebelum batas waktu",
                    date = tomorrowStr,
                    startTime = "15:00",
                    endTime = "16:00",
                    category = ScheduleCategory.IMPORTANT.id,
                    isCompleted = false,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 60
                ),
                ScheduleItem(
                    title = "Makan Malam Keluarga",
                    description = "Reservasi meja di restoran seafood",
                    date = tomorrowStr,
                    startTime = "19:00",
                    endTime = "21:00",
                    category = ScheduleCategory.PERSONAL.id,
                    isCompleted = false,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 30
                ),

                // Jadwal Lusa
                ScheduleItem(
                    title = "Kuliah Tamu Arsitektur Mobile",
                    description = "Narasumber dari industri tentang Compose modern",
                    date = dayAfterTomorrowStr,
                    startTime = "09:30",
                    endTime = "11:30",
                    category = ScheduleCategory.MEETING.id,
                    isCompleted = false,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 15
                ),
                ScheduleItem(
                    title = "Evaluasi Mingguan & Perencanaan",
                    description = "Rencana target agenda dan prioritas mingguan berikutnya",
                    date = nextWeekStr,
                    startTime = "08:30",
                    endTime = "10:00",
                    category = ScheduleCategory.TASK.id,
                    isCompleted = false,
                    isReminderEnabled = true,
                    reminderMinutesBefore = 15
                )
            )

            dao.insertAll(seedData)
        }
    }
}
