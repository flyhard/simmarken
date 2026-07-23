package se.simmarken.ui.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.RequirementProgressEntity
import se.simmarken.domain.BadgeCatalogMapper
import se.simmarken.domain.ProgressWriteLogic
import se.simmarken.domain.model.BadgeDetailUiState
import se.simmarken.domain.model.RequirementRowUiModel
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.ProgressRepository

@OptIn(ExperimentalCoroutinesApi::class)
class BadgeDetailViewModel(
    private val kidId: Long,
    private val badgeId: Long,
    private val catalogRepository: CatalogRepository,
    private val progressRepository: ProgressRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    private val showUncheckPurchaseDialog = MutableStateFlow(false)

    val uiState = catalogRepository.observeBadgeById(badgeId)
        .flatMapLatest { badge ->
            if (badge == null) {
                showUncheckPurchaseDialog.map { showDialog ->
                    BadgeDetailUiState(
                        badgeMissing = true,
                        isLoading = false,
                        showUncheckPurchaseDialog = showDialog,
                    )
                }
            } else {
                combine(
                    catalogRepository.observeCategoryById(badge.categoryId),
                    catalogRepository.observeRequirements(badgeId),
                    progressRepository.observeRequirementProgress(kidId),
                    progressRepository.observeBadgeProgress(kidId),
                    showUncheckPurchaseDialog,
                ) { category, requirements, reqProgress, badgeProgress, showDialog ->
                    val requirementProgressById =
                        reqProgress.associate { it.requirementId to it.isAchieved }
                    val badgeProgressEntity = badgeProgress.find { it.badgeId == badgeId }
                    val isGotten = badgeProgressEntity?.isGotten == true
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
                        isGotten = isGotten,
                        isPurchaseEnabled = ProgressWriteLogic.isPurchaseEnabled(
                            totalRequirements = cell.totalRequirements,
                            visualState = cell.visualState,
                        ),
                        showUncheckPurchaseDialog = showDialog,
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
            val requirements = catalogRepository.observeRequirements(badgeId).first()
            val requirementProgressById = progressRepository.observeRequirementProgress(kidId)
                .first()
                .associate { it.requirementId to it.isAchieved }
            val existingBadgeProgress = progressRepository.observeBadgeProgress(kidId)
                .first()
                .find { it.badgeId == badgeId }

            val currentlyAchieved = requirementProgressById[requirementId] == true
            val flipped = !currentlyAchieved
            var badgeProgress = existingBadgeProgress
                ?: BadgeProgressEntity(kidId = kidId, badgeId = badgeId)
            var badgeProgressDirty = false

            if (ProgressWriteLogic.shouldClearGottenOnUncheck(
                    isGotten = badgeProgress.isGotten,
                    flippingToAchieved = flipped,
                )
            ) {
                badgeProgress = badgeProgress.copy(
                    isGotten = false,
                    gottenAtEpochMillis = null,
                )
                badgeProgressDirty = true
            }

            progressRepository.upsertRequirementProgress(
                RequirementProgressEntity(
                    kidId = kidId,
                    requirementId = requirementId,
                    isAchieved = flipped,
                    achievedAtEpochMillis = if (flipped) System.currentTimeMillis() else null,
                ),
            )

            val updatedProgressById = requirementProgressById.toMutableMap().apply {
                put(requirementId, flipped)
            }
            val achievedCount = requirements.count { updatedProgressById[it.id] == true }
            if (ProgressWriteLogic.shouldSetAchievedAt(
                    totalRequirements = requirements.size,
                    achievedCountAfterToggle = achievedCount,
                    existingAchievedAt = badgeProgress.achievedAtEpochMillis,
                )
            ) {
                badgeProgress = badgeProgress.copy(
                    achievedAtEpochMillis = ProgressWriteLogic.preserveAchievedAt(
                        existing = badgeProgress.achievedAtEpochMillis,
                        proposed = System.currentTimeMillis(),
                    ),
                )
                badgeProgressDirty = true
            }

            if (badgeProgressDirty) {
                progressRepository.upsertBadgeProgress(badgeProgress)
            }
        }
    }

    fun setGotten(value: Boolean) {
        if (!value) return
        viewModelScope.launch(ioDispatcher) {
            val existing = progressRepository.observeBadgeProgress(kidId)
                .first()
                .find { it.badgeId == badgeId }
            val base = existing ?: BadgeProgressEntity(kidId = kidId, badgeId = badgeId)
            progressRepository.upsertBadgeProgress(
                base.copy(
                    isGotten = true,
                    gottenAtEpochMillis = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun requestClearGotten() {
        showUncheckPurchaseDialog.value = true
    }

    fun dismissClearGotten() {
        showUncheckPurchaseDialog.value = false
    }

    fun confirmClearGotten() {
        showUncheckPurchaseDialog.value = false
        viewModelScope.launch(ioDispatcher) {
            val existing = progressRepository.observeBadgeProgress(kidId)
                .first()
                .find { it.badgeId == badgeId }
                ?: return@launch
            progressRepository.upsertBadgeProgress(
                existing.copy(
                    isGotten = false,
                    gottenAtEpochMillis = null,
                ),
            )
        }
    }
}
