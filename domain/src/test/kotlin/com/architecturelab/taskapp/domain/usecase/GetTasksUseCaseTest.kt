package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.TaskFilter
import com.architecturelab.taskapp.domain.model.TaskSort
import com.architecturelab.taskapp.domain.repository.FakeTaskRepository
import com.architecturelab.taskapp.domain.time.FixedTimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class GetTasksUseCaseTest {
    private val now = Instant.parse("2026-10-26T12:00:00Z")
    private val timeProvider = FixedTimeProvider(now, ZoneId.of("UTC"))
    private val repository = FakeTaskRepository()
    private val createUseCase = CreateTaskUseCase(repository, timeProvider)
    private val useCase = GetTasksUseCase(repository, timeProvider)

    @Test
    fun `getTasks applies combined filter and deterministic sort`() =
        runTest {
            val tHigh = createUseCase("Tarea Alta", priority = Priority.HIGH)
            createUseCase("Tarea Baja", priority = Priority.LOW)

            val result =
                useCase(
                    filter = TaskFilter.ByPriority(Priority.HIGH),
                    sort = TaskSort.PriorityDesc,
                ).first()

            assertEquals(1, result.size)
            assertEquals(tHigh.id, result.first().id)
        }
}
