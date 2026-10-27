package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.exception.TaskDomainException
import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.model.TaskStatus
import com.architecturelab.taskapp.domain.repository.TaskRepository
import com.architecturelab.taskapp.domain.time.TimeProvider

class ToggleTaskStatusUseCase(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(id: TaskId): Task {
        val existingTask =
            taskRepository.getTaskById(id)
                ?: throw TaskDomainException.TaskNotFound

        val updatedTask =
            when (existingTask.status) {
                TaskStatus.PENDING -> {
                    existingTask.copy(status = TaskStatus.COMPLETED)
                }
                TaskStatus.COMPLETED -> {
                    val now = timeProvider.now()
                    val shouldRemoveDueDate = existingTask.dueDate != null && !existingTask.dueDate.value.isAfter(now)
                    existingTask.copy(
                        status = TaskStatus.PENDING,
                        dueDate = if (shouldRemoveDueDate) null else existingTask.dueDate,
                    )
                }
            }

        taskRepository.updateTask(updatedTask)
        return updatedTask
    }
}
