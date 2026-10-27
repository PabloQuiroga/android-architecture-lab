package com.architecturelab.taskapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.architecturelab.taskapp.TaskApplication
import com.architecturelab.taskapp.presentation.form.TaskFormViewModel
import com.architecturelab.taskapp.presentation.list.TaskListScreen
import com.architecturelab.taskapp.presentation.list.TaskListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as TaskApplication).appContainer

        val listViewModel =
            TaskListViewModel(
                getTasksUseCase = appContainer.getTasksUseCase,
                toggleTaskStatusUseCase = appContainer.toggleTaskStatusUseCase,
                deleteTaskUseCase = appContainer.deleteTaskUseCase,
                timeProvider = appContainer.timeProvider,
            )

        val formViewModel =
            TaskFormViewModel(
                createTaskUseCase = appContainer.createTaskUseCase,
                updateTaskUseCase = appContainer.updateTaskUseCase,
                taskRepository = appContainer.taskRepository,
                timeProvider = appContainer.timeProvider,
            )

        setContent {
            MaterialTheme {
                Surface {
                    TaskListScreen(
                        listViewModel = listViewModel,
                        formViewModel = formViewModel,
                    )
                }
            }
        }
    }
}
