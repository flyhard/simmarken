package se.simmarken.domain.repository

import se.simmarken.data.local.dao.BadgeProgressDao
import se.simmarken.data.local.dao.RequirementProgressDao
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

class ProgressRepositoryImpl(
    private val requirementProgressDao: RequirementProgressDao,
    private val badgeProgressDao: BadgeProgressDao,
) : ProgressRepository {
    override fun observeRequirementProgress(kidId: Long) =
        requirementProgressDao.observeForKid(kidId)

    override fun observeBadgeProgress(kidId: Long) = badgeProgressDao.observeForKid(kidId)

    override suspend fun upsertRequirementProgress(progress: RequirementProgressEntity) =
        requirementProgressDao.upsert(progress)

    override suspend fun upsertBadgeProgress(progress: BadgeProgressEntity) =
        badgeProgressDao.upsert(progress)
}
