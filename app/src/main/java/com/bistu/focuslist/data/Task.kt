package com.bistu.focuslist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 任务实体。对应数据库表 tasks。
 *
 * 这是“数据持久化”的核心数据模型，同时也是 ContentProvider 对外暴露的数据结构。
 */
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 任务标题 */
    val title: String,

    /** 备注说明 */
    val notes: String = "",

    /** 分类标签，如“学习”“工作”“生活” */
    val category: String = "默认",

    /** 优先级：0=低，1=中，2=高 */
    val priority: Int = PRIORITY_NORMAL,

    /** 是否已完成 */
    val isDone: Boolean = false,

    /** 截止/提醒时间戳（毫秒）。为空表示不设置提醒 */
    val dueTime: Long? = null,

    /** 创建时间戳（毫秒） */
    val createdAt: Long = System.currentTimeMillis(),

    /** 该任务累计完成的番茄钟个数 */
    val pomodoroCount: Int = 0
) {
    companion object {
        const val PRIORITY_LOW = 0
        const val PRIORITY_NORMAL = 1
        const val PRIORITY_HIGH = 2
    }
}
