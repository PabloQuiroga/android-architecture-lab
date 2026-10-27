package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.exception.TaskDomainException
import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskDueDate
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.model.TaskTitle
import com.architecturelab.taskapp.domain.repository.TaskRepository
import com.architecturelab.taskapp.domain.time.TimeProvider
import java.time.Instant

class UpdateTaskUseCase(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(
        id: TaskId,
        titleRaw: String,
        description: String? = null,
        priority: Priority = Priority.MEDIUM,
        dueDateInstant: Instant? = null,
        isTimeSet: Boolean = true,
    ): Task {
        val existingTask =
            taskRepository.getTaskById(id)
                ?: throw TaskDomainException.TaskNotFound

        val newTitle = TaskTitle.create(titleRaw)

        val newDueDate =
            dueDateInstant?.let { instant ->
                if (isTimeSet) {
                    TaskDueDate(instant)
                } else {
                    val localDate = instant.atZone(timeProvider.timeZone()).toLocalDate()
                    val midnightInstant = localDate.atStartOfDay(timeProvider.timeZone()).toInstant()
                    TaskDueDate(midnightInstant)
                }
            }

        val updatedTask =
            existingTask.copy(
                title = newTitle,
                description = description?.trim()?.ifEmpty { null },
                priority = priority,
                dueDate = newDueDate,
            )

        taskRepository.updateTask(updatedTask)
        return updatedTask
    }
}
