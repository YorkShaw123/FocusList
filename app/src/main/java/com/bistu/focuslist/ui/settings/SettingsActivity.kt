package com.bistu.focuslist.ui.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.bistu.focuslist.R
import com.bistu.focuslist.databinding.ActivitySettingsBinding
import com.bistu.focuslist.util.Prefs
import com.bistu.focuslist.util.SoundManager

/**
 * 设置页（Activity）。
 * 管理默认专注时长、提示音 / 白噪音 / 振动开关，并提供试听、试振按钮。
 * 设置项保存在 SharedPreferences（轻量持久化）。
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings)

        // 默认专注时长
        val minutes = Prefs.getFocusMinutes(this)
        binding.sliderFocus.value = minutes.toFloat().coerceIn(5f, 60f)
        binding.textFocusValue.text = getString(R.string.minutes_fmt, minutes)
        binding.sliderFocus.addOnChangeListener { _, value, _ ->
            val m = value.toInt()
            Prefs.setFocusMinutes(this, m)
            binding.textFocusValue.text = getString(R.string.minutes_fmt, m)
        }

        // 开关
        binding.switchSound.isChecked = Prefs.isSoundEnabled(this)
        binding.switchSound.setOnCheckedChangeListener { _, checked ->
            Prefs.setSoundEnabled(this, checked)
        }
        binding.switchAmbient.isChecked = Prefs.isAmbientEnabled(this)
        binding.switchAmbient.setOnCheckedChangeListener { _, checked ->
            Prefs.setAmbientEnabled(this, checked)
        }
        binding.switchVibrate.isChecked = Prefs.isVibrateEnabled(this)
        binding.switchVibrate.setOnCheckedChangeListener { _, checked ->
            Prefs.setVibrateEnabled(this, checked)
        }

        // 试听 / 试振
        binding.btnTestSound.setOnClickListener { SoundManager.playChime(this, force = true) }
        binding.btnTestVibrate.setOnClickListener { SoundManager.vibrate(this, force = true) }

        binding.textVersion.text = getString(R.string.version_fmt, "1.0")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
