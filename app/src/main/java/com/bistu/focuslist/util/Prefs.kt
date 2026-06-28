package com.bistu.focuslist.util

import android.content.Context

/**
 * SharedPreferences 封装，保存用户设置（轻量级数据持久化）。
 */
object Prefs {
    private const val NAME = "focuslist_prefs"

    private const val KEY_FOCUS_MINUTES = "focus_minutes"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_AMBIENT = "ambient_enabled"
    private const val KEY_VIBRATE = "vibrate_enabled"
    private const val KEY_LAST_QUOTE = "last_quote"
    private const val KEY_LAST_QUOTE_FROM = "last_quote_from"

    const val DEFAULT_FOCUS_MINUTES = 25

    private fun sp(context: Context) =
        context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun getFocusMinutes(context: Context): Int =
        sp(context).getInt(KEY_FOCUS_MINUTES, DEFAULT_FOCUS_MINUTES)

    fun setFocusMinutes(context: Context, minutes: Int) =
        sp(context).edit().putInt(KEY_FOCUS_MINUTES, minutes).apply()

    fun isSoundEnabled(context: Context): Boolean =
        sp(context).getBoolean(KEY_SOUND, true)

    fun setSoundEnabled(context: Context, enabled: Boolean) =
        sp(context).edit().putBoolean(KEY_SOUND, enabled).apply()

    fun isAmbientEnabled(context: Context): Boolean =
        sp(context).getBoolean(KEY_AMBIENT, false)

    fun setAmbientEnabled(context: Context, enabled: Boolean) =
        sp(context).edit().putBoolean(KEY_AMBIENT, enabled).apply()

    fun isVibrateEnabled(context: Context): Boolean =
        sp(context).getBoolean(KEY_VIBRATE, true)

    fun setVibrateEnabled(context: Context, enabled: Boolean) =
        sp(context).edit().putBoolean(KEY_VIBRATE, enabled).apply()

    fun getLastQuote(context: Context): String =
        sp(context).getString(KEY_LAST_QUOTE, "") ?: ""

    fun getLastQuoteFrom(context: Context): String =
        sp(context).getString(KEY_LAST_QUOTE_FROM, "") ?: ""

    fun saveQuote(context: Context, text: String, from: String) =
        sp(context).edit()
            .putString(KEY_LAST_QUOTE, text)
            .putString(KEY_LAST_QUOTE_FROM, from)
            .apply()
}
