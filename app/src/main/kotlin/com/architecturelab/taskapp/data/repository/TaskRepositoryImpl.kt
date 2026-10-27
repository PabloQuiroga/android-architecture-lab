package com.architecturelab.taskapp.data.repository

import com.architecturelab.taskapp.data.local.mapper.toDomain
import com.architecturelab.taskapp.data.local.mapper.toEntity
import com.architecturelab.taskapp.data.local.source.TaskLocalDataSource
import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val localDataSource: TaskLocalDataSource,
) : TaskRepository {
    override fun getTasks(): Flow<List<Task>> {
        return localDataSource.getTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(id: TaskId): Task? {
        return localDataSource.getTaskById(id.value)?.toDomain()
    }

    override suspend fun saveTask(task: Task) {
        localDataSource.saveTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        localDataSource.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(id: TaskId) {
        localDataSource.deleteTask(id.value)
    }
}
