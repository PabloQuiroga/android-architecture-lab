package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.model.Priority
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

class CreateTaskUseCaseTest {
    private val fixedInstant = Instant.parse("2026-10-26T12:00:00Z")
    private val timeProvider = FixedTimeProvider(fixedInstant, ZoneId.of("UTC"))
    private val repository = FakeTaskRepository()
    private val useCase = CreateTaskUseCase(repository, timeProvider)

    @Test
    fun `creates task with title and default values`() =
        runTest {
            val created = useCase("  Estudiar Kotlin  ")

            assertEquals("Estudiar Kotlin", created.title.value)
            assertEquals(Priority.MEDIUM, created.priority)
            assertEquals(TaskStatus.PENDING, created.status)
            assertNull(created.description)
            assertNull(created.dueDate)

            val saved = repository.getTaskById(created.id)
            assertNotNull(saved)
        }

    @Test
    fun `creates task specifying date without time sets midnight 00_00`() =
        runTest {
            val dateInstant = Instant.parse("2026-10-28T15:30:00Z")
            val created =
                useCase(
                    titleRaw = "Tarea con fecha",
                    dueDateInstant = dateInstant,
                    isTimeSet = false,
                )

            val expectedMidnight = Instant.parse("2026-10-28T00:00:00Z")
            assertEquals(expectedMidnight, created.dueDate?.value)
        }
}
