package com.architecturelab.taskapp.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TaskFilterTest {
    private val zoneId = ZoneId.of("UTC")
    private val now = Instant.parse("2026-10-26T12:00:00Z")

    @Test
    fun `All filter matches any task`() {
        val task = Task(title = TaskTitle.create("Cualquier tarea"))
        assertTrue(TaskFilter.All.matches(task, now, zoneId))
    }

    @Test
    fun `Overdue filter matches only overdue tasks`() {
        val overdueTask =
            Task(
                title = TaskTitle.create("Vencida"),
                dueDate = TaskDueDate(Instant.parse("2026-10-26T11:00:00Z")),
                status = TaskStatus.PENDING,
            )
        val futureTask =
            Task(
                title = TaskTitle.create("Futura"),
                dueDate = TaskDueDate(Instant.parse("2026-10-26T15:00:00Z")),
                status = TaskStatus.PENDING,
            )

        assertTrue(TaskFilter.Overdue.matches(overdueTask, now, zoneId))
        assertFalse(TaskFilter.Overdue.matches(futureTask, now, zoneId))
    }

    @Test
    fun `DueToday filter matches pending tasks with due date later today`() {
        val dueTodayTask =
            Task(
                title = TaskTitle.create("Hoy tarde"),
                dueDate = TaskDueDate(Instant.parse("2026-10-26T18:00:00Z")),
                status = TaskStatus.PENDING,
            )
        val overdueTask =
            Task(
                title = TaskTitle.create("Ya venció"),
                dueDate = TaskDueDate(Instant.parse("2026-10-26T10:00:00Z")),
                status = TaskStatus.PENDING,
            )
        val tomorrowTask =
            Task(
                title = TaskTitle.create("Mañana"),
                dueDate = TaskDueDate(Instant.parse("2026-10-27T10:00:00Z")),
                status = TaskStatus.PENDING,
            )

        assertTrue(TaskFilter.DueToday.matches(dueTodayTask, now, zoneId))
        assertFalse(TaskFilter.DueToday.matches(overdueTask, now, zoneId))
        assertFalse(TaskFilter.DueToday.matches(tomorrowTask, now, zoneId))
    }

    @Test
    fun `Upcoming filter matches tasks starting from tomorrow`() {
        val tomorrowTask =
            Task(
                title = TaskTitle.create("Mañana"),
                dueDate = TaskDueDate(Instant.parse("2026-10-27T00:00:00Z")),
                status = TaskStatus.PENDING,
            )
        val todayTask =
            Task(
                title = TaskTitle.create("Hoy"),
                dueDate = TaskDueDate(Instant.parse("2026-10-26T23:59:59Z")),
                status = TaskStatus.PENDING,
            )

        assertTrue(TaskFilter.Upcoming.matches(tomorrowTask, now, zoneId))
        assertFalse(TaskFilter.Upcoming.matches(todayTask, now, zoneId))
    }

    @Test
    fun `NoDueDate filter matches tasks with no due date`() {
        val noDueTask = Task(title = TaskTitle.create("Sin fecha"))
        val dueTask = Task(title = TaskTitle.create("Con fecha"), dueDate = TaskDueDate(now))

        assertTrue(TaskFilter.NoDueDate.matches(noDueTask, now, zoneId))
        assertFalse(TaskFilter.NoDueDate.matches(dueTask, now, zoneId))
    }

    @Test
    fun `SpecificDate filter matches tasks matching local date`() {
        val date = LocalDate.parse("2026-10-28")
        val taskOnDate =
            Task(
                title = TaskTitle.create("En fecha especifica"),
                dueDate = TaskDueDate(Instant.parse("2026-10-28T15:00:00Z")),
            )
        val taskOtherDate =
            Task(
                title = TaskTitle.create("Otra fecha"),
                dueDate = TaskDueDate(Instant.parse("2026-10-29T15:00:00Z")),
            )

        val filter = TaskFilter.SpecificDate(date)
        assertTrue(filter.matches(taskOnDate, now, zoneId))
        assertFalse(filter.matches(taskOtherDate, now, zoneId))
    }

    @Test
    fun `ByPriority filter matches tasks matching target priority`() {
        val highTask = Task(title = TaskTitle.create("Alta"), priority = Priority.HIGH)
        val lowTask = Task(title = TaskTitle.create("Baja"), priority = Priority.LOW)

        val filter = TaskFilter.ByPriority(Priority.HIGH)
        assertTrue(filter.matches(highTask, now, zoneId))
        assertFalse(filter.matches(lowTask, now, zoneId))
    }

    @Test
    fun `Combined filter requires satisfying both filters`() {
        val highAndOverdueTask =
            Task(
                title = TaskTitle.create("Alta y Vencida"),
                priority = Priority.HIGH,
                dueDate = TaskDueDate(Instant.parse("2026-10-26T10:00:00Z")),
                status = TaskStatus.PENDING,
            )
        val lowAndOverdueTask =
            Task(
                title = TaskTitle.create("Baja y Vencida"),
                priority = Priority.LOW,
                dueDate = TaskDueDate(Instant.parse("2026-10-26T10:00:00Z")),
                status = TaskStatus.PENDING,
            )

        val combined =
            TaskFilter.Combined(
                dueDateFilter = TaskFilter.Overdue,
                priorityFilter = TaskFilter.ByPriority(Priority.HIGH),
            )

        assertTrue(combined.matches(highAndOverdueTask, now, zoneId))
        assertFalse(combined.matches(lowAndOverdueTask, now, zoneId))
    }
}
