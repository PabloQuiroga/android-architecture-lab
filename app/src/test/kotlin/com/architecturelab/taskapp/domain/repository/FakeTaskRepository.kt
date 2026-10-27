package com.architecturelab.taskapp.domain.repository

import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTaskRepository : TaskRepository {
    private val tasksMap = MutableStateFlow<Map<TaskId, Task>>(emptyMap())

    override fun getTasks(): Flow<List<Task>> {
        return tasksMap.map { it.values.toList() }
    }

    override suspend fun getTaskById(id: TaskId): Task? {
        return tasksMap.value[id]
    }

    override suspend fun saveTask(task: Task) {
        tasksMap.value = tasksMap.value + (task.id to task)
    }

    override suspend fun updateTask(task: Task) {
        tasksMap.value = tasksMap.value + (task.id to task)
    }

    override suspend fun deleteTask(id: TaskId) {
        tasksMap.value = tasksMap.value - id
    }
}
