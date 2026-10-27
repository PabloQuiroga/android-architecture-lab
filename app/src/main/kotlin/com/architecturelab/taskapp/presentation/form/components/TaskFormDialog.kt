package com.architecturelab.taskapp.presentation.form.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.presentation.form.state.TaskFormUiEvent
import com.architecturelab.taskapp.presentation.form.state.TaskFormUiState

@Composable
fun TaskFormDialog(
    state: TaskFormUiState,
    onEvent: (TaskFormUiEvent) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (state.isEditMode) "Editar Tarea" else "Nueva Tarea",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = state.titleRaw,
                    onValueChange = { onEvent(TaskFormUiEvent.OnTitleChanged(it)) },
                    label = { Text("Título obligatorio") },
                    isError = state.titleError != null,
                    supportingText = {
                        if (state.titleError != null) {
                            Text(
                                text = state.titleError,
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            val trimmed = state.titleRaw.trim()
                            val count = if (trimmed.isEmpty()) 0 else trimmed.codePointCount(0, trimmed.length)
                            Text("$count/36 caracteres")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.descriptionRaw ?: "",
                    onValueChange = { onEvent(TaskFormUiEvent.OnDescriptionChanged(it)) },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Prioridad",
                    style = MaterialTheme.typography.labelLarge,
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = state.priority == priority,
                            onClick = { onEvent(TaskFormUiEvent.OnPriorityChanged(priority)) },
                            label = {
                                Text(
                                    when (priority) {
                                        Priority.LOW -> "Baja"
                                        Priority.MEDIUM -> "Media"
                                        Priority.HIGH -> "Alta"
                                    },
                                )
                            },
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(TaskFormUiEvent.OnSaveClicked) },
                enabled = !state.isSaving,
            ) {
                Text(if (state.isEditMode) "Actualizar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
