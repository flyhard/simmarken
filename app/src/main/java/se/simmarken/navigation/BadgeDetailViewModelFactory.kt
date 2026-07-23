package se.simmarken.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.ProgressRepository
import se.simmarken.ui.badge.BadgeDetailViewModel

class BadgeDetailViewModelFactory(
    private val kidId: Long,
    private val badgeId: Long,
    private val catalogRepository: CatalogRepository,
    private val progressRepository: ProgressRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BadgeDetailViewModel::class.java)) {
            return BadgeDetailViewModel(
                kidId = kidId,
                badgeId = badgeId,
                catalogRepository = catalogRepository,
                progressRepository = progressRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
