package com.bistu.focuslist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bistu.focuslist.util.NotificationHelper

/**
 * 任务提醒广播接收器（四大组件之一）。
 * 由 AlarmManager 在任务到点时唤起，弹出提醒通知。
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMIND) return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "你有一项任务到期了"
        NotificationHelper.showReminder(context, taskId, title)
    }

    companion object {
        const val ACTION_REMIND = "com.bistu.focuslist.action.REMIND"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }
}
