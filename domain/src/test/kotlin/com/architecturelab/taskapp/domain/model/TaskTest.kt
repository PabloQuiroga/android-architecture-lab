package com.architecturelab.taskapp.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class TaskTest {
    private val now = Instant.parse("2026-10-26T12:00:00Z")
    private val pastDueDate = TaskDueDate(Instant.parse("2026-10-26T11:59:59Z"))
    private val exactDueDate = TaskDueDate(now)
    private val futureDueDate = TaskDueDate(Instant.parse("2026-10-26T12:00:01Z"))

    @Test
    fun `pending task with past due date is overdue`() {
        val task =
            Task(
                title = TaskTitle.create("Tarea vencida"),
                dueDate = pastDueDate,
                status = TaskStatus.PENDING,
            )
        assertTrue(task.isOverdue(now))
    }

    @Test
    fun `pending task with exact due date matching now is overdue`() {
        val task =
            Task(
                title = TaskTitle.create("Tarea en vencimiento exacto"),
                dueDate = exactDueDate,
                status = TaskStatus.PENDING,
            )
        assertTrue(task.isOverdue(now))
    }

    @Test
    fun `pending task with future due date is not overdue`() {
        val task =
            Task(
                title = TaskTitle.create("Tarea futura"),
                dueDate = futureDueDate,
                status = TaskStatus.PENDING,
            )
        assertFalse(task.isOverdue(now))
    }

    @Test
    fun `completed task with past due date is never overdue`() {
        val task =
            Task(
                title = TaskTitle.create("Tarea completada"),
                dueDate = pastDueDate,
                status = TaskStatus.COMPLETED,
            )
        assertFalse(task.isOverdue(now))
    }

    @Test
    fun `task with no due date is never overdue`() {
        val task =
            Task(
                title = TaskTitle.create("Tarea sin vencimiento"),
                dueDate = null,
                status = TaskStatus.PENDING,
            )
        assertFalse(task.isOverdue(now))
    }
}
