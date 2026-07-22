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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCatalog(catalog: CatalogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBadge(badge: BadgeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRequirement(requirement: RequirementEntity): Long
}
