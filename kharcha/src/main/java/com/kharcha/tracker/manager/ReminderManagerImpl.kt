package com.kharcha.tracker.manager

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kharcha.core.domain.manager.ReminderManager
import com.kharcha.tracker.util.NotificationHelper
import com.kharcha.tracker.worker.ReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderManagerImpl @Inject constructor() : ReminderManager {
    override fun scheduleReminder(context: Context, hour: Int, minute: Int, enable: Boolean) {
        val workManager = WorkManager.getInstance(context)
        
        if (enable) {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
            
            if (target.before(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val delay = target.timeInMillis - now.timeInMillis
            
            val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
                
            workManager.enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        } else {
            workManager.cancelUniqueWork("daily_reminder")
        }
    }

    override fun sendTestNotification(context: Context) {
        NotificationHelper.showReminderNotification(
            context,
            "Test Reminder \uD83D\uDD14",
            "This is how your daily reminder will look!"
        )
    }
}
