package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class TaskAlarmReceiver : BroadcastReceiver() {
    private val TAG = "TaskAlarmReceiver"
    private val CHANNEL_ID = "akademic_task_channel"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TaskAlarmScheduler.ACTION_SHOW_TASK_REMINDER) return

        val taskId = intent.getIntExtra("task_id", -1)
        val title = intent.getStringExtra("task_title") ?: "Task reminder"
        val desc = intent.getStringExtra("task_desc") ?: "Do not forget your offline task."

        Log.d(TAG, "Task notification received: ID=$taskId, Title=$title")

        // Wake up phone screen
        wakeupScreen(context)

        // Show popup heads-up alert
        showTaskPopupNotification(context, taskId, title, desc)
    }

    private fun wakeupScreen(context: Context) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (pm != null) {
                val isInteractive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                    pm.isInteractive
                } else {
                    @Suppress("DEPRECATION")
                    pm.isScreenOn
                }
                if (!isInteractive) {
                    @Suppress("DEPRECATION")
                    val wl = pm.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                        "Akademic::TaskWakeLockTag"
                    )
                    wl.acquire(5000L) // Release lock after 5 seconds automatically
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Wake up lock registration error: ${e.message}")
        }
    }

    private fun showTaskPopupNotification(context: Context, taskId: Int, title: String, desc: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task & Study Reminders"
            val descriptionText = "Get real-time notification alerts when your tasks are active"
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

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId + 50000,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("📌 TASK REMINDER: $title")
            .setContentText(if (desc.isBlank()) "Keep up your task schedule!" else desc)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 800, 300, 800, 300, 800))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(taskId + 40000, builder.build())
    }
}
