package se.simmarken

import android.app.Application
import se.simmarken.di.AppContainer

class SimmarkenApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
