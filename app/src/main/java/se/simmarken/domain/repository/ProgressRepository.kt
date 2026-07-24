package se.simmarken.domain.repository

import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

/**
 * Progress persistence API. Phase 5 owns D-03 write-once [BadgeProgressEntity.achievedAtEpochMillis]
 * and D-05 badge visual state derivation — this phase provides pass-through upserts only.
 */
interface ProgressRepository {
    fun observeRequirementProgress(kidId: Long): Flow<List<RequirementProgressEntity>>
    fun observeBadgeProgress(kidId: Long): Flow<List<BadgeProgressEntity>>
    suspend fun upsertRequirementProgress(progress: RequirementProgressEntity)
    suspend fun upsertBadgeProgress(progress: BadgeProgressEntity)

    /** Atomically upserts badge side-effects (if any) before requirement progress. */
    suspend fun applyRequirementToggle(
        requirementProgress: RequirementProgressEntity,
        badgeProgress: BadgeProgressEntity?,
    )
}
