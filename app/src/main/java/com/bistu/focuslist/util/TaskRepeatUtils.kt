package com.bistu.focuslist.util

import com.bistu.focuslist.data.Task
import com.bistu.focuslist.R
import java.util.Calendar

object TaskRepeatUtils {

    fun repeatLabel(task: Task): Int? = when (task.repeatPeriod) {
        Task.REPEAT_DAILY -> R.string.repeat_daily
        Task.REPEAT_WEEKLY -> R.string.repeat_weekly
        Task.REPEAT_MONTHLY -> R.string.repeat_monthly
        else -> null
    }

    fun nextOccurrence(task: Task, now: Long = System.currentTimeMillis()): Task? {
        val due = task.dueTime ?: return null
        if (task.repeatPeriod == Task.REPEAT_NONE) return null
        var next = due
        do {
            next = addPeriod(next, task.repeatPeriod)
        } while (next <= now)
        return task.copy(isDone = false, dueTime = next)
    }

    private fun addPeriod(time: Long, repeatPeriod: Int): Long {
        return Calendar.getInstance().apply {
            timeInMillis = time
            when (repeatPeriod) {
                Task.REPEAT_DAILY -> add(Calendar.DAY_OF_YEAR, 1)
                Task.REPEAT_WEEKLY -> add(Calendar.WEEK_OF_YEAR, 1)
                Task.REPEAT_MONTHLY -> add(Calendar.MONTH, 1)
            }
        }.timeInMillis
    }
}
