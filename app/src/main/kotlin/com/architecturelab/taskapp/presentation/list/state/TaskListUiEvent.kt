package com.architecturelab.taskapp.presentation.list.state

import com.architecturelab.taskapp.domain.model.TaskFilter
import com.architecturelab.taskapp.domain.model.TaskSort

sealed interface TaskListUiEvent {
    data class OnFilterSelected(val filter: TaskFilter) : TaskListUiEvent

    data class OnSortSelected(val sort: TaskSort) : TaskListUiEvent

    data class OnToggleTaskStatus(val taskId: String) : TaskListUiEvent

    data class OnDeleteTask(val taskId: String) : TaskListUiEvent

    data object OnUserMessageDismissed : TaskListUiEvent
}
