package com.bistu.focuslist.ui.tasks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bistu.focuslist.data.Repository
import com.bistu.focuslist.data.Task
import com.bistu.focuslist.util.AlarmScheduler
import com.bistu.focuslist.util.TaskRepeatUtils
import com.bistu.focuslist.util.TimeUtils
import com.bistu.focuslist.widget.TaskWidgetProvider
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * 任务列表 ViewModel。
 * 负责任务的增删改查、搜索筛选，并在数据变化后同步刷新闹钟与桌面小组件。
 */
class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = Repository.get(app)

    private val searchQuery = MutableLiveData("")
    private val filterCategory = MutableLiveData("")
    private val filterPriority = MutableLiveData(-1)
    private val filterDueMode = MutableLiveData(DUE_FILTER_ALL)
    private val filteredTasks = MediatorLiveData<List<Task>>()
    private var currentTaskSource: LiveData<List<Task>>? = null

    val tasks: LiveData<List<Task>> = filteredTasks

    init {
        filteredTasks.addSource(searchQuery) { refreshTaskSource() }
        filteredTasks.addSource(filterCategory) { refreshTaskSource() }
        filteredTasks.addSource(filterPriority) { refreshTaskSource() }
        filteredTasks.addSource(filterDueMode) { refreshTaskSource() }
        refreshTaskSource()
    }

    private fun refreshTaskSource() {
        currentTaskSource?.let { filteredTasks.removeSource(it) }
        val dueMode = filterDueMode.value ?: DUE_FILTER_ALL
        val todayStart = TimeUtils.startOfToday()
        val tomorrowStart = Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        val source = repo.observeFilteredTasks(
            query = searchQuery.value.orEmpty(),
            category = filterCategory.value.orEmpty(),
            priority = filterPriority.value ?: -1,
            dueOnly = dueMode == DUE_FILTER_HAS_DUE,
            todayOnly = dueMode == DUE_FILTER_TODAY,
            overdueOnly = dueMode == DUE_FILTER_OVERDUE,
            todayStart = todayStart,
            tomorrowStart = tomorrowStart,
            now = System.currentTimeMillis()
        )
        filteredTasks.addSource(source) { filteredTasks.value = it }
        currentTaskSource = source
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query.trim()
    }

    fun setFilterCategory(category: String) {
        filterCategory.value = category
    }

    fun setFilterPriority(priority: Int) {
        filterPriority.value = priority
    }

    fun setFilterDueMode(mode: Int) {
        filterDueMode.value = mode
    }

    fun clearFilterOptions() {
        filterCategory.value = ""
        filterPriority.value = -1
        filterDueMode.value = DUE_FILTER_ALL
    }

    /** 勾选 / 取消勾选完成状态 */
    fun toggleDone(task: Task) {
        viewModelScope.launch {
            val updated = if (!task.isDone) {
                TaskRepeatUtils.nextOccurrence(task) ?: task.copy(isDone = true)
            } else {
                task.copy(isDone = false)
            }
            repo.updateTask(updated)
            val ctx = getApplication<Application>()
            if (updated.isDone) {
                AlarmScheduler.cancel(ctx, updated.id)
            } else {
                AlarmScheduler.schedule(ctx, updated)
            }
            TaskWidgetProvider.notifyRefresh(ctx)
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            repo.deleteTask(task)
            val ctx = getApplication<Application>()
            AlarmScheduler.cancel(ctx, task.id)
            TaskWidgetProvider.notifyRefresh(ctx)
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            repo.clearCompletedTasks()
            TaskWidgetProvider.notifyRefresh(getApplication())
        }
    }

    /** 撤销删除时重新插入 */
    fun insert(task: Task) {
        viewModelScope.launch {
            val newId = repo.insertTask(task.copy(id = 0))
            val ctx = getApplication<Application>()
            AlarmScheduler.schedule(ctx, task.copy(id = newId))
            TaskWidgetProvider.notifyRefresh(ctx)
        }
    }

    companion object {
        const val DUE_FILTER_ALL = 0
        const val DUE_FILTER_HAS_DUE = 1
        const val DUE_FILTER_TODAY = 2
        const val DUE_FILTER_OVERDUE = 3
    }
}
