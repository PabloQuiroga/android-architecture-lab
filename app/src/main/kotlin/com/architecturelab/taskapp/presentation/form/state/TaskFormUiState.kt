package com.architecturelab.taskapp.presentation.form.state

import com.architecturelab.taskapp.domain.model.Priority
import java.time.Instant

data class TaskFormUiState(
    val taskId: String? = null,
    val titleRaw: String = "",
    val descriptionRaw: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val dueDateInstant: Instant? = null,
    val isTimeSet: Boolean = false,
    val titleError: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val userMessage: String? = null,
) {
    val isEditMode: Boolean get() = taskId != null
}
