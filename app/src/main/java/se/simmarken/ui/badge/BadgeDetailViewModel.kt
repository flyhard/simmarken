package se.simmarken.ui.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import se.simmarken.domain.BadgeStateCalculator
import se.simmarken.domain.model.BadgeDetailUiState
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.ProgressRepository

class BadgeDetailViewModel(
    private val kidId: Long,
    private val badgeId: Long,
    private val catalogRepository: CatalogRepository,
    private val progressRepository: ProgressRepository,
) : ViewModel() {
    val uiState = catalogRepository.observeBadgeById(badgeId)
        .flatMapLatest { badge ->
            if (badge == null) {
                flowOf(BadgeDetailUiState(badgeMissing = true, isLoading = false))
            } else {
                combine(
                    catalogRepository.observeCategoryById(badge.categoryId),
                    catalogRepository.observeRequirements(badgeId),
                    progressRepository.observeRequirementProgress(kidId),
                    progressRepository.observeBadgeProgress(kidId),
                ) { category, requirements, reqProgress, badgeProgress ->
                    val requirementProgressById =
                        reqProgress.associate { it.requirementId to it.isAchieved }
                    val isGotten = badgeProgress.any { it.badgeId == badgeId && it.isGotten }
                    val totalRequirements = requirements.size
                    val achievedCount = requirements.count { requirement ->
                        requirementProgressById[requirement.id] == true
                    }
                    BadgeDetailUiState(
                        nameSv = badge.nameSv,
                        imageAssetPath = badge.imageAssetPath,
                        categoryCode = category?.code.orEmpty(),
                        visualState = BadgeStateCalculator.compute(
                            totalRequirements = totalRequirements,
                            achievedCount = achievedCount,
                            isGotten = isGotten,
                        ),
                        progressFraction = BadgeStateCalculator.progressFraction(
                            achievedCount = achievedCount,
                            totalRequirements = totalRequirements,
                        ),
                        achievedCount = achievedCount,
                        totalRequirements = totalRequirements,
                        badgeMissing = false,
                        isLoading = category == null,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BadgeDetailUiState(),
        )
}
