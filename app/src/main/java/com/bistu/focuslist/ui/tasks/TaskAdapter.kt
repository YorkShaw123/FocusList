package com.bistu.focuslist.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bistu.focuslist.R
import com.bistu.focuslist.data.Task
import com.bistu.focuslist.databinding.ItemTaskBinding
import com.bistu.focuslist.util.TimeUtils

/**
 * 任务列表适配器（适配器视图）。
 * 使用 ListAdapter + DiffUtil 实现高效的差量刷新。
 */
class TaskAdapter(
    private val onToggle: (Task) -> Unit,
    private val onClick: (Task) -> Unit,
    private val onStartFocus: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DIFF) {

    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        val ctx = holder.itemView.context
        with(holder.binding) {
            // 先解绑监听，避免复用时误触发
            checkboxDone.setOnCheckedChangeListener(null)
            checkboxDone.isChecked = task.isDone

            textTitle.text = task.title
            textTitle.paintFlags = if (task.isDone) {
                textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            // 分类与番茄数
            textCategory.text = task.category
            textPomodoro.text = ctx.getString(R.string.pomodoro_count_fmt, task.pomodoroCount)
            textPomodoro.visibility = if (task.pomodoroCount > 0) View.VISIBLE else View.GONE

            // 截止时间
            if (task.dueTime != null) {
                textDue.visibility = View.VISIBLE
                textDue.text = TimeUtils.formatDateTime(task.dueTime)
            } else {
                textDue.visibility = View.GONE
            }

            // 优先级色条
            val colorRes = when (task.priority) {
                Task.PRIORITY_HIGH -> R.color.priority_high
                Task.PRIORITY_LOW -> R.color.priority_low
                else -> R.color.priority_normal
            }
            priorityBar.setBackgroundColor(ContextCompat.getColor(ctx, colorRes))

            // 已完成则隐藏“专注”按钮
            btnFocus.visibility = if (task.isDone) View.GONE else View.VISIBLE

            checkboxDone.setOnCheckedChangeListener { _, _ -> onToggle(task) }
            root.setOnClickListener { onClick(task) }
            btnFocus.setOnClickListener { onStartFocus(task) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
        }
    }
}
