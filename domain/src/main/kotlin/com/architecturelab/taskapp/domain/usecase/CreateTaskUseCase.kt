package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskDueDate
import com.architecturelab.taskapp.domain.model.TaskTitle
import com.architecturelab.taskapp.domain.repository.TaskRepository
import com.architecturelab.taskapp.domain.time.TimeProvider
import java.time.Instant

class CreateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        titleRaw: String,
        description: String? = null,
        priority: Priority = Priority.MEDIUM,
        dueDateInstant: Instant? = null,
        isTimeSet: Boolean = true,
    ): Task {
        val title = TaskTitle.create(titleRaw)

        val dueDate =
            dueDateInstant?.let { instant ->
                if (isTimeSet) {
                    TaskDueDate(instant)
                } else {
                    val localDate = instant.atZone(timeProvider.timeZone()).toLocalDate()
                    val midnightInstant = localDate.atStartOfDay(timeProvider.timeZone()).toInstant()
                    TaskDueDate(midnightInstant)
                }
            }

        val task =
            Task(
                title = title,
                description = description?.trim()?.ifEmpty { null },
                priority = priority,
                dueDate = dueDate,
            )

        taskRepository.saveTask(task)
        return task
    }
}
