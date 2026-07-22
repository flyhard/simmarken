package se.simmarken

import android.app.Application

class SimmarkenApplication : Application() {
    lateinit var container: Unit
        private set

    override fun onCreate() {
        super.onCreate()
        // AppContainer lands in Plan 01-03
        container = Unit
    }
}
