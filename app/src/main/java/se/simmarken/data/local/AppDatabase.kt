package se.simmarken.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverters
import se.simmarken.data.local.dao.BadgeProgressDao
import se.simmarken.data.local.dao.CatalogDao
import se.simmarken.data.local.dao.KidDao
import se.simmarken.data.local.dao.RequirementProgressDao
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

@Database(
    entities = [
        CatalogEntity::class,
        CategoryEntity::class,
        BadgeEntity::class,
        RequirementEntity::class,
        KidEntity::class,
        RequirementProgressEntity::class,
        BadgeProgressEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun kidDao(): KidDao
    abstract fun requirementProgressDao(): RequirementProgressDao
    abstract fun badgeProgressDao(): BadgeProgressDao

    @Transaction
    suspend fun applyRequirementToggle(
        requirementProgress: RequirementProgressEntity,
        badgeProgress: BadgeProgressEntity?,
    ) {
        badgeProgress?.let { badgeProgressDao().upsert(it) }
        requirementProgressDao().upsert(requirementProgress)
    }

    companion object {
        const val DB_NAME = "simmarken.db"
    }
}
