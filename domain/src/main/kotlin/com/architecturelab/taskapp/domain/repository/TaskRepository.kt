package com.architecturelab.taskapp.domain.repository

import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskId
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>

    suspend fun getTaskById(id: TaskId): Task?

    suspend fun saveTask(task: Task)

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(id: TaskId)
}
