package com.architecturelab.taskapp.presentation.list.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.architecturelab.taskapp.domain.model.Priority
import com.architecturelab.taskapp.domain.model.TaskFilter
import com.architecturelab.taskapp.domain.model.TaskSort

@Composable
fun FilterSortChipRow(
    activeFilter: TaskFilter,
    activeSort: TaskSort,
    onFilterSelected: (TaskFilter) -> Unit,
    onSortSelected: (TaskSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = activeFilter is TaskFilter.All,
            onClick = { onFilterSelected(TaskFilter.All) },
            label = { Text("Todas") },
        )

        FilterChip(
            selected = activeFilter is TaskFilter.Overdue,
            onClick = { onFilterSelected(TaskFilter.Overdue) },
            label = { Text("Vencidas") },
        )

        FilterChip(
            selected = activeFilter is TaskFilter.DueToday,
            onClick = { onFilterSelected(TaskFilter.DueToday) },
            label = { Text("Vencen hoy") },
        )

        FilterChip(
            selected = activeFilter is TaskFilter.Upcoming,
            onClick = { onFilterSelected(TaskFilter.Upcoming) },
            label = { Text("Próximas") },
        )

        FilterChip(
            selected = activeFilter is TaskFilter.NoDueDate,
            onClick = { onFilterSelected(TaskFilter.NoDueDate) },
            label = { Text("Sin vencimiento") },
        )

        FilterChip(
            selected = activeFilter is TaskFilter.ByPriority && (activeFilter as TaskFilter.ByPriority).priority == Priority.HIGH,
            onClick = { onFilterSelected(TaskFilter.ByPriority(Priority.HIGH)) },
            label = { Text("Alta Prioridad") },
        )

        FilterChip(
            selected = activeSort == TaskSort.DueDateAsc || activeSort == TaskSort.DueDateDesc,
            onClick = {
                val nextSort = if (activeSort == TaskSort.DueDateAsc) TaskSort.DueDateDesc else TaskSort.DueDateAsc
                onSortSelected(nextSort)
            },
            label = {
                Text(if (activeSort == TaskSort.DueDateAsc) "Fecha ⬆" else "Fecha ⬇")
            },
        )

        FilterChip(
            selected = activeSort == TaskSort.PriorityDesc || activeSort == TaskSort.PriorityAsc,
            onClick = {
                val nextSort = if (activeSort == TaskSort.PriorityDesc) TaskSort.PriorityAsc else TaskSort.PriorityDesc
                onSortSelected(nextSort)
            },
            label = {
                Text(if (activeSort == TaskSort.PriorityDesc) "Prioridad ⬇" else "Prioridad ⬆")
            },
        )
    }
}
