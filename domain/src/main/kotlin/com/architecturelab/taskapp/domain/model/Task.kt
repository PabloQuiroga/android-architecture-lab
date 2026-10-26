package com.architecturelab.taskapp.domain.model

import java.time.Instant

data class Task(
    val id: TaskId = TaskId(),
    val title: TaskTitle,
    val description: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: TaskDueDate? = null,
    val status: TaskStatus = TaskStatus.PENDING,
) {
    /**
     * Una tarea está vencida si su estado es PENDING, tiene fecha de vencimiento
     * y su vencimiento es menor o igual al instante actual (now).
     * Las tareas completadas o sin vencimiento NUNCA se consideran vencidas (RF-12, RN-06).
     */
    fun isOverdue(now: Instant): Boolean {
        if (status != TaskStatus.PENDING || dueDate == null) return false
        return !dueDate.value.isAfter(now)
    }
}
