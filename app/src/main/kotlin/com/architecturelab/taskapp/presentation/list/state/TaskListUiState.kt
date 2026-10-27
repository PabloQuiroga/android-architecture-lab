package com.architecturelab.taskapp.presentation.list.state

import com.architecturelab.taskapp.domain.model.TaskFilter
import com.architecturelab.taskapp.domain.model.TaskSort
import com.architecturelab.taskapp.presentation.list.model.TaskItemUiModel

data class TaskListUiState(
    val tasks: List<TaskItemUiModel> = emptyList(),
    val activeFilter: TaskFilter = TaskFilter.All,
    val activeSort: TaskSort = TaskSort.DueDateAsc,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
)
