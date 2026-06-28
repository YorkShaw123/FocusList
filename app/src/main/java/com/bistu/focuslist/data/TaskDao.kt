package com.bistu.focuslist.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * 任务数据访问对象（DAO）。
 * 查询返回 LiveData，实现数据库到界面的自动刷新（响应式）。
 * 写操作为挂起函数，需在协程中调用。
 */
@Dao
interface TaskDao {

    @Query(
        "SELECT * FROM tasks " +
            "ORDER BY isDone ASC, priority DESC, " +
            "CASE WHEN dueTime IS NULL THEN 1 ELSE 0 END ASC, dueTime ASC, createdAt DESC"
    )
    fun observeAll(): LiveData<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY priority DESC, createdAt DESC")
    fun observePending(): LiveData<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 0")
    fun observePendingCount(): LiveData<Int>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    /** 取出所有“未完成且设置了提醒”的任务，用于开机后重排闹钟 */
    @Query("SELECT * FROM tasks WHERE dueTime IS NOT NULL AND isDone = 0")
    suspend fun getTasksWithReminder(): List<Task>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE isDone = 1")
    suspend fun clearCompleted()
}
