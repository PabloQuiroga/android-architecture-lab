package com.architecturelab.taskapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    val priority: String,
    val dueDateEpochMillis: Long?,
    val status: String,
)
