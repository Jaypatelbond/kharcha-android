package com.kharcha.tracker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kharcha.core.domain.repository.TransactionRepository
import com.kharcha.core.common.util.DateUtils
import com.kharcha.tracker.util.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReminderWorkerEntryPoint {
        fun repository(): TransactionRepository
        fun recurringDao(): com.kharcha.core.database.dao.RecurringDao
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appContext = applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            ReminderWorkerEntryPoint::class.java
        )
        val repository = entryPoint.repository()
        val recurringDao = entryPoint.recurringDao()

        val startOfDay = DateUtils.getStartOfDay()
        val endOfDay = DateUtils.getEndOfDay()
        
        // 1. Check for specific recurring reminders
        val recurringItems = recurringDao.getAll().first()
        val dueItems = recurringItems.filter { 
            val nextDue = it.nextDueDate
            it.reminderEnabled && 
            it.isActive &&
            (nextDue != null && nextDue <= endOfDay) 
        }

        if (dueItems.isNotEmpty()) {
            val itemNames = dueItems.joinToString(", ") { it.note }
            NotificationHelper.showReminderNotification(
                appContext,
                title = "Upcoming Bills Due!",
                message = "Don't forget: $itemNames"
            )
            // If specific reminders exist, we might skip the generic one or show both. 
            // For now, let's prefer the specific one and return.
            return@withContext Result.success()
        }

        // 2. Fallback to generic reminder if no transactions today
        val transactionsToday = repository.getTransactionsByDateRange(startOfDay, endOfDay).first()
        
        if (transactionsToday.isEmpty()) {
            NotificationHelper.showReminderNotification(appContext)
        }
        
        Result.success()
    }
}
