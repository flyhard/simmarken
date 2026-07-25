package se.simmarken.ui.settings

import android.app.Application
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
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
import kotlinx.coroutines.withContext
import se.simmarken.R
import se.simmarken.data.export.ExportRepository
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.prefs.LanguageMode
import se.simmarken.data.prefs.LocalePreferencesRepository
import se.simmarken.data.prefs.mapModeToLocaleList
import se.simmarken.domain.export.ImportPreview
import se.simmarken.domain.export.InvalidReason
import se.simmarken.domain.repository.KidRepository

data class ExportKidPickerState(
    val kids: List<KidEntity>,
    val selectedKidIds: Set<Long>,
)

data class SettingsUiState(
    val languageMode: LanguageMode = LanguageMode.SYSTEM,
    val kids: List<KidEntity> = emptyList(),
    val isExporting: Boolean = false,
    val showExportKidPicker: Boolean = false,
    val exportKidPickerState: ExportKidPickerState? = null,
    val importPreview: ImportPreview? = null,
    val selectedNewKidStableIds: Set<String> = emptySet(),
    val importError: InvalidReason? = null,
    val snackbarMessageRes: Int? = null,
)

class SettingsViewModel(
    application: Application,
    private val kidRepository: KidRepository,
    private val exportRepository: ExportRepository,
    private val localePreferencesRepository: LocalePreferencesRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AndroidViewModel(application) {
    private val isExporting = MutableStateFlow(false)
    private val showExportKidPicker = MutableStateFlow(false)
    private val exportKidPickerState = MutableStateFlow<ExportKidPickerState?>(null)
    private val importPreview = MutableStateFlow<ImportPreview?>(null)
    private val selectedNewKidStableIds = MutableStateFlow<Set<String>>(emptySet())
    private val importError = MutableStateFlow<InvalidReason?>(null)
    private val snackbarMessageRes = MutableStateFlow<Int?>(null)

    val uiState = combine(
        kidRepository.observeAll(),
        localePreferencesRepository.mode,
        isExporting,
        showExportKidPicker,
        exportKidPickerState,
        importPreview,
        selectedNewKidStableIds,
        importError,
        snackbarMessageRes,
    ) { values ->
        SettingsUiState(
            kids = values[0] as List<KidEntity>,
            languageMode = values[1] as LanguageMode,
            isExporting = values[2] as Boolean,
            showExportKidPicker = values[3] as Boolean,
            exportKidPickerState = values[4] as ExportKidPickerState?,
            importPreview = values[5] as ImportPreview?,
            selectedNewKidStableIds = values[6] as Set<String>,
            importError = values[7] as InvalidReason?,
            snackbarMessageRes = values[8] as Int?,
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
            else -> {
                exportKidPickerState.value = ExportKidPickerState(
                    kids = kids,
                    selectedKidIds = kids.map { it.id }.toSet(),
                )
                showExportKidPicker.value = true
            }
        }
    }

    fun dismissExportKidPicker() {
        showExportKidPicker.value = false
        exportKidPickerState.value = null
    }

    fun toggleExportKidSelection(kidId: Long) {
        val current = exportKidPickerState.value ?: return
        val updated = if (kidId in current.selectedKidIds) {
            current.selectedKidIds - kidId
        } else {
            current.selectedKidIds + kidId
        }
        exportKidPickerState.value = current.copy(selectedKidIds = updated)
    }

    fun toggleExportSelectAll() {
        val current = exportKidPickerState.value ?: return
        val allSelected = current.selectedKidIds.size == current.kids.size
        exportKidPickerState.value = current.copy(
            selectedKidIds = if (allSelected) emptySet() else current.kids.map { it.id }.toSet(),
        )
    }

    fun confirmExportKidPicker() {
        val selected = exportKidPickerState.value?.selectedKidIds?.toList().orEmpty()
        if (selected.isEmpty()) return
        exportSelectedKids(selected)
    }

    fun exportSelectedKids(kidIds: List<Long>) {
        if (kidIds.isEmpty()) return
        showExportKidPicker.value = false
        exportKidPickerState.value = null
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

    fun onImportUriReceived(uri: Uri) {
        viewModelScope.launch(ioDispatcher) {
            val bytes = getApplication<Application>().contentResolver.openInputStream(uri)?.use {
                it.readBytes()
            } ?: run {
                importError.value = InvalidReason.InvalidFile
                return@launch
            }
            when (val result = exportRepository.parseBackup(bytes)) {
                is se.simmarken.domain.export.ValidationResult.Valid -> {
                    importError.value = null
                    val preview = exportRepository.planImport(result.dto)
                    importPreview.value = preview
                    selectedNewKidStableIds.value = emptySet()
                }
                is se.simmarken.domain.export.ValidationResult.Invalid -> {
                    importPreview.value = null
                    importError.value = result.reason
                }
            }
        }
    }

    fun toggleNewKidAccepted(stableId: String) {
        val updated = if (stableId in selectedNewKidStableIds.value) {
            selectedNewKidStableIds.value - stableId
        } else {
            selectedNewKidStableIds.value + stableId
        }
        selectedNewKidStableIds.value = updated
    }

    fun dismissImportPreview() {
        importPreview.value = null
        selectedNewKidStableIds.value = emptySet()
    }

    fun dismissImportError() {
        importError.value = null
    }

    fun confirmImport() {
        val preview = importPreview.value ?: return
        val accepted = selectedNewKidStableIds.value
        val canImport = preview.updateCount > 0 || accepted.isNotEmpty()
        if (!canImport) return

        viewModelScope.launch(ioDispatcher) {
            exportRepository.merge(preview, accepted)
            importPreview.value = null
            selectedNewKidStableIds.value = emptySet()
            snackbarMessageRes.value = R.string.import_success_snackbar
        }
    }

    fun clearSnackbarMessage() {
        snackbarMessageRes.value = null
    }

    fun setLanguageMode(mode: LanguageMode) {
        viewModelScope.launch(ioDispatcher) {
            localePreferencesRepository.setMode(mode)
            withContext(Dispatchers.Main.immediate) {
                AppCompatDelegate.setApplicationLocales(mapModeToLocaleList(mode))
            }
        }
    }
}
