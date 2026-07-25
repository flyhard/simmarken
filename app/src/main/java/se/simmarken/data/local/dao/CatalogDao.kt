package se.simmarken.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
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

    @Query("SELECT * FROM requirements")
    fun observeAllRequirements(): Flow<List<RequirementEntity>>

    @Query("SELECT * FROM badges ORDER BY sortOrder, nameSv")
    fun observeAllBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM categories")
    fun observeAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM badges WHERE id = :badgeId LIMIT 1")
    fun observeBadgeById(badgeId: Long): Flow<BadgeEntity?>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    fun observeCategoryById(categoryId: Long): Flow<CategoryEntity?>

    @Query("SELECT * FROM catalogs WHERE code = :code LIMIT 1")
    suspend fun findCatalogByCode(code: String): CatalogEntity?

    @Query("SELECT * FROM categories WHERE catalogId = :catalogId AND code = :code LIMIT 1")
    suspend fun findCategoryByCatalogAndCode(catalogId: Long, code: String): CategoryEntity?

    @Query("SELECT * FROM badges WHERE categoryId = :categoryId AND code = :code LIMIT 1")
    suspend fun findBadgeByCategoryAndCode(categoryId: Long, code: String): BadgeEntity?

    @Query(
        """
        SELECT badges.* FROM badges
        INNER JOIN categories ON badges.categoryId = categories.id
        WHERE categories.catalogId = :catalogId AND badges.code = :code
        LIMIT 1
        """,
    )
    suspend fun findBadgeByCatalogAndCode(catalogId: Long, code: String): BadgeEntity?

    @Query("SELECT * FROM categories WHERE catalogId = :catalogId ORDER BY sortOrder, nameSv")
    suspend fun listCategoriesForCatalog(catalogId: Long): List<CategoryEntity>

    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: Long)

    @Query("DELETE FROM badges WHERE id = :badgeId")
    suspend fun deleteBadgeById(badgeId: Long)

    @Query("SELECT * FROM requirements WHERE badgeId = :badgeId AND code = :code LIMIT 1")
    suspend fun findRequirementByBadgeAndCode(badgeId: Long, code: String): RequirementEntity?

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun findCategoryById(categoryId: Long): CategoryEntity?

    @Query("SELECT * FROM badges WHERE categoryId = :categoryId ORDER BY sortOrder, nameSv")
    suspend fun listBadgesForCategory(categoryId: Long): List<BadgeEntity>

    @Query("SELECT * FROM catalogs")
    suspend fun listCatalogs(): List<CatalogEntity>

    @Query("SELECT * FROM categories")
    suspend fun listCategories(): List<CategoryEntity>

    @Query("SELECT * FROM badges")
    suspend fun listBadges(): List<BadgeEntity>

    @Query("SELECT * FROM requirements")
    suspend fun listRequirements(): List<RequirementEntity>

    @Query("SELECT * FROM requirements WHERE id = :requirementId LIMIT 1")
    suspend fun findRequirementById(requirementId: Long): RequirementEntity?

    @Query("SELECT * FROM badges WHERE id = :badgeId LIMIT 1")
    suspend fun findBadgeById(badgeId: Long): BadgeEntity?

    @Query("SELECT COUNT(*) FROM catalogs")
    suspend fun countCatalogs(): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int

    @Upsert
    suspend fun upsertCatalog(catalog: CatalogEntity): Long

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity): Long

    @Upsert
    suspend fun upsertBadge(badge: BadgeEntity): Long

    @Upsert
    suspend fun upsertRequirement(requirement: RequirementEntity): Long
}
