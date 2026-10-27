package com.architecturelab.taskapp.presentation.form.state

import com.architecturelab.taskapp.domain.model.Priority
import java.time.Instant

sealed interface TaskFormUiEvent {
    data class OnTitleChanged(val title: String) : TaskFormUiEvent

    data class OnDescriptionChanged(val description: String?) : TaskFormUiEvent

    data class OnPriorityChanged(val priority: Priority) : TaskFormUiEvent

    data class OnDueDateChanged(val dueDateInstant: Instant?, val isTimeSet: Boolean) : TaskFormUiEvent

    data class OnLoadTask(val taskId: String) : TaskFormUiEvent

    data object OnSaveClicked : TaskFormUiEvent

    data object OnUserMessageDismissed : TaskFormUiEvent
}
