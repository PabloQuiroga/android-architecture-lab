package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.repository.TaskRepository

class DeleteTaskUseCase(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(id: TaskId) {
        taskRepository.deleteTask(id)
    }
}
