package com.example.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.ScheduleItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ReminderManager {

    fun scheduleReminder(context: Context, item: ScheduleItem) {
        if (!item.isReminderEnabled) return

        try {
            val dateParts = item.date.split("-").mapNotNull { it.toIntOrNull() }
            if (dateParts.size < 3) return

            val targetDate = LocalDate.of(dateParts[0], dateParts[1], dateParts[2])
            val targetTime = LocalTime.of(item.startHour, item.startMinute)
            val scheduleDateTime = LocalDateTime.of(targetDate, targetTime)
                .minusMinutes(item.reminderMinutesBefore.toLong())

            val triggerEpochMilli = scheduleDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            // If time has already passed, don't schedule
            if (triggerEpochMilli <= System.currentTimeMillis()) return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val intent = Intent(context, ScheduleReminderReceiver::class.java).apply {
                putExtra(ScheduleReminderReceiver.EXTRA_TITLE, item.title)
                putExtra(ScheduleReminderReceiver.EXTRA_TIME, "${item.startTime} - ${item.endTime}")
                putExtra(ScheduleReminderReceiver.EXTRA_CATEGORY, item.categoryEnum.displayName)
                putExtra(ScheduleReminderReceiver.EXTRA_ID, item.id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                item.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMilli,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMilli,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMilli,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            // Gracefully ignore if permissions or parsing issue
        }
    }

    fun cancelReminder(context: Context, itemId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ScheduleReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                itemId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (_: Exception) {
            // Ignore
        }
    }
}
