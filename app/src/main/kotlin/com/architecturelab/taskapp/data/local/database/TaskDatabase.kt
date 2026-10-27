package com.architecturelab.taskapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.architecturelab.taskapp.data.local.dao.TaskDao
import com.architecturelab.taskapp.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
