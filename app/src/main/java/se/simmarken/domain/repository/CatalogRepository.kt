package se.simmarken.domain.repository

import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity

interface CatalogRepository {
    fun observeCatalogs(): Flow<List<CatalogEntity>>
    fun observeCategories(catalogId: Long): Flow<List<CategoryEntity>>
    fun observeBadges(categoryId: Long): Flow<List<BadgeEntity>>
    fun observeRequirements(badgeId: Long): Flow<List<RequirementEntity>>
    suspend fun upsertCatalog(catalog: CatalogEntity): Long
    suspend fun upsertCategory(category: CategoryEntity): Long
    suspend fun upsertBadge(badge: BadgeEntity): Long
    suspend fun upsertRequirement(requirement: RequirementEntity): Long
}
