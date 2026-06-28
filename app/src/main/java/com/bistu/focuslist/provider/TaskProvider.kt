package com.bistu.focuslist.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.bistu.focuslist.data.AppDatabase

/**
 * 内容提供器（四大组件之一）。
 *
 * 对外（含跨进程，如桌面小组件进程）以标准 content:// Uri 暴露任务数据，
 * 底层复用 Room 创建的同一个 SQLite 数据库，避免数据不一致。
 *
 * 支持的 Uri：
 *   content://com.bistu.focuslist.provider/tasks       —— 全部任务（dir）
 *   content://com.bistu.focuslist.provider/tasks/{id}  —— 单条任务（item）
 */
class TaskProvider : ContentProvider() {

    private lateinit var database: AppDatabase

    override fun onCreate(): Boolean {
        database = AppDatabase.getInstance(context!!)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val readable = database.openHelper.readableDatabase
        val cursor: Cursor = when (matcher.match(uri)) {
            CODE_TASKS -> {
                val sql = buildString {
                    append("SELECT * FROM ").append(TABLE)
                    if (!selection.isNullOrEmpty()) append(" WHERE ").append(selection)
                    append(" ORDER BY ").append(sortOrder ?: "isDone ASC, priority DESC, createdAt DESC")
                }
                readable.query(SimpleSQLiteQuery(sql, selectionArgs))
            }
            CODE_TASK_ID -> {
                val id = ContentUris.parseId(uri)
                readable.query(
                    SimpleSQLiteQuery("SELECT * FROM $TABLE WHERE id = ?", arrayOf<Any?>(id))
                )
            }
            else -> throw IllegalArgumentException("未知的 Uri: $uri")
        }
        // 注册数据变化通知，使观察者（如小组件）可感知刷新
        cursor.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (matcher.match(uri) != CODE_TASKS) {
            throw IllegalArgumentException("不支持向该 Uri 插入: $uri")
        }
        val cv = ContentValues(values ?: ContentValues())
        // 为未提供的非空列补默认值，保证 NOT NULL 约束
        if (!cv.containsKey("notes")) cv.put("notes", "")
        if (!cv.containsKey("category")) cv.put("category", "默认")
        if (!cv.containsKey("priority")) cv.put("priority", 1)
        if (!cv.containsKey("isDone")) cv.put("isDone", 0)
        if (!cv.containsKey("createdAt")) cv.put("createdAt", System.currentTimeMillis())
        if (!cv.containsKey("pomodoroCount")) cv.put("pomodoroCount", 0)

        val writable = database.openHelper.writableDatabase
        val id = writable.insert(TABLE, SQLiteDatabase.CONFLICT_REPLACE, cv)
        context!!.contentResolver.notifyChange(uri, null)
        return ContentUris.withAppendedId(CONTENT_URI, id)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val cv = values ?: return 0
        val writable = database.openHelper.writableDatabase
        val count = when (matcher.match(uri)) {
            CODE_TASKS ->
                writable.update(TABLE, SQLiteDatabase.CONFLICT_REPLACE, cv, selection, selectionArgs)
            CODE_TASK_ID -> {
                val id = ContentUris.parseId(uri)
                writable.update(TABLE, SQLiteDatabase.CONFLICT_REPLACE, cv, "id = ?", arrayOf<Any?>(id))
            }
            else -> throw IllegalArgumentException("未知的 Uri: $uri")
        }
        if (count > 0) context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val writable = database.openHelper.writableDatabase
        val count = when (matcher.match(uri)) {
            CODE_TASKS -> writable.delete(TABLE, selection, selectionArgs)
            CODE_TASK_ID -> {
                val id = ContentUris.parseId(uri)
                writable.delete(TABLE, "id = ?", arrayOf<Any?>(id))
            }
            else -> throw IllegalArgumentException("未知的 Uri: $uri")
        }
        if (count > 0) context!!.contentResolver.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String? = when (matcher.match(uri)) {
        CODE_TASKS -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$TABLE"
        CODE_TASK_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.$TABLE"
        else -> null
    }

    companion object {
        const val AUTHORITY = "com.bistu.focuslist.provider"
        const val TABLE = "tasks"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$TABLE")

        private const val CODE_TASKS = 1
        private const val CODE_TASK_ID = 2

        private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, TABLE, CODE_TASKS)
            addURI(AUTHORITY, "$TABLE/#", CODE_TASK_ID)
        }
    }
}
