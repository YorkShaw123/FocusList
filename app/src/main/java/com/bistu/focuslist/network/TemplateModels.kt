package com.bistu.focuslist.network

/** 在线学习模板接口返回结构。 */
data class TemplateResponse(
    val templates: List<StudyTemplate> = emptyList()
)

data class StudyTemplate(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val estimatedDays: Int = 0,
    val source: String = "",
    val tasks: List<TemplateTask> = emptyList()
)

data class TemplateTask(
    val title: String = "",
    val notes: String = "",
    val category: String = "",
    val priority: Int = 1
)
