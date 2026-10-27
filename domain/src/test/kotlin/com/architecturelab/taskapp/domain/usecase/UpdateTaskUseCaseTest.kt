package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.exception.TaskDomainException
import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.repository.FakeTaskRepository
import com.architecturelab.taskapp.domain.time.FixedTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class UpdateTaskUseCaseTest {
    private val fixedInstant = Instant.parse("2026-10-26T12:00:00Z")
    private val timeProvider = FixedTimeProvider(fixedInstant, ZoneId.of("UTC"))
    private val repository = FakeTaskRepository()
    private val createUseCase = CreateTaskUseCase(repository, timeProvider)
    private val useCase = UpdateTaskUseCase(repository, timeProvider)

    @Test
    fun `updates existing task title, priority and removes due date`() =
        runTest {
            val task =
                createUseCase(
                    titleRaw = "Título original",
                    dueDateInstant = fixedInstant,
                )

            val updated =
                useCase(
                    id = task.id,
                    titleRaw = "Título nuevo",
                    priority = Priority.HIGH,
                    dueDateInstant = null,
                )

            assertEquals("Título nuevo", updated.title.value)
            assertEquals(Priority.HIGH, updated.priority)
            assertNull(updated.dueDate)
        }

    @Test(expected = TaskDomainException.TaskNotFound::class)
    fun `updating non-existing task throws TaskNotFound`() =
        runTest {
            useCase(
                id = TaskId("invalido"),
                titleRaw = "Nuevo título",
            )
        }
}
