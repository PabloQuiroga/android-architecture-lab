package com.architecturelab.taskapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class TaskSorterTest {
    private val date1 = TaskDueDate(Instant.parse("2026-10-26T10:00:00Z"))
    private val date2 = TaskDueDate(Instant.parse("2026-10-26T15:00:00Z"))

    @Test
    fun `DueDateAsc puts earlier dates first and no-due-date tasks at the end`() {
        val taskEarly = Task(id = TaskId("1"), title = TaskTitle.create("Temprano"), dueDate = date1)
        val taskLate = Task(id = TaskId("2"), title = TaskTitle.create("Tarde"), dueDate = date2)
        val taskNoDate = Task(id = TaskId("3"), title = TaskTitle.create("Sin fecha"), dueDate = null)

        val sorted = TaskSorter.sort(listOf(taskNoDate, taskLate, taskEarly), TaskSort.DueDateAsc)
        assertEquals(listOf(taskEarly, taskLate, taskNoDate), sorted)
    }

    @Test
    fun `DueDateDesc puts later dates first and no-due-date tasks ALWAYS at the end`() {
        val taskEarly = Task(id = TaskId("1"), title = TaskTitle.create("Temprano"), dueDate = date1)
        val taskLate = Task(id = TaskId("2"), title = TaskTitle.create("Tarde"), dueDate = date2)
        val taskNoDate = Task(id = TaskId("3"), title = TaskTitle.create("Sin fecha"), dueDate = null)

        val sorted = TaskSorter.sort(listOf(taskNoDate, taskEarly, taskLate), TaskSort.DueDateDesc)
        assertEquals(listOf(taskLate, taskEarly, taskNoDate), sorted)
    }

    @Test
    fun `DueDate tie-breaker sorts by priority descending, then title NFC case-insensitive, then ID`() {
        val t1 = Task(id = TaskId("A"), title = TaskTitle.create("Beta"), priority = Priority.LOW, dueDate = date1)
        val t2 = Task(id = TaskId("B"), title = TaskTitle.create("Alfa"), priority = Priority.HIGH, dueDate = date1)
        val t3 = Task(id = TaskId("C"), title = TaskTitle.create("alfa"), priority = Priority.HIGH, dueDate = date1)

        val sorted = TaskSorter.sort(listOf(t1, t2, t3), TaskSort.DueDateAsc)

        assertEquals(listOf(t2, t3, t1), sorted)
    }

    @Test
    fun `PriorityAsc sorts LOW to HIGH and PriorityDesc sorts HIGH to LOW`() {
        val low = Task(id = TaskId("1"), title = TaskTitle.create("Baja"), priority = Priority.LOW)
        val med = Task(id = TaskId("2"), title = TaskTitle.create("Media"), priority = Priority.MEDIUM)
        val high = Task(id = TaskId("3"), title = TaskTitle.create("Alta"), priority = Priority.HIGH)

        val sortedAsc = TaskSorter.sort(listOf(high, low, med), TaskSort.PriorityAsc)
        assertEquals(listOf(low, med, high), sortedAsc)

        val sortedDesc = TaskSorter.sort(listOf(low, high, med), TaskSort.PriorityDesc)
        assertEquals(listOf(high, med, low), sortedDesc)
    }
}
