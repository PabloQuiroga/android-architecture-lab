package com.architecturelab.taskapp.domain.exception

sealed class TaskDomainException(message: String) : Exception(message) {
    sealed class InvalidTitle(message: String) : TaskDomainException(message) {
        object EmptyTitle : InvalidTitle("El título de la tarea no puede estar vacío o contener solo espacios.")

        data class ExceedsMaxLength(val actualCodePoints: Int, val maxCodePoints: Int = 36) :
            InvalidTitle("El título no puede superar los $maxCodePoints puntos de código Unicode (actual: $actualCodePoints).")
    }

    object TaskNotFound : TaskDomainException("La tarea solicitada no existe.")
}
