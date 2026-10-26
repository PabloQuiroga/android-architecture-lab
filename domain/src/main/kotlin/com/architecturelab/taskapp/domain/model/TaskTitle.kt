package com.architecturelab.taskapp.domain.model

import com.architecturelab.taskapp.domain.exception.TaskDomainException

@ConsistentCopyVisibility
data class TaskTitle private constructor(val value: String) {
    companion object {
        const val MAX_CODE_POINTS = 36

        fun create(rawTitle: String): TaskTitle {
            val trimmed = rawTitle.trim()
            if (trimmed.isEmpty()) {
                throw TaskDomainException.InvalidTitle.EmptyTitle
            }

            val codePointCount = trimmed.codePointCount(0, trimmed.length)
            if (codePointCount > MAX_CODE_POINTS) {
                throw TaskDomainException.InvalidTitle.ExceedsMaxLength(
                    actualCodePoints = codePointCount,
                    maxCodePoints = MAX_CODE_POINTS,
                )
            }

            return TaskTitle(trimmed)
        }
    }
}
