package se.simmarken.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.domain.repository.KidRepository

sealed interface KidSheetState {
    data object Hidden : KidSheetState
    data class Add(val sessionId: Long) : KidSheetState
    data class Edit(val kidId: Long, val sessionId: Long) : KidSheetState
}

data class HomeUiState(
    val kids: List<KidEntity> = emptyList(),
    val sheetState: KidSheetState = KidSheetState.Hidden,
    val deleteTarget: KidEntity? = null,
)

class HomeViewModel(
    private val kidRepository: KidRepository,
) : ViewModel() {
    private val sheetState = MutableStateFlow<KidSheetState>(KidSheetState.Hidden)
    private val deleteTarget = MutableStateFlow<KidEntity?>(null)
    private var nextSheetSessionId = 0L

    val uiState = combine(
        kidRepository.observeAll(),
        sheetState,
        deleteTarget,
    ) { kids, sheet, delete ->
        HomeUiState(kids = kids, sheetState = sheet, deleteTarget = delete)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun openAddSheet() {
        deleteTarget.value = null
        sheetState.value = KidSheetState.Add(sessionId = nextSheetSessionId++)
    }

    fun openEditSheet(kidId: Long) {
        deleteTarget.value = null
        sheetState.value = KidSheetState.Edit(kidId = kidId, sessionId = nextSheetSessionId++)
    }

    fun closeSheet() {
        sheetState.value = KidSheetState.Hidden
    }

    fun requestDelete(kid: KidEntity) {
        sheetState.value = KidSheetState.Hidden
        deleteTarget.value = kid
    }

    fun dismissDelete() {
        deleteTarget.value = null
    }

    fun confirmDelete() {
        val kid = deleteTarget.value ?: return
        deleteTarget.value = null
        viewModelScope.launch(Dispatchers.IO) {
            kidRepository.delete(kid.id)
        }
    }
}
