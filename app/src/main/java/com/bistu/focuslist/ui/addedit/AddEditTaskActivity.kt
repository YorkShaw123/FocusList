package com.bistu.focuslist.ui.addedit

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bistu.focuslist.R
import com.bistu.focuslist.data.Repository
import com.bistu.focuslist.data.Task
import com.bistu.focuslist.databinding.ActivityAddEditTaskBinding
import com.bistu.focuslist.util.AlarmScheduler
import com.bistu.focuslist.util.TimeUtils
import com.bistu.focuslist.widget.TaskWidgetProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 添加 / 编辑任务页（Activity）。
 * 演示：多 Activity 跳转与回传、EditText / Spinner / Chip / Switch 等基础控件、
 * DatePicker + TimePicker 时间选择，以及把数据写入 Room 并联动闹钟与小组件。
 */
class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding
    private val repo by lazy { Repository.get(this) }

    private var editingTaskId: Long = -1L
    private var existingTask: Task? = null
    private var dueTime: Long? = null

    private val categories by lazy {
        listOf(
            getString(R.string.cat_study),
            getString(R.string.cat_work),
            getString(R.string.cat_life),
            getString(R.string.cat_other)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editingTaskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        binding.toolbar.title =
            if (editingTaskId > 0) getString(R.string.edit_task) else getString(R.string.add_task)

        // 分类下拉
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        // 提醒开关
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            binding.btnPickTime.isEnabled = isChecked
            if (isChecked && dueTime == null) {
                pickDateTime()
            } else if (!isChecked) {
                dueTime = null
                binding.textDueTime.text = getString(R.string.no_reminder)
            }
        }
        binding.btnPickTime.setOnClickListener { pickDateTime() }
        binding.btnPickTime.isEnabled = false

        if (editingTaskId > 0) loadTask(editingTaskId)
    }

    private fun loadTask(id: Long) {
        lifecycleScope.launch {
            val task = repo.getTask(id) ?: return@launch
            existingTask = task
            binding.editTitle.setText(task.title)
            binding.editNotes.setText(task.notes)
            binding.spinnerCategory.setSelection(
                categories.indexOf(task.category).coerceAtLeast(0)
            )
            when (task.priority) {
                Task.PRIORITY_LOW -> binding.chipLow.isChecked = true
                Task.PRIORITY_HIGH -> binding.chipHigh.isChecked = true
                else -> binding.chipNormal.isChecked = true
            }
            dueTime = task.dueTime
            if (task.dueTime != null) {
                binding.switchReminder.isChecked = true
                binding.textDueTime.text = TimeUtils.formatDateTime(task.dueTime)
            }
        }
    }

    private fun pickDateTime() {
        val init = Calendar.getInstance().apply {
            dueTime?.let { timeInMillis = it }
        }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                TimePickerDialog(
                    this,
                    { _, hour, minute ->
                        val c = Calendar.getInstance()
                        c.set(year, month, day, hour, minute, 0)
                        c.set(Calendar.MILLISECOND, 0)
                        dueTime = c.timeInMillis
                        binding.textDueTime.text = TimeUtils.formatDateTime(c.timeInMillis)
                    },
                    init.get(Calendar.HOUR_OF_DAY),
                    init.get(Calendar.MINUTE),
                    true
                ).show()
            },
            init.get(Calendar.YEAR),
            init.get(Calendar.MONTH),
            init.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun selectedPriority(): Int = when (binding.chipGroupPriority.checkedChipId) {
        R.id.chipLow -> Task.PRIORITY_LOW
        R.id.chipHigh -> Task.PRIORITY_HIGH
        else -> Task.PRIORITY_NORMAL
    }

    private fun saveTask() {
        val title = binding.editTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) {
            binding.editTitle.error = getString(R.string.title_required)
            return
        }
        val category = categories.getOrElse(binding.spinnerCategory.selectedItemPosition) {
            categories.first()
        }
        val notes = binding.editNotes.text?.toString()?.trim().orEmpty()
        val reminder = if (binding.switchReminder.isChecked) dueTime else null

        lifecycleScope.launch {
            val base = existingTask
            if (base == null) {
                val newTask = Task(
                    title = title,
                    notes = notes,
                    category = category,
                    priority = selectedPriority(),
                    dueTime = reminder
                )
                val newId = repo.insertTask(newTask)
                AlarmScheduler.schedule(this@AddEditTaskActivity, newTask.copy(id = newId))
            } else {
                val updated = base.copy(
                    title = title,
                    notes = notes,
                    category = category,
                    priority = selectedPriority(),
                    dueTime = reminder
                )
                repo.updateTask(updated)
                AlarmScheduler.cancel(this@AddEditTaskActivity, updated.id)
                AlarmScheduler.schedule(this@AddEditTaskActivity, updated)
            }
            TaskWidgetProvider.notifyRefresh(this@AddEditTaskActivity)
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun deleteTask() {
        val task = existingTask ?: run { finish(); return }
        lifecycleScope.launch {
            repo.deleteTask(task)
            AlarmScheduler.cancel(this@AddEditTaskActivity, task.id)
            TaskWidgetProvider.notifyRefresh(this@AddEditTaskActivity)
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_add_edit, menu)
        menu.findItem(R.id.action_delete)?.isVisible = editingTaskId > 0
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> { saveTask(); true }
            R.id.action_delete -> { deleteTask(); true }
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }
}
