package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskFilter
import com.architecturelab.taskapp.domain.model.TaskSort
import com.architecturelab.taskapp.domain.model.TaskSorter
import com.architecturelab.taskapp.domain.repository.TaskRepository
import com.architecturelab.taskapp.domain.time.TimeProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTasksUseCase(
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
) {
    operator fun invoke(
        filter: TaskFilter = TaskFilter.All,
        sort: TaskSort = TaskSort.DueDateAsc,
    ): Flow<List<Task>> {
        return taskRepository.getTasks().map { tasks ->
            val now = timeProvider.now()
            val zoneId = timeProvider.timeZone()
            val filtered = tasks.filter { filter.matches(it, now, zoneId) }
            TaskSorter.sort(filtered, sort)
        }
    }
}
