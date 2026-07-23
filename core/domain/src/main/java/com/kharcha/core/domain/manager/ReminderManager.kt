package com.kharcha.core.domain.manager

import android.content.Context

interface ReminderManager {
    fun scheduleReminder(context: Context, hour: Int, minute: Int, enable: Boolean)
    fun sendTestNotification(context: Context)
}
