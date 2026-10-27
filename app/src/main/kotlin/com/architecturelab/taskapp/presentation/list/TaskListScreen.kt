package com.architecturelab.taskapp.presentation.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.architecturelab.taskapp.presentation.form.TaskFormViewModel
import com.architecturelab.taskapp.presentation.form.components.TaskFormDialog
import com.architecturelab.taskapp.presentation.form.state.TaskFormUiEvent
import com.architecturelab.taskapp.presentation.list.components.FilterSortChipRow
import com.architecturelab.taskapp.presentation.list.components.TaskCard
import com.architecturelab.taskapp.presentation.list.state.TaskListUiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    listViewModel: TaskListViewModel,
    formViewModel: TaskFormViewModel,
    modifier: Modifier = Modifier,
) {
    val listState by listViewModel.uiState.collectAsState()
    val formState by formViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showFormDialog by remember { mutableStateOf(false) }

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) {
            showFormDialog = false
            formViewModel.onEvent(TaskFormUiEvent.OnTitleChanged(""))
        }
    }

    LaunchedEffect(listState.userMessage) {
        listState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            listViewModel.onEvent(TaskListUiEvent.OnUserMessageDismissed)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Tareas") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    formViewModel.onEvent(TaskFormUiEvent.OnTitleChanged(""))
                    showFormDialog = true
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar Tarea",
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            FilterSortChipRow(
                activeFilter = listState.activeFilter,
                activeSort = listState.activeSort,
                onFilterSelected = { listViewModel.onEvent(TaskListUiEvent.OnFilterSelected(it)) },
                onSortSelected = { listViewModel.onEvent(TaskListUiEvent.OnSortSelected(it)) },
            )

            if (listState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (listState.tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No hay tareas para mostrar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = listState.tasks,
                        key = { it.id },
                    ) { task ->
                        TaskCard(
                            task = task,
                            onToggleStatus = { listViewModel.onEvent(TaskListUiEvent.OnToggleTaskStatus(it)) },
                            onEditTask = { taskId ->
                                formViewModel.onEvent(TaskFormUiEvent.OnLoadTask(taskId))
                                showFormDialog = true
                            },
                            onDeleteTask = { listViewModel.onEvent(TaskListUiEvent.OnDeleteTask(it)) },
                        )
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        TaskFormDialog(
            state = formState,
            onEvent = formViewModel::onEvent,
            onDismiss = { showFormDialog = false },
        )
    }
}
