package se.simmarken.domain.repository

import se.simmarken.data.local.AppDatabase
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

class ProgressRepositoryImpl(
    private val database: AppDatabase,
) : ProgressRepository {
    private val requirementProgressDao = database.requirementProgressDao()
    private val badgeProgressDao = database.badgeProgressDao()
    override fun observeRequirementProgress(kidId: Long) =
        requirementProgressDao.observeForKid(kidId)

    override fun observeBadgeProgress(kidId: Long) = badgeProgressDao.observeForKid(kidId)

    override suspend fun upsertRequirementProgress(progress: RequirementProgressEntity) =
        requirementProgressDao.upsert(progress)

    override suspend fun upsertBadgeProgress(progress: BadgeProgressEntity) =
        badgeProgressDao.upsert(progress)

    override suspend fun applyRequirementToggle(
        requirementProgress: RequirementProgressEntity,
        badgeProgress: BadgeProgressEntity?,
    ) = database.applyRequirementToggle(requirementProgress, badgeProgress)

    override suspend fun getRequirementProgressForKids(kidIds: List<Long>) =
        requirementProgressDao.findForKids(kidIds)

    override suspend fun getBadgeProgressForKids(kidIds: List<Long>) =
        badgeProgressDao.findForKids(kidIds)
}
