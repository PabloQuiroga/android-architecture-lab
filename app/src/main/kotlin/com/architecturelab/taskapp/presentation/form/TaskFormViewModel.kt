package com.architecturelab.taskapp.presentation.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.architecturelab.taskapp.domain.exception.TaskDomainException
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.model.TaskTitle
import com.architecturelab.taskapp.domain.repository.TaskRepository
import com.architecturelab.taskapp.domain.time.TimeProvider
import com.architecturelab.taskapp.domain.usecase.CreateTaskUseCase
import com.architecturelab.taskapp.domain.usecase.UpdateTaskUseCase
import com.architecturelab.taskapp.presentation.form.state.TaskFormUiEvent
import com.architecturelab.taskapp.presentation.form.state.TaskFormUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskFormViewModel(
    private val createTaskUseCase: CreateTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val taskRepository: TaskRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    fun onEvent(event: TaskFormUiEvent) {
        when (event) {
            is TaskFormUiEvent.OnTitleChanged -> {
                val error = validateLiveTitle(event.title)
                _uiState.update {
                    it.copy(
                        titleRaw = event.title,
                        titleError = error,
                    )
                }
            }
            is TaskFormUiEvent.OnDescriptionChanged -> {
                _uiState.update { it.copy(descriptionRaw = event.description) }
            }
            is TaskFormUiEvent.OnPriorityChanged -> {
                _uiState.update { it.copy(priority = event.priority) }
            }
            is TaskFormUiEvent.OnDueDateChanged -> {
                _uiState.update {
                    it.copy(
                        dueDateInstant = event.dueDateInstant,
                        isTimeSet = event.isTimeSet,
                    )
                }
            }
            is TaskFormUiEvent.OnLoadTask -> {
                loadTaskForEditing(event.taskId)
            }
            TaskFormUiEvent.OnSaveClicked -> {
                saveTask()
            }
            TaskFormUiEvent.OnUserMessageDismissed -> {
                _uiState.update { it.copy(userMessage = null) }
            }
        }
    }

    private fun validateLiveTitle(rawTitle: String): String? {
        val trimmed = rawTitle.trim()
        if (trimmed.isEmpty()) {
            return "El título no puede estar vacío."
        }
        val codePoints = trimmed.codePointCount(0, trimmed.length)
        if (codePoints > TaskTitle.MAX_CODE_POINTS) {
            return "El título no puede superar los 36 puntos de código Unicode."
        }
        return null
    }

    private fun loadTaskForEditing(taskId: String) {
        viewModelScope.launch {
            val task = taskRepository.getTaskById(TaskId(taskId))
            if (task != null) {
                _uiState.update {
                    it.copy(
                        taskId = task.id.value,
                        titleRaw = task.title.value,
                        descriptionRaw = task.description,
                        priority = task.priority,
                        dueDateInstant = task.dueDate?.value,
                        isTimeSet = task.dueDate != null,
                        titleError = null,
                    )
                }
            } else {
                _uiState.update { it.copy(userMessage = "No se encontró la tarea a editar.") }
            }
        }
    }

    private fun saveTask() {
        val currentState = _uiState.value
        val titleError = validateLiveTitle(currentState.titleRaw)
        if (titleError != null) {
            _uiState.update { it.copy(titleError = titleError) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                if (currentState.isEditMode) {
                    updateTaskUseCase(
                        id = TaskId(currentState.taskId!!),
                        titleRaw = currentState.titleRaw,
                        description = currentState.descriptionRaw,
                        priority = currentState.priority,
                        dueDateInstant = currentState.dueDateInstant,
                        isTimeSet = currentState.isTimeSet,
                    )
                } else {
                    createTaskUseCase(
                        titleRaw = currentState.titleRaw,
                        description = currentState.descriptionRaw,
                        priority = currentState.priority,
                        dueDateInstant = currentState.dueDateInstant,
                        isTimeSet = currentState.isTimeSet,
                    )
                }
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: TaskDomainException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = e.message,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        userMessage = "Ocurrió un error al guardar la tarea.",
                    )
                }
            }
        }
    }
}
