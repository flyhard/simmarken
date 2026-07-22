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
            val catalogId = catalogDao.upsertCatalog(
                seed.toEntity(existingId = stored?.id ?: 0),
            )
            for (categorySeed in seed.categories) {
                mergeCategory(catalogId, categorySeed)
            }
        }
    }

    private suspend fun mergeCategory(catalogId: Long, categorySeed: CategorySeedDto) {
        val existing = catalogDao.findCategoryByCatalogAndCode(catalogId, categorySeed.code)
        val categoryId = catalogDao.upsertCategory(
            categorySeed.toEntity(
                catalogId = catalogId,
                existingId = existing?.id ?: 0,
            ),
        )
        for (badgeSeed in categorySeed.badges) {
            mergeBadge(categoryId, badgeSeed)
        }
    }

    private suspend fun mergeBadge(categoryId: Long, badgeSeed: BadgeSeedDto) {
        val existing = catalogDao.findBadgeByCategoryAndCode(categoryId, badgeSeed.code)
        val badgeId = catalogDao.upsertBadge(
            badgeSeed.toEntity(
                categoryId = categoryId,
                existingId = existing?.id ?: 0,
            ),
        )
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

    companion object {
        private val SEED_ASSET_PATHS = listOf(
            "seed/simidrott.json",
            "seed/sls.json",
        )
    }
}
