package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d(TAG, "Device rebooted. Rescheduling all academic class reminders.")
            
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, "akademic_db"
                    ).fallbackToDestructiveMigration().build()
                    
                    val schedules = db.academicDao().getAllScheduleItems().first()
                    AlarmScheduler.rescheduleAllAlarms(context, schedules)

                    val tasks = db.academicDao().getAllTasks().first()
                    tasks.forEach { task ->
                        if (task.isReminderEnabled) {
                            TaskAlarmScheduler.scheduleTaskReminder(context, task)
                        }
                    }
                    db.close()
                    Log.d(TAG, "Successfully restored ${schedules.size} class alarms and active task reminders on boot.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring alarms on boot: ${e.message}")
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
