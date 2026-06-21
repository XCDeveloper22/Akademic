package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.ScheduleItem
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"
    const val ACTION_SHOW_NOTIFICATION = "com.example.ACTION_SHOW_SCHEDULE_NOTIFICATION"

    fun rescheduleAllAlarms(context: Context, scheduleItems: List<ScheduleItem>) {
        val prefs = context.getSharedPreferences("akademic_notifications", Context.MODE_PRIVATE)
        val notify30Mins = prefs.getBoolean("notify_30_mins", true)
        val notify1Hour = prefs.getBoolean("notify_1_hour", false)

        Log.d(TAG, "Rescheduling all alarms: 30m=$notify30Mins, 1h=$notify1Hour, itemsCount=${scheduleItems.size}")

        // Cancel all potential alarms first
        cancelAllAlarms(context, scheduleItems)

        if (!notify30Mins && !notify1Hour) {
            return
        }

        for (item in scheduleItems) {
            if (notify30Mins) {
                scheduleAlarm(context, item, offsetMinutes = 30, requestCode = item.id * 2)
            }
            if (notify1Hour) {
                scheduleAlarm(context, item, offsetMinutes = 60, requestCode = item.id * 2 + 1)
            }
        }
    }

    private fun cancelAllAlarms(context: Context, scheduleItems: List<ScheduleItem>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (item in scheduleItems) {
            // Cancel both 30m and 1h
            cancelAlarm(context, alarmManager, item.id * 2)
            cancelAlarm(context, alarmManager, item.id * 2 + 1)
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ACTION_SHOW_NOTIFICATION
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun scheduleAlarm(context: Context, item: ScheduleItem, offsetMinutes: Int, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Parse weekday
        val dayOfWeek = getCalendarDayOfWeek(item.dayOfWeek) ?: return

        // Parse startTime "HH:mm"
        val timeParts = item.startTime.split(":")
        if (timeParts.size != 2) return
        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        val triggerTime = calculateNextTriggerTime(dayOfWeek, hour, minute, offsetMinutes)

        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            action = ACTION_SHOW_NOTIFICATION
            putExtra("schedule_id", item.id)
            putExtra("schedule_title", item.title)
            putExtra("schedule_code", item.code)
            putExtra("schedule_room", item.room)
            putExtra("schedule_time", item.startTime)
            putExtra("offset_minutes", offsetMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
            Log.d(TAG, "Scheduled alarm for item '${item.title}' in $offsetMinutes min at ${Calendar.getInstance().apply { timeInMillis = triggerTime }.time} (ReqCode: $requestCode)")
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Log.w(TAG, "SecurityException scheduling exact alarm, fallback to standard: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm: ${e.message}")
        }
    }

    private fun getCalendarDayOfWeek(day: String): Int? {
        return when (day.lowercase().trim()) {
            "sunday" -> Calendar.SUNDAY
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            else -> null
        }
    }

    fun calculateNextTriggerTime(dayOfWeek: Int, hourOfDay: Int, minuteOfHour: Int, offsetMinutes: Int): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minuteOfHour)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (target.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Subtract the offset minutes
        target.add(Calendar.MINUTE, -offsetMinutes)

        // If target is in the past, add 1 week
        if (target.timeInMillis <= now) {
            target.add(Calendar.WEEK_OF_YEAR, 1)
        }

        return target.timeInMillis
    }
}
