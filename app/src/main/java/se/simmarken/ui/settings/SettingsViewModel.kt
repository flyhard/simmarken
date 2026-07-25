package se.simmarken.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.simmarken.data.export.ExportRepository
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.domain.repository.KidRepository

data class SettingsUiState(
    val kids: List<KidEntity> = emptyList(),
    val isExporting: Boolean = false,
    val showExportKidPicker: Boolean = false,
)

class SettingsViewModel(
    application: Application,
    private val kidRepository: KidRepository,
    private val exportRepository: ExportRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AndroidViewModel(application) {
    private val isExporting = MutableStateFlow(false)
    private val showExportKidPicker = MutableStateFlow(false)

    val uiState = combine(
        kidRepository.observeAll(),
        isExporting,
        showExportKidPicker,
    ) { kids, exporting, picker ->
        SettingsUiState(
            kids = kids,
            isExporting = exporting,
            showExportKidPicker = picker,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    private val _shareExportUri = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val shareExportUri = _shareExportUri.asSharedFlow()

    fun onExportClicked() {
        val kids = uiState.value.kids
        when {
            kids.isEmpty() -> return
            kids.size == 1 -> exportSelectedKids(listOf(kids.first().id))
            else -> showExportKidPicker.value = true
        }
    }

    fun dismissExportKidPicker() {
        showExportKidPicker.value = false
    }

    fun exportSelectedKids(kidIds: List<Long>) {
        if (kidIds.isEmpty()) return
        showExportKidPicker.value = false
        viewModelScope.launch(ioDispatcher) {
            isExporting.value = true
            try {
                val dto = exportRepository.buildBackup(kidIds)
                val file = exportRepository.writeExportFile(getApplication(), dto)
                val uri = exportRepository.fileToShareUri(getApplication(), file)
                _shareExportUri.emit(uri)
            } finally {
                isExporting.value = false
            }
        }
    }
}
