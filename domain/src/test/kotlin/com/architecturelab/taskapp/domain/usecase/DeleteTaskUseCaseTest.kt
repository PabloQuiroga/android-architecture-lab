package com.architecturelab.taskapp.domain.usecase

import com.architecturelab.taskapp.domain.repository.FakeTaskRepository
import com.architecturelab.taskapp.domain.time.FixedTimeProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DeleteTaskUseCaseTest {
    private val repository = FakeTaskRepository()
    private val timeProvider = FixedTimeProvider(Instant.now(), ZoneId.of("UTC"))
    private val createUseCase = CreateTaskUseCase(repository, timeProvider)
    private val useCase = DeleteTaskUseCase(repository)

    @Test
    fun `deleting saved task removes it from repository`() =
        runTest {
            val task = createUseCase("Tarea a borrar")
            assertNotNull(repository.getTaskById(task.id))

            useCase(task.id)

            assertNull(repository.getTaskById(task.id))
        }
}
