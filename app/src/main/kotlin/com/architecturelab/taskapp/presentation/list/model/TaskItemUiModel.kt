package com.architecturelab.taskapp.presentation.list.model

import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskStatus
import com.architecturelab.taskapp.domain.time.TimeProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TaskItemUiModel(
    val id: String,
    val title: String,
    val description: String?,
    val priority: Priority,
    val dueDateFormatted: String?,
    val isCompleted: Boolean,
    val isOverdue: Boolean,
)

fun Task.toUiModel(timeProvider: TimeProvider): TaskItemUiModel {
    val now = timeProvider.now()
    val zoneId = timeProvider.timeZone()

    val formattedDate = dueDate?.value?.let { formatDueDate24H(it, zoneId) }

    return TaskItemUiModel(
        id = id.value,
        title = title.value,
        description = description,
        priority = priority,
        dueDateFormatted = formattedDate,
        isCompleted = status == TaskStatus.COMPLETED,
        isOverdue = isOverdue(now),
    )
}

private fun formatDueDate24H(
    instant: Instant,
    zoneId: ZoneId,
): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return instant.atZone(zoneId).format(formatter)
}
