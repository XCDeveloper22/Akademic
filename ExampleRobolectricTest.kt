package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.data.ScheduleItem
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScheduleAlarmReceiver : BroadcastReceiver() {
    private val TAG = "ScheduleAlarmReceiver"
    private val CHANNEL_ID = "akademic_schedule_channel"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmScheduler.ACTION_SHOW_NOTIFICATION) return

        val sId = intent.getIntExtra("schedule_id", -1)
        val title = intent.getStringExtra("schedule_title") ?: "Class lecture"
        val code = intent.getStringExtra("schedule_code") ?: ""
        val room = intent.getStringExtra("schedule_room") ?: ""
        val startTime = intent.getStringExtra("schedule_time") ?: ""
        val offset = intent.getIntExtra("offset_minutes", 30)

        Log.d(TAG, "Received alarm for $title ($code). Offset: $offset min.")

        // Show the popup heads-up notification!
        showNotification(context, sId, title, code, room, startTime, offset)

        // Reschedule for next week's occurrence of this schedule item
        rescheduleThisItem(context, sId)
    }

    private fun showNotification(
        context: Context,
        id: Int,
        title: String,
        code: String,
        room: String,
        startTime: String,
        offset: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Class Schedule Reminders"
            val descriptionText = "Notifies you before your classes start"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action when clicked - open App
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Choose appropriate visual display formatting
        val timeLabel = if (offset == 60) "1 hour" else "$offset minutes"
        val contentTitle = "Upcoming Class: $code"
        val contentText = "$title starts in $timeLabel ($startTime) at $room"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Custom metallic gold style look as per the theme
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using system alarm icon as standard/safe launcher icon
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(id, builder.build())
    }

    private fun rescheduleThisItem(context: Context, sId: Int) {
        if (sId == -1) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "akademic_db"
                ).fallbackToDestructiveMigration().build()
                
                val schedules = db.academicDao().getAllScheduleItems().first()
                val targetItem = schedules.find { it.id == sId }
                if (targetItem != null) {
                    // Simply calling rescheduleAllAlarms updates all schedule targets properly for subsequent weeks
                    AlarmScheduler.rescheduleAllAlarms(context, schedules)
                }
                db.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed rescheduling item after alert: ${e.message}")
            }
        }
    }
}
