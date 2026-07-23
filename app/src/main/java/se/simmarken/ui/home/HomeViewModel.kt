package se.simmarken.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.domain.repository.KidRepository

sealed interface KidSheetState {
    data object Hidden : KidSheetState
    data object Add : KidSheetState
}

data class HomeUiState(
    val kids: List<KidEntity> = emptyList(),
    val sheetState: KidSheetState = KidSheetState.Hidden,
)

class HomeViewModel(
    private val kidRepository: KidRepository,
) : ViewModel() {
    private val sheetState = MutableStateFlow<KidSheetState>(KidSheetState.Hidden)

    val uiState = combine(
        kidRepository.observeAll(),
        sheetState,
    ) { kids, sheet ->
        HomeUiState(kids = kids, sheetState = sheet)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun openAddSheet() {
        sheetState.value = KidSheetState.Add
    }

    fun closeSheet() {
        sheetState.value = KidSheetState.Hidden
    }
}
