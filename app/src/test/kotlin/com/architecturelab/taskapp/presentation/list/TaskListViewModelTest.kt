package com.architecturelab.taskapp.presentation.list

import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.TaskFilter
import com.architecturelab.taskapp.domain.model.TaskSort
import com.architecturelab.taskapp.domain.repository.FakeTaskRepository
import com.architecturelab.taskapp.domain.time.FixedTimeProvider
import com.architecturelab.taskapp.domain.usecase.CreateTaskUseCase
import com.architecturelab.taskapp.domain.usecase.DeleteTaskUseCase
import com.architecturelab.taskapp.domain.usecase.GetTasksUseCase
import com.architecturelab.taskapp.domain.usecase.ToggleTaskStatusUseCase
import com.architecturelab.taskapp.presentation.list.state.TaskListUiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val now = Instant.parse("2026-10-26T12:00:00Z")
    private val timeProvider = FixedTimeProvider(now, ZoneId.of("UTC"))
    private val repository = FakeTaskRepository()

    private val createTaskUseCase = CreateTaskUseCase(repository, timeProvider)
    private val getTasksUseCase = GetTasksUseCase(repository, timeProvider)
    private val toggleTaskStatusUseCase = ToggleTaskStatusUseCase(repository, timeProvider)
    private val deleteTaskUseCase = DeleteTaskUseCase(repository)

    private lateinit var viewModel: TaskListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel =
            TaskListViewModel(
                getTasksUseCase = getTasksUseCase,
                toggleTaskStatusUseCase = toggleTaskStatusUseCase,
                deleteTaskUseCase = deleteTaskUseCase,
                timeProvider = timeProvider,
            )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state emits empty task list when repository is empty`() =
        runTest {
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(state.tasks.isEmpty())
            assertEquals(TaskFilter.All, state.activeFilter)
            assertEquals(TaskSort.DueDateAsc, state.activeSort)
        }

    @Test
    fun `initial state emits list of tasks formatted as UiModels`() =
        runTest {
            val task = createTaskUseCase("Tarea 1", priority = Priority.HIGH)
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(1, state.tasks.size)
            assertEquals(task.id.value, state.tasks.first().id)
            assertEquals("Tarea 1", state.tasks.first().title)
            assertEquals(Priority.HIGH, state.tasks.first().priority)
        }

    @Test
    fun `onEvent OnFilterSelected updates activeFilter in state`() =
        runTest {
            createTaskUseCase("Alta", priority = Priority.HIGH)
            createTaskUseCase("Baja", priority = Priority.LOW)
            testDispatcher.scheduler.advanceUntilIdle()

            val newFilter = TaskFilter.ByPriority(Priority.HIGH)
            viewModel.onEvent(TaskListUiEvent.OnFilterSelected(newFilter))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(newFilter, state.activeFilter)
            assertEquals(1, state.tasks.size)
            assertEquals("Alta", state.tasks.first().title)
        }

    @Test
    fun `onEvent OnSortSelected updates activeSort in state`() =
        runTest {
            createTaskUseCase("Baja", priority = Priority.LOW)
            createTaskUseCase("Alta", priority = Priority.HIGH)
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(TaskListUiEvent.OnSortSelected(TaskSort.PriorityDesc))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertEquals(TaskSort.PriorityDesc, state.activeSort)
            assertEquals("Alta", state.tasks.first().title)
        }

    @Test
    fun `onEvent OnToggleTaskStatus triggers ToggleTaskStatusUseCase`() =
        runTest {
            val task = createTaskUseCase("Tarea pendiente")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(TaskListUiEvent.OnToggleTaskStatus(task.id.value))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(state.tasks.first().isCompleted)
        }

    @Test
    fun `onEvent OnDeleteTask triggers DeleteTaskUseCase`() =
        runTest {
            val task = createTaskUseCase("Tarea a eliminar")
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(TaskListUiEvent.OnDeleteTask(task.id.value))
            testDispatcher.scheduler.advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(state.tasks.isEmpty())
        }
}
