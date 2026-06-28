package com.bistu.focuslist.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bistu.focuslist.R
import com.bistu.focuslist.databinding.ActivityMainBinding
import com.bistu.focuslist.ui.focus.FocusFragment
import com.bistu.focuslist.ui.settings.SettingsActivity
import com.bistu.focuslist.ui.stats.StatsFragment
import com.bistu.focuslist.ui.tasks.TaskListFragment

/**
 * 主界面（Activity）。
 * 使用 BottomNavigationView + Fragment 实现“待办 / 专注 / 统计”三大模块切换。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* 忽略结果 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_tasks -> switchTo(TAB_TASKS)
                R.id.nav_focus -> switchTo(TAB_FOCUS)
                R.id.nav_stats -> switchTo(TAB_STATS)
            }
            true
        }

        val initialTab = intent.getIntExtra(EXTRA_OPEN_TAB, TAB_TASKS)
        binding.bottomNav.selectedItemId = tabToMenuId(initialTab)

        requestNotificationPermissionIfNeeded()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        val tab = intent.getIntExtra(EXTRA_OPEN_TAB, TAB_TASKS)
        binding.bottomNav.selectedItemId = tabToMenuId(tab)
    }

    /** 找到或创建对应 Fragment，隐藏其它、显示目标，兼容配置变更后的恢复。 */
    private fun switchTo(index: Int) {
        val tag = "tab_$index"
        val fm = supportFragmentManager
        val target = fm.findFragmentByTag(tag) ?: createFragment(index)

        val tx = fm.beginTransaction()
        fm.fragments.forEach { tx.hide(it) }
        if (!target.isAdded) tx.add(R.id.fragmentContainer, target, tag)
        tx.show(target)
        tx.commitNowAllowingStateLoss()

        binding.toolbar.title = when (index) {
            TAB_FOCUS -> getString(R.string.tab_focus)
            TAB_STATS -> getString(R.string.tab_stats)
            else -> getString(R.string.app_name)
        }
    }

    private fun createFragment(index: Int): Fragment = when (index) {
        TAB_FOCUS -> FocusFragment()
        TAB_STATS -> StatsFragment()
        else -> TaskListFragment()
    }

    /** 供 Fragment 调用：从任务列表快速开始专注后跳到专注页 */
    fun showFocusTab() {
        binding.bottomNav.selectedItemId = R.id.nav_focus
    }

    private fun tabToMenuId(tab: Int): Int = when (tab) {
        TAB_FOCUS -> R.id.nav_focus
        TAB_STATS -> R.id.nav_stats
        else -> R.id.nav_tasks
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(android.content.Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_OPEN_TAB = "extra_open_tab"
        const val TAB_TASKS = 0
        const val TAB_FOCUS = 1
        const val TAB_STATS = 2
    }
}
