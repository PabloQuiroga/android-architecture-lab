package com.architecturelab.taskapp.presentation.form

import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.TaskId
import com.architecturelab.taskapp.domain.repository.FakeTaskRepository
import com.architecturelab.taskapp.domain.time.FixedTimeProvider
import com.architecturelab.taskapp.domain.usecase.CreateTaskUseCase
import com.architecturelab.taskapp.domain.usecase.UpdateTaskUseCase
import com.architecturelab.taskapp.presentation.form.state.TaskFormUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class TaskFormViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-10-26T12:00:00Z")
    private val timeProvider = FixedTimeProvider(now, ZoneId.of("UTC"))
    private val repository = FakeTaskRepository()

    private val createTaskUseCase = CreateTaskUseCase(repository, timeProvider)
    private val updateTaskUseCase = UpdateTaskUseCase(repository, timeProvider)

    private lateinit var viewModel: TaskFormViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel =
            TaskFormViewModel(
                createTaskUseCase = createTaskUseCase,
                updateTaskUseCase = updateTaskUseCase,
                taskRepository = repository,
                timeProvider = timeProvider,
            )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent OnTitleChanged updates titleRaw and clears titleError when valid`() =
        runTest {
            viewModel.onEvent(TaskFormUiEvent.OnTitleChanged("Nueva tarea"))

            val state = viewModel.uiState.first()
            assertEquals("Nueva tarea", state.titleRaw)
            assertNull(state.titleError)
        }

    @Test
    fun `onEvent OnSaveClicked with empty title sets titleError in state`() =
        runTest {
            viewModel.onEvent(TaskFormUiEvent.OnTitleChanged("   "))
            viewModel.onEvent(TaskFormUiEvent.OnSaveClicked)

            val state = viewModel.uiState.first()
            assertNotNull(state.titleError)
            assertFalse(state.isSaved)
        }

    @Test
    fun `onEvent OnSaveClicked in creation mode saves task and sets isSaved to true`() =
        runTest {
            viewModel.onEvent(TaskFormUiEvent.OnTitleChanged("Comprar pan"))
            viewModel.onEvent(TaskFormUiEvent.OnPriorityChanged(Priority.HIGH))
            viewModel.onEvent(TaskFormUiEvent.OnSaveClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(state.isSaved)

            val tasks = repository.getTaskById(TaskId(repository.getTasks().first().first().id.value))
            assertNotNull(tasks)
            assertEquals("Comprar pan", tasks?.title?.value)
            assertEquals(Priority.HIGH, tasks?.priority)
        }

    @Test
    fun `onEvent OnLoadTask in edit mode loads existing task into form state`() =
        runTest {
            val created =
                createTaskUseCase(
                    titleRaw = "Tarea a editar",
                    priority = Priority.LOW,
                    dueDateInstant = now,
                )

            viewModel.onEvent(TaskFormUiEvent.OnLoadTask(created.id.value))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(state.isEditMode)
            assertEquals("Tarea a editar", state.titleRaw)
            assertEquals(Priority.LOW, state.priority)
            assertEquals(now, state.dueDateInstant)
        }

    @Test
    fun `onEvent OnSaveClicked in edit mode updates task and sets isSaved to true`() =
        runTest {
            val created = createTaskUseCase("Original")

            viewModel.onEvent(TaskFormUiEvent.OnLoadTask(created.id.value))
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(TaskFormUiEvent.OnTitleChanged("Editado"))
            viewModel.onEvent(TaskFormUiEvent.OnSaveClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(state.isSaved)

            val updated = repository.getTaskById(created.id)
            assertEquals("Editado", updated?.title?.value)
        }
}
