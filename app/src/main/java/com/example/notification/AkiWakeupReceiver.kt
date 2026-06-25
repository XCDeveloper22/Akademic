package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class AkiWakeupReceiver : BroadcastReceiver() {
    private val TAG = "AkiWakeupReceiver"
    private val CHANNEL_ID = "akademic_aki_channel"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Aki wakeup broadcast received.")

        // 1. Wake up the phone screen
        wakeupScreen(context)

        // 2. Show the cute wakeup alert notification in the notification drawer
        showAkiNotification(context)

        // 3. Reschedule the next alarm to trigger in 12 hours
        AkiAlarmScheduler.scheduleNextAkiWakeup(context)
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
                        "Akademic::AkiWakeLockTag"
                    )
                    wl.acquire(6000L) // Keep screen on for 6 seconds
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring wake lock: ${e.message}")
        }
    }

    private fun showAkiNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mochi Study Phoenix Alerts"
            val descriptionText = "Friendly reminders to keep Mochi's study fire burning bright"
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

        // Click launches main app and opens Journal section (sectionIndex = 5)
        MainActivity.pendingSection = 5
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            99901,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Get the matching cute quote for the current 12-hour block
        val cuteMessage = AkiQuotes.getCurrentQuote(context)

        // Decode our brand logo to display in the notification drawer!
        val largeLogoBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.akademic_app_icon_1782210443308)
        } catch (e: Exception) {
            null
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.star_on) // Standard safe icon
            .setContentTitle("🔥 Mochi's Fire Needs You!")
            .setContentText(cuteMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(cuteMessage))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (largeLogoBitmap != null) {
            builder.setLargeIcon(largeLogoBitmap)
        }

        notificationManager.notify(99902, builder.build())
    }
}
