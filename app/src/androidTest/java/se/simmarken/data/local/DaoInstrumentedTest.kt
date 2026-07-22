package se.simmarken.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

@RunWith(AndroidJUnit4::class)
class DaoInstrumentedTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DB_NAME)
    }

    @Test
    fun catalogChainInsertsWithForeignKeys() = runBlocking {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        ).build()

        val catalogId = db.catalogDao().upsertCatalog(
            CatalogEntity(
                code = "simidrott",
                nameSv = "Svensk Simidrott",
                nameEn = "Swedish Swimming",
                catalogVersion = "1",
                sortOrder = 0,
            ),
        )
        val categoryId = db.catalogDao().upsertCategory(
            CategoryEntity(
                catalogId = catalogId,
                code = "grund",
                nameSv = "Grund",
                nameEn = "Basic",
                sortOrder = 0,
            ),
        )
        val badgeId = db.catalogDao().upsertBadge(
            BadgeEntity(
                categoryId = categoryId,
                code = "simmare",
                nameSv = "Simmare",
                nameEn = "Swimmer",
                imageAssetPath = null,
                sortOrder = 0,
            ),
        )
        val requirementId = db.catalogDao().upsertRequirement(
            RequirementEntity(
                badgeId = badgeId,
                code = "req1",
                textSv = "Simma 25 meter",
                textEn = "Swim 25 meters",
                sortOrder = 0,
            ),
        )
        val kidId = db.kidDao().upsert(
            KidEntity(
                name = "Ella",
                avatarColorArgb = 0xFF2196F3.toInt(),
                createdAtEpochMillis = 1L,
                sortOrder = 0,
            ),
        )
        db.requirementProgressDao().upsert(
            RequirementProgressEntity(
                kidId = kidId,
                requirementId = requirementId,
                isAchieved = true,
                achievedAtEpochMillis = 1000L,
            ),
        )
        db.badgeProgressDao().upsert(
            BadgeProgressEntity(
                kidId = kidId,
                badgeId = badgeId,
                isGotten = false,
                achievedAtEpochMillis = 5000L,
            ),
        )

        assertEquals(1, db.catalogDao().observeCatalogs().first().size)
        assertEquals(1, db.catalogDao().observeCategories(catalogId).first().size)
        assertEquals(1, db.catalogDao().observeBadges(categoryId).first().size)
        assertEquals(1, db.catalogDao().observeRequirements(badgeId).first().size)
        assertEquals(1, db.kidDao().observeAll().first().size)
        assertEquals(1, db.requirementProgressDao().observeForKid(kidId).first().size)
        assertEquals(1, db.badgeProgressDao().observeForKid(kidId).first().size)

        db.close()
    }
}
