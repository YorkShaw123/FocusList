package com.bistu.focuslist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bistu.focuslist.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 开机广播接收器（四大组件之一）。
 * 设备重启后，系统会清除所有已注册的闹钟，
 * 因此这里监听 BOOT_COMPLETED，重新为未完成任务排程提醒。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // 数据库读取是耗时操作，用 goAsync() 申请额外执行时间
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                AlarmScheduler.rescheduleAll(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
