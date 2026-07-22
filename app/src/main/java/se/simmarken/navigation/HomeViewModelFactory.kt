package se.simmarken.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se.simmarken.domain.repository.KidRepository
import se.simmarken.ui.home.HomeViewModel

class HomeViewModelFactory(
    private val kidRepository: KidRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(kidRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
