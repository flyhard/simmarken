package se.simmarken.ui.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.simmarken.data.local.entity.RequirementProgressEntity
import se.simmarken.domain.BadgeCatalogMapper
import se.simmarken.domain.model.BadgeDetailUiState
import se.simmarken.domain.model.RequirementRowUiModel
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.ProgressRepository

class BadgeDetailViewModel(
    private val kidId: Long,
    private val badgeId: Long,
    private val catalogRepository: CatalogRepository,
    private val progressRepository: ProgressRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
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
                    val cell = BadgeCatalogMapper.toBadgeCellUiModel(
                        badge = badge,
                        categoryCode = category?.code.orEmpty(),
                        requirements = requirements,
                        requirementProgressById = requirementProgressById,
                        isGotten = isGotten,
                    )
                    val requirementRows = requirements
                        .sortedBy { it.sortOrder }
                        .map { requirement ->
                            RequirementRowUiModel(
                                id = requirement.id,
                                textSv = requirement.textSv,
                                isAchieved = requirementProgressById[requirement.id] == true,
                            )
                        }
                    BadgeDetailUiState(
                        nameSv = cell.nameSv,
                        imageAssetPath = cell.imageAssetPath,
                        categoryCode = cell.categoryCode,
                        visualState = cell.visualState,
                        progressFraction = cell.progressFraction,
                        achievedCount = cell.achievedCount,
                        totalRequirements = cell.totalRequirements,
                        requirements = requirementRows,
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

    fun toggleRequirement(requirementId: Long) {
        viewModelScope.launch(ioDispatcher) {
            val requirementProgressById = progressRepository.observeRequirementProgress(kidId)
                .first()
                .associate { it.requirementId to it.isAchieved }
            val currentlyAchieved = requirementProgressById[requirementId] == true
            val flipped = !currentlyAchieved
            progressRepository.upsertRequirementProgress(
                RequirementProgressEntity(
                    kidId = kidId,
                    requirementId = requirementId,
                    isAchieved = flipped,
                    achievedAtEpochMillis = if (flipped) System.currentTimeMillis() else null,
                ),
            )
        }
    }
}
