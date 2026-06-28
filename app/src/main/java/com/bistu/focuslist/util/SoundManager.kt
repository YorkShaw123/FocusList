package com.bistu.focuslist.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.bistu.focuslist.R

/**
 * 多媒体管理：使用 SoundPool 播放“完成提示音”，并控制振动。
 * SoundPool 适合播放短促音效，是 Android 多媒体 API 的典型用法。
 */
object SoundManager {

    private var soundPool: SoundPool? = null
    private var chimeId: Int = 0
    private var loaded: Boolean = false

    fun init(context: Context) {
        if (soundPool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attrs)
            .build()
        pool.setOnLoadCompleteListener { _, _, status ->
            loaded = status == 0
        }
        chimeId = pool.load(context.applicationContext, R.raw.chime, 1)
        soundPool = pool
    }

    /** 播放完成提示音；force=true 时忽略开关（用于设置页试听） */
    fun playChime(context: Context, force: Boolean = false) {
        if (!force && !Prefs.isSoundEnabled(context)) return
        if (soundPool == null) init(context)
        if (loaded) {
            soundPool?.play(chimeId, 1f, 1f, 1, 0, 1f)
        }
    }

    /** 振动一次；force=true 时忽略开关（用于设置页测试） */
    fun vibrate(context: Context, durationMs: Long = 250L, force: Boolean = false) {
        if (!force && !Prefs.isVibrateEnabled(context)) return
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(
            VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }
}
