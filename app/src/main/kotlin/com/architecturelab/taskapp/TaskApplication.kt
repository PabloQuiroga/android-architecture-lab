package com.architecturelab.taskapp

import android.app.Application
import com.architecturelab.taskapp.di.AppContainer

class TaskApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(applicationContext)
    }
}
