package com.architecturelab.taskapp.data.local.mapper

import com.architecturelab.taskapp.data.local.entity.TaskEntity
import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.Task
import com.architecturelab.taskapp.domain.model.TaskDueDate
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.model.TaskStatus
import com.architecturelab.taskapp.domain.model.TaskTitle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TaskMapperTest {
    @Test
    fun `maps Task domain model to TaskEntity correctly`() {
        val instant = Instant.parse("2026-10-26T12:00:00Z")
        val task =
            Task(
                id = TaskId("123"),
                title = TaskTitle.create("Probar mapeador"),
                description = "Descripción de prueba",
                priority = Priority.HIGH,
                dueDate = TaskDueDate(instant),
                status = TaskStatus.PENDING,
            )

        val entity = task.toEntity()

        assertEquals("123", entity.id)
        assertEquals("Probar mapeador", entity.title)
        assertEquals("Descripción de prueba", entity.description)
        assertEquals("HIGH", entity.priority)
        assertEquals(instant.toEpochMilli(), entity.dueDateEpochMillis)
        assertEquals("PENDING", entity.status)
    }

    @Test
    fun `maps TaskEntity to Task domain model correctly`() {
        val instant = Instant.parse("2026-10-26T12:00:00Z")
        val entity =
            TaskEntity(
                id = "456",
                title = "Desde BD",
                description = null,
                priority = "MEDIUM",
                dueDateEpochMillis = instant.toEpochMilli(),
                status = "COMPLETED",
            )

        val domain = entity.toDomain()

        assertEquals("456", domain.id.value)
        assertEquals("Desde BD", domain.title.value)
        assertNull(domain.description)
        assertEquals(Priority.MEDIUM, domain.priority)
        assertEquals(instant, domain.dueDate?.value)
        assertEquals(TaskStatus.COMPLETED, domain.status)
    }
}
