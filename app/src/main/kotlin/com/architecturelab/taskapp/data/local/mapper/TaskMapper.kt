package com.architecturelab.taskapp.data.local.mapper

import com.architecturelab.taskapp.data.local.entity.TaskEntity
import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskDueDate
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.model.TaskStatus
import com.architecturelab.taskapp.domain.model.TaskTitle
import java.time.Instant

fun TaskEntity.toDomain(): Task {
    return Task(
        id = TaskId(id),
        title = TaskTitle.create(title),
        description = description,
        priority = Priority.valueOf(priority),
        dueDate = dueDateEpochMillis?.let { TaskDueDate(Instant.ofEpochMilli(it)) },
        status = TaskStatus.valueOf(status),
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id.value,
        title = title.value,
        description = description,
        priority = priority.name,
        dueDateEpochMillis = dueDate?.value?.toEpochMilli(),
        status = status.name,
    )
}
