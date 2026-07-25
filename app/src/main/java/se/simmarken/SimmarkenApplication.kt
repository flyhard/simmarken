package se.simmarken

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import se.simmarken.data.prefs.mapModeToLocaleList
import se.simmarken.di.AppContainer

class SimmarkenApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch {
            container.localePreferencesRepository.mode.collect { mode ->
                AppCompatDelegate.setApplicationLocales(mapModeToLocaleList(mode))
            }
        }
    }
}
