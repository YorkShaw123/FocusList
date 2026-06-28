package com.bistu.focuslist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 番茄钟专注记录。对应数据库表 focus_sessions。
 * 用于统计页展示每日 / 累计专注时长。
 */
@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 关联的任务 id，可为空（自由专注） */
    val taskId: Long? = null,

    /** 关联任务标题快照（便于历史展示） */
    val taskTitle: String = "",

    /** 本次专注时长（分钟） */
    val durationMinutes: Int,

    /** 开始时间戳（毫秒） */
    val startTime: Long,

    /** 结束时间戳（毫秒） */
    val endTime: Long,

    /** 是否完整完成（true=自然结束，false=中途放弃） */
    val completed: Boolean = true
)
