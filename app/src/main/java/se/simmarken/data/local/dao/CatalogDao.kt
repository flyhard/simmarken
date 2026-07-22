package se.simmarken.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalogs ORDER BY sortOrder, nameSv")
    fun observeCatalogs(): Flow<List<CatalogEntity>>

    @Query("SELECT * FROM categories WHERE catalogId = :catalogId ORDER BY sortOrder, nameSv")
    fun observeCategories(catalogId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM badges WHERE categoryId = :categoryId ORDER BY sortOrder, nameSv")
    fun observeBadges(categoryId: Long): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM requirements WHERE badgeId = :badgeId ORDER BY sortOrder, code")
    fun observeRequirements(badgeId: Long): Flow<List<RequirementEntity>>

    @Query("SELECT * FROM catalogs WHERE code = :code LIMIT 1")
    suspend fun findCatalogByCode(code: String): CatalogEntity?

    @Query("SELECT * FROM categories WHERE catalogId = :catalogId AND code = :code LIMIT 1")
    suspend fun findCategoryByCatalogAndCode(catalogId: Long, code: String): CategoryEntity?

    @Query("SELECT * FROM badges WHERE categoryId = :categoryId AND code = :code LIMIT 1")
    suspend fun findBadgeByCategoryAndCode(categoryId: Long, code: String): BadgeEntity?

    @Query("SELECT * FROM requirements WHERE badgeId = :badgeId AND code = :code LIMIT 1")
    suspend fun findRequirementByBadgeAndCode(badgeId: Long, code: String): RequirementEntity?

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun findCategoryById(categoryId: Long): CategoryEntity?

    @Query("SELECT * FROM badges WHERE categoryId = :categoryId ORDER BY sortOrder, nameSv")
    suspend fun listBadgesForCategory(categoryId: Long): List<BadgeEntity>

    @Query("SELECT COUNT(*) FROM catalogs")
    suspend fun countCatalogs(): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCatalog(catalog: CatalogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBadge(badge: BadgeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRequirement(requirement: RequirementEntity): Long
}
