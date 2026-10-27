package com.architecturelab.taskapp.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.architecturelab.taskapp.data.local.database.TaskDatabase
import com.architecturelab.taskapp.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskDaoTest {
    private lateinit var database: TaskDatabase
    private lateinit var taskDao: TaskDao

    @Before
    fun setup() {
        database =
            Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                TaskDatabase::class.java,
            ).allowMainThreadQueries().build()
        taskDao = database.taskDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndQueryTaskByIdReturnsTaskEntity() =
        runTest {
            val entity =
                TaskEntity(
                    id = "1",
                    title = "Prueba Room",
                    description = "Descripción",
                    priority = "HIGH",
                    dueDateEpochMillis = 1000L,
                    status = "PENDING",
                )

            taskDao.insertTask(entity)

            val retrieved = taskDao.getTaskById("1")
            assertNotNull(retrieved)
            assertEquals("Prueba Room", retrieved?.title)
            assertEquals("HIGH", retrieved?.priority)
        }

    @Test
    fun updateTaskUpdatesEntityInDatabase() =
        runTest {
            val entity =
                TaskEntity(
                    id = "1",
                    title = "Título Original",
                    description = null,
                    priority = "MEDIUM",
                    dueDateEpochMillis = null,
                    status = "PENDING",
                )
            taskDao.insertTask(entity)

            val updatedEntity = entity.copy(title = "Título Editado", status = "COMPLETED")
            taskDao.updateTask(updatedEntity)

            val retrieved = taskDao.getTaskById("1")
            assertEquals("Título Editado", retrieved?.title)
            assertEquals("COMPLETED", retrieved?.status)
        }

    @Test
    fun deleteTaskByIdRemovesTaskFromDatabase() =
        runTest {
            val entity =
                TaskEntity(
                    id = "1",
                    title = "A borrar",
                    description = null,
                    priority = "LOW",
                    dueDateEpochMillis = null,
                    status = "PENDING",
                )
            taskDao.insertTask(entity)

            taskDao.deleteTaskById("1")

            val retrieved = taskDao.getTaskById("1")
            assertNull(retrieved)
        }

    @Test
    fun getTasksReturnsFlowOfTasks() =
        runTest {
            val t1 = TaskEntity("1", "T1", null, "LOW", null, "PENDING")
            val t2 = TaskEntity("2", "T2", null, "HIGH", null, "PENDING")

            taskDao.insertTask(t1)
            taskDao.insertTask(t2)

            val list = taskDao.getTasks().first()
            assertEquals(2, list.size)
        }
}
