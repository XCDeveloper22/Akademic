package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.ZonedDateTime
import java.time.ZoneId
import java.util.Calendar

object AkiAlarmScheduler {
    private const val TAG = "AkiAlarmScheduler"
    const val ACTION_AKI_WAKEUP = "com.example.ACTION_AKI_WAKEUP"
    const val REQUEST_CODE = 99903

    fun scheduleNextAkiWakeup(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        // 1. Get the user's selected timezone
        val prefs = context.getSharedPreferences("akademic_timezone_prefs", Context.MODE_PRIVATE)
        val zoneIdStr = prefs.getString("selected_zone", "Asia/Manila") ?: "Asia/Manila"
        val zoneId = try { ZoneId.of(zoneIdStr) } catch (e: Exception) { ZoneId.systemDefault() }

        // 2. Compute the next 12-hour block transition time (12:00 or 00:00)
        val now = ZonedDateTime.now(zoneId)
        val nextTransition = if (now.hour < 12) {
            now.toLocalDate().atTime(12, 0).atZone(zoneId)
        } else {
            now.toLocalDate().plusDays(1).atStartOfDay(zoneId)
        }

        val triggerMillis = nextTransition.toInstant().toEpochMilli()

        // Create the alarm pending intent
        val intent = Intent(context, AkiWakeupReceiver::class.java).apply {
            action = ACTION_AKI_WAKEUP
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            }
            Log.d(TAG, "Scheduled next Aki wakeup alert for: ${nextTransition.toLocalDateTime()} in timezone: $zoneIdStr")
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
            Log.w(TAG, "SecurityException: scheduled standard alarm fallback instead of exact.")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling Aki alarm: ${e.message}")
        }
    }
}
