package com.architecturelab.taskapp.data.local.source

import com.architecturelab.taskapp.data.local.dao.TaskDao
import com.architecturelab.taskapp.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskLocalDataSource {
    fun getTasks(): Flow<List<TaskEntity>>

    suspend fun getTaskById(id: String): TaskEntity?

    suspend fun saveTask(task: TaskEntity)

    suspend fun updateTask(task: TaskEntity)

    suspend fun deleteTask(id: String)
}

class TaskLocalDataSourceImpl(
    private val taskDao: TaskDao,
) : TaskLocalDataSource {
    override fun getTasks(): Flow<List<TaskEntity>> = taskDao.getTasks()

    override suspend fun getTaskById(id: String): TaskEntity? = taskDao.getTaskById(id)

    override suspend fun saveTask(task: TaskEntity) = taskDao.insertTask(task)

    override suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    override suspend fun deleteTask(id: String) = taskDao.deleteTaskById(id)
}
