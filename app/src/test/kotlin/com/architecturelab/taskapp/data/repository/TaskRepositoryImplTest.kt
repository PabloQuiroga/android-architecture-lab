package com.architecturelab.taskapp.data.repository

import com.architecturelab.taskapp.data.local.entity.TaskEntity
import com.architecturelab.taskapp.data.local.source.TaskLocalDataSource
import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.model.TaskStatus
import com.architecturelab.taskapp.domain.model.TaskTitle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TaskRepositoryImplTest {
    private val localDataSource: TaskLocalDataSource = mockk()
    private val repository = TaskRepositoryImpl(localDataSource)

    private val entity =
        TaskEntity(
            id = "123",
            title = "Tarea Repo",
            description = "Desc",
            priority = "HIGH",
            dueDateEpochMillis = null,
            status = "PENDING",
        )

    @Test
    fun `getTasks maps entity flow to domain model flow`() =
        runTest {
            every { localDataSource.getTasks() } returns flowOf(listOf(entity))

            val tasks = repository.getTasks().first()

            assertEquals(1, tasks.size)
            assertEquals("123", tasks.first().id.value)
            assertEquals("Tarea Repo", tasks.first().title.value)
            assertEquals(Priority.HIGH, tasks.first().priority)
        }

    @Test
    fun `getTaskById maps retrieved entity to domain model`() =
        runTest {
            coEvery { localDataSource.getTaskById("123") } returns entity

            val task = repository.getTaskById(TaskId("123"))

            assertNotNull(task)
            assertEquals("123", task?.id?.value)
        }

    @Test
    fun `saveTask delegates mapped entity to localDataSource`() =
        runTest {
            coEvery { localDataSource.saveTask(any()) } returns Unit

            val domainTask =
                Task(
                    id = TaskId("123"),
                    title = TaskTitle.create("Tarea Repo"),
                    priority = Priority.HIGH,
                    status = TaskStatus.PENDING,
                )

            repository.saveTask(domainTask)

            coVerify { localDataSource.saveTask(match { it.id == "123" && it.title == "Tarea Repo" }) }
        }

    @Test
    fun `deleteTask delegates ID to localDataSource`() =
        runTest {
            coEvery { localDataSource.deleteTask("123") } returns Unit

            repository.deleteTask(TaskId("123"))

            coVerify { localDataSource.deleteTask("123") }
        }
}
