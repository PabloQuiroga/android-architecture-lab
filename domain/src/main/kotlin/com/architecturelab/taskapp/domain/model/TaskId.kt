package com.architecturelab.taskapp.domain.model

import java.util.UUID

data class TaskId(val value: String = UUID.randomUUID().toString()) {
    init {
        require(value.isNotBlank()) { "El identificador de la tarea no puede estar vacío." }
    }
}
