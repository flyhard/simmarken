package se.simmarken.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import se.simmarken.domain.repository.KidRepository

class ChildCatalogViewModel(
    kidRepository: KidRepository,
    kidId: Long,
) : ViewModel() {
    val childName = kidRepository.observeById(kidId)
        .map { kid -> kid?.name ?: "" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "",
        )
}
