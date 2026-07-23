package se.simmarken.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se.simmarken.domain.repository.KidRepository
import se.simmarken.ui.home.KidFormViewModel

class KidFormViewModelFactory(
    private val kidRepository: KidRepository,
    private val kidId: Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KidFormViewModel::class.java)) {
            return KidFormViewModel(kidRepository, kidId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
