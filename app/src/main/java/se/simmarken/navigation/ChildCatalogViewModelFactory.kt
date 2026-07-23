package se.simmarken.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se.simmarken.domain.repository.KidRepository
import se.simmarken.ui.child.ChildCatalogViewModel

class ChildCatalogViewModelFactory(
    private val kidRepository: KidRepository,
    private val kidId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChildCatalogViewModel::class.java)) {
            return ChildCatalogViewModel(kidRepository, kidId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
