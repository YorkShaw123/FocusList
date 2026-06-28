package com.bistu.focuslist

import android.app.Application
import com.bistu.focuslist.util.NotificationHelper
import com.bistu.focuslist.util.SoundManager

/**
 * 自定义 Application：App 启动时创建通知渠道、预加载音效。
 */
class FocusListApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        SoundManager.init(this)
    }
}
