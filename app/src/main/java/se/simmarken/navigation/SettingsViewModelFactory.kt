package se.simmarken.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import se.simmarken.SimmarkenApplication
import se.simmarken.data.export.ExportRepository
import se.simmarken.domain.repository.KidRepository
import se.simmarken.ui.settings.SettingsViewModel

class SettingsViewModelFactory(
    private val application: SimmarkenApplication,
    private val kidRepository: KidRepository,
    private val exportRepository: ExportRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                application = application,
                kidRepository = kidRepository,
                exportRepository = exportRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
