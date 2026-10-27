package com.architecturelab.taskapp.di

import android.content.Context
import androidx.room.Room
import com.architecturelab.taskapp.data.local.database.TaskDatabase
import com.architecturelab.taskapp.data.local.source.TaskLocalDataSourceImpl
import com.architecturelab.taskapp.data.repository.TaskRepositoryImpl
import com.architecturelab.taskapp.domain.repository.TaskRepository
import com.architecturelab.taskapp.domain.time.SystemTimeProvider
import com.architecturelab.taskapp.domain.time.TimeProvider
import com.architecturelab.taskapp.domain.usecase.CreateTaskUseCase
import com.architecturelab.taskapp.domain.usecase.DeleteTaskUseCase
import com.architecturelab.taskapp.domain.usecase.GetTasksUseCase
import com.architecturelab.taskapp.domain.usecase.ToggleTaskStatusUseCase
import com.architecturelab.taskapp.domain.usecase.UpdateTaskUseCase

class AppContainer(context: Context) {
    private val database: TaskDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            TaskDatabase::class.java,
            "task_app_database.db",
        ).build()
    }

    private val localDataSource by lazy {
        TaskLocalDataSourceImpl(database.taskDao())
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(localDataSource)
    }

    val timeProvider: TimeProvider by lazy {
        SystemTimeProvider()
    }

    val createTaskUseCase by lazy {
        CreateTaskUseCase(taskRepository, timeProvider)
    }

    val updateTaskUseCase by lazy {
        UpdateTaskUseCase(taskRepository, timeProvider)
    }

    val toggleTaskStatusUseCase by lazy {
        ToggleTaskStatusUseCase(taskRepository, timeProvider)
    }

    val deleteTaskUseCase by lazy {
        DeleteTaskUseCase(taskRepository)
    }

    val getTasksUseCase by lazy {
        GetTasksUseCase(taskRepository, timeProvider)
    }
}
