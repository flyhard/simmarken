package se.simmarken.data.seed

import android.content.Context
import androidx.room.withTransaction
import se.simmarken.data.local.AppDatabase
import se.simmarken.data.local.dao.CatalogDao

class CatalogSeedLoader(
    private val context: Context,
    private val database: AppDatabase,
) {
    private val catalogDao: CatalogDao
        get() = database.catalogDao()

    suspend fun seedIfNeeded() {
        for (assetPath in SEED_ASSET_PATHS) {
            val seed = CatalogSeedParser.loadFromAssets(context, assetPath)
            val stored = catalogDao.findCatalogByCode(seed.code)
            if (!CatalogVersion.shouldMerge(seed.catalogVersion, stored?.catalogVersion)) {
                continue
            }
            mergeCatalog(seed)
        }
    }

    suspend fun mergeCatalog(seed: CatalogSeedDto) {
        database.withTransaction {
            val stored = catalogDao.findCatalogByCode(seed.code)
            catalogDao.upsertCatalog(
                seed.toEntity(existingId = stored?.id ?: 0),
            )
            val catalogId = catalogDao.findCatalogByCode(seed.code)!!.id
            val seedCategoryCodes = seed.categories.map { it.code }.toSet()
            val seedBadgeCodesByCategory =
                seed.categories.associate { category ->
                    category.code to category.badges.map { it.code }.toSet()
                }

            for (categorySeed in seed.categories) {
                mergeCategory(catalogId, categorySeed)
            }

            pruneObsoleteStructure(
                catalogId = catalogId,
                seedCategoryCodes = seedCategoryCodes,
                seedBadgeCodesByCategory = seedBadgeCodesByCategory,
            )
        }
    }

    private suspend fun mergeCategory(catalogId: Long, categorySeed: CategorySeedDto) {
        val existing = catalogDao.findCategoryByCatalogAndCode(catalogId, categorySeed.code)
        catalogDao.upsertCategory(
            categorySeed.toEntity(
                catalogId = catalogId,
                existingId = existing?.id ?: 0,
            ),
        )
        val categoryId = catalogDao.findCategoryByCatalogAndCode(catalogId, categorySeed.code)!!.id
        for (badgeSeed in categorySeed.badges) {
            mergeBadge(
                catalogId = catalogId,
                categoryId = categoryId,
                badgeSeed = badgeSeed,
            )
        }
    }

    private suspend fun mergeBadge(
        catalogId: Long,
        categoryId: Long,
        badgeSeed: BadgeSeedDto,
    ) {
        val existing =
            catalogDao.findBadgeByCatalogAndCode(catalogId, badgeSeed.code)
                ?: catalogDao.findBadgeByCategoryAndCode(categoryId, badgeSeed.code)
        catalogDao.upsertBadge(
            badgeSeed.toEntity(
                categoryId = categoryId,
                existingId = existing?.id ?: 0,
            ),
        )
        val badgeId = catalogDao.findBadgeByCategoryAndCode(categoryId, badgeSeed.code)!!.id
        for (requirementSeed in badgeSeed.requirements) {
            mergeRequirement(badgeId, requirementSeed)
        }
    }

    private suspend fun mergeRequirement(badgeId: Long, requirementSeed: RequirementSeedDto) {
        val existing = catalogDao.findRequirementByBadgeAndCode(badgeId, requirementSeed.code)
        catalogDao.upsertRequirement(
            requirementSeed.toEntity(
                badgeId = badgeId,
                existingId = existing?.id ?: 0,
            ),
        )
        // v1 merge intentionally does not prune requirements removed from seed assets.
    }

    private suspend fun pruneObsoleteStructure(
        catalogId: Long,
        seedCategoryCodes: Set<String>,
        seedBadgeCodesByCategory: Map<String, Set<String>>,
    ) {
        val storedCategories = catalogDao.listCategoriesForCatalog(catalogId)
        for (category in storedCategories) {
            if (category.code !in seedCategoryCodes) {
                catalogDao.deleteCategoryById(category.id)
                continue
            }

            val seedBadgeCodes = seedBadgeCodesByCategory[category.code].orEmpty()
            val storedBadges = catalogDao.listBadgesForCategory(category.id)
            for (badge in storedBadges) {
                if (badge.code !in seedBadgeCodes) {
                    catalogDao.deleteBadgeById(badge.id)
                }
            }
        }
    }

    companion object {
        private val SEED_ASSET_PATHS = listOf(
            "seed/simidrott.json",
            "seed/sls.json",
        )
    }
}
