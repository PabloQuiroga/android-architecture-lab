package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.model.TaskDueDate
import com.architecturelab.taskapp.domain.model.TaskStatus
import com.architecturelab.taskapp.domain.repository.FakeTaskRepository
import com.architecturelab.taskapp.domain.time.FixedTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class ToggleTaskStatusUseCaseTest {
    private val now = Instant.parse("2026-10-26T12:00:00Z")
    private val timeProvider = FixedTimeProvider(now, ZoneId.of("UTC"))
    private val repository = FakeTaskRepository()
    private val createUseCase = CreateTaskUseCase(repository, timeProvider)
    private val useCase = ToggleTaskStatusUseCase(repository, timeProvider)

    @Test
    fun `completing pending task changes status to COMPLETED and keeps due date`() =
        runTest {
            val task = createUseCase("Tarea", dueDateInstant = now.plusSeconds(3600))

            val completed = useCase(task.id)

            assertEquals(TaskStatus.COMPLETED, completed.status)
            assertNotNull(completed.dueDate)
        }

    @Test
    fun `reopening completed task whose due date has passed removes due date`() =
        runTest {
            val pastDueDate = Instant.parse("2026-10-26T10:00:00Z")
            val task = createUseCase("Tarea pasada", dueDateInstant = pastDueDate)

            val completed = useCase(task.id)
            assertEquals(TaskStatus.COMPLETED, completed.status)

            val reopened = useCase(completed.id)
            assertEquals(TaskStatus.PENDING, reopened.status)
            assertNull(reopened.dueDate)
        }

    @Test
    fun `reopening completed task whose due date is in the future retains due date`() =
        runTest {
            val futureDueDate = Instant.parse("2026-10-26T15:00:00Z")
            val task = createUseCase("Tarea futura", dueDateInstant = futureDueDate)

            val completed = useCase(task.id)
            val reopened = useCase(completed.id)

            assertEquals(TaskStatus.PENDING, reopened.status)
            assertEquals(TaskDueDate(futureDueDate), reopened.dueDate)
        }
}
