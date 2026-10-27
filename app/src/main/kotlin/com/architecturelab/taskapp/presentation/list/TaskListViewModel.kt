package com.architecturelab.taskapp.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.architecturelab.taskapp.domain.model.TaskFilter
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.model.TaskSort
import com.architecturelab.taskapp.domain.time.TimeProvider
import com.architecturelab.taskapp.domain.usecase.DeleteTaskUseCase
import com.architecturelab.taskapp.domain.usecase.GetTasksUseCase
import com.architecturelab.taskapp.domain.usecase.ToggleTaskStatusUseCase
import com.architecturelab.taskapp.presentation.list.model.toUiModel
import com.architecturelab.taskapp.presentation.list.state.TaskListUiEvent
import com.architecturelab.taskapp.presentation.list.state.TaskListUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val toggleTaskStatusUseCase: ToggleTaskStatusUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    private val filterFlow = MutableStateFlow<TaskFilter>(TaskFilter.All)
    private val sortFlow = MutableStateFlow<TaskSort>(TaskSort.DueDateAsc)
    private val isLoadingFlow = MutableStateFlow(false)
    private val userMessageFlow = MutableStateFlow<String?>(null)

    private val tasksFlow =
        combine(filterFlow, sortFlow) { filter, sort ->
            filter to sort
        }.flatMapLatest { (filter, sort) ->
            getTasksUseCase(filter, sort)
        }

    val uiState: StateFlow<TaskListUiState> =
        combine(
            tasksFlow,
            filterFlow,
            sortFlow,
            isLoadingFlow,
            userMessageFlow,
        ) { tasks, activeFilter, activeSort, isLoading, userMessage ->
            TaskListUiState(
                tasks = tasks.map { it.toUiModel(timeProvider) },
                activeFilter = activeFilter,
                activeSort = activeSort,
                isLoading = isLoading,
                userMessage = userMessage,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TaskListUiState(isLoading = true),
        )

    fun onEvent(event: TaskListUiEvent) {
        when (event) {
            is TaskListUiEvent.OnFilterSelected -> {
                filterFlow.value = event.filter
            }
            is TaskListUiEvent.OnSortSelected -> {
                sortFlow.value = event.sort
            }
            is TaskListUiEvent.OnToggleTaskStatus -> {
                viewModelScope.launch {
                    try {
                        toggleTaskStatusUseCase(TaskId(event.taskId))
                    } catch (e: Exception) {
                        userMessageFlow.value = e.message ?: "Error al actualizar la tarea."
                    }
                }
            }
            is TaskListUiEvent.OnDeleteTask -> {
                viewModelScope.launch {
                    try {
                        deleteTaskUseCase(TaskId(event.taskId))
                    } catch (e: Exception) {
                        userMessageFlow.value = e.message ?: "Error al eliminar la tarea."
                    }
                }
            }
            TaskListUiEvent.OnUserMessageDismissed -> {
                userMessageFlow.value = null
            }
        }
    }
}
