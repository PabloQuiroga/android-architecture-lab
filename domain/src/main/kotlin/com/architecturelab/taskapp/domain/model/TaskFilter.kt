package com.architecturelab.taskapp.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

sealed interface TaskFilter {
    fun matches(
        task: Task,
        now: Instant,
        zoneId: ZoneId,
    ): Boolean

    data object All : TaskFilter {
        override fun matches(
            task: Task,
            now: Instant,
            zoneId: ZoneId,
        ): Boolean = true
    }

    data object Overdue : TaskFilter {
        override fun matches(
            task: Task,
            now: Instant,
            zoneId: ZoneId,
        ): Boolean = task.isOverdue(now)
    }

    data object DueToday : TaskFilter {
        override fun matches(
            task: Task,
            now: Instant,
            zoneId: ZoneId,
        ): Boolean {
            if (task.status != TaskStatus.PENDING || task.dueDate == null) return false
            val dueInstant = task.dueDate.value
            if (dueInstant.isBefore(now) || dueInstant == now) return false

            val today = now.atZone(zoneId).toLocalDate()
            val dueLocalDate = dueInstant.atZone(zoneId).toLocalDate()
            return dueLocalDate == today
        }
    }

    data object Upcoming : TaskFilter {
        override fun matches(
            task: Task,
            now: Instant,
            zoneId: ZoneId,
        ): Boolean {
            if (task.dueDate == null) return false
            val tomorrowStart = now.atZone(zoneId).toLocalDate().plusDays(1).atStartOfDay(zoneId).toInstant()
            return !task.dueDate.value.isBefore(tomorrowStart)
        }
    }

    data object NoDueDate : TaskFilter {
        override fun matches(
            task: Task,
            now: Instant,
            zoneId: ZoneId,
        ): Boolean = task.dueDate == null
    }

    data class SpecificDate(val date: LocalDate) : TaskFilter {
        override fun matches(
            task: Task,
            now: Instant,
            zoneId: ZoneId,
        ): Boolean {
            if (task.dueDate == null) return false
            val dueLocalDate = task.dueDate.value.atZone(zoneId).toLocalDate()
            return dueLocalDate == date
        }
    }

    data class ByPriority(val priority: Priority) : TaskFilter {
        override fun matches(
            task: Task,
            now: Instant,
            zoneId: ZoneId,
        ): Boolean = task.priority == priority
    }

    data class Combined(
        val dueDateFilter: TaskFilter,
        val priorityFilter: TaskFilter,
    ) : TaskFilter {
        override fun matches(
            task: Task,
            now: Instant,
            zoneId: ZoneId,
        ): Boolean = dueDateFilter.matches(task, now, zoneId) && priorityFilter.matches(task, now, zoneId)
    }
}
