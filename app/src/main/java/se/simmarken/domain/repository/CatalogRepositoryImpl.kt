package se.simmarken.domain.repository

import se.simmarken.data.local.dao.CatalogDao
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity

class CatalogRepositoryImpl(
    private val catalogDao: CatalogDao,
) : CatalogRepository {
    override fun observeCatalogs() = catalogDao.observeCatalogs()
    override fun observeCategories(catalogId: Long) = catalogDao.observeCategories(catalogId)
    override fun observeBadges(categoryId: Long) = catalogDao.observeBadges(categoryId)
    override fun observeRequirements(badgeId: Long) = catalogDao.observeRequirements(badgeId)
    override fun observeAllRequirements() = catalogDao.observeAllRequirements()
    override fun observeBadgeById(badgeId: Long) = catalogDao.observeBadgeById(badgeId)
    override suspend fun upsertCatalog(catalog: CatalogEntity) = catalogDao.upsertCatalog(catalog)
    override suspend fun upsertCategory(category: CategoryEntity) = catalogDao.upsertCategory(category)
    override suspend fun upsertBadge(badge: BadgeEntity) = catalogDao.upsertBadge(badge)
    override suspend fun upsertRequirement(requirement: RequirementEntity) =
        catalogDao.upsertRequirement(requirement)
}
