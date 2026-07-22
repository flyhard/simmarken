package se.simmarken.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.domain.repository.KidRepository

data class HomeUiState(
    val kidCount: Int = 0,
)

class HomeViewModel(
    private val kidRepository: KidRepository,
) : ViewModel() {
    val uiState = kidRepository.observeAll()
        .map { kids -> HomeUiState(kidCount = kids.size) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun addTestKid() {
        viewModelScope.launch(Dispatchers.IO) {
            kidRepository.upsert(
                KidEntity(
                    name = "Test Kid",
                    avatarColorArgb = 0xFF2196F3.toInt(),
                    createdAtEpochMillis = System.currentTimeMillis(),
                    sortOrder = 0,
                ),
            )
        }
    }
}
