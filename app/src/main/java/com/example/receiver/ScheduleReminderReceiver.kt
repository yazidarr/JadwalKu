package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class ScheduleReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "jadwalku_reminders_channel"
        const val EXTRA_TITLE = "extra_schedule_title"
        const val EXTRA_TIME = "extra_schedule_time"
        const val EXTRA_CATEGORY = "extra_schedule_category"
        const val EXTRA_ID = "extra_schedule_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Agenda JadwalKu"
        val time = intent.getStringExtra(EXTRA_TIME) ?: ""
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Agenda"
        val scheduleId = intent.getLongExtra(EXTRA_ID, System.currentTimeMillis())

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pengingat JadwalKu",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat agenda dan jadwal harian"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (time.isNotBlank()) "Pukul $time • $category" else category

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("🔔 $title")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Pengingat untuk: $title\n$contentText"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(scheduleId.toInt(), notification)
    }
}
