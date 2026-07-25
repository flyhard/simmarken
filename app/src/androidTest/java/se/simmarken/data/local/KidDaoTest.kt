package se.simmarken.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

@RunWith(AndroidJUnit4::class)
class KidDaoTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DB_NAME)
    }

    @Test
    fun observeAllOrder() = runBlocking {
        val db = buildDatabase()
        db.kidDao().upsert(
            KidEntity(
                stableId = "stable-adam",
                name = "Adam",
                avatarColorArgb = 0xFF1E88E5.toInt(),
                createdAtEpochMillis = 100L,
                sortOrder = 0,
            ),
        )
        db.kidDao().upsert(
            KidEntity(
                stableId = "stable-ella",
                name = "Ella",
                avatarColorArgb = 0xFF43A047.toInt(),
                createdAtEpochMillis = 200L,
                sortOrder = 1,
            ),
        )

        val kids = db.kidDao().observeAll().first()
        assertEquals(2, kids.size)
        assertEquals("Adam", kids[0].name)
        assertEquals("Ella", kids[1].name)

        db.close()
    }

    @Test
    fun upsertPreservesFields() = runBlocking {
        val db = buildDatabase()
        val id = db.kidDao().upsert(
            KidEntity(
                stableId = "stable-adam",
                name = "Adam",
                avatarColorArgb = 0xFF1E88E5.toInt(),
                createdAtEpochMillis = 100L,
                sortOrder = 0,
            ),
        )
        val original = db.kidDao().observeById(id).first()!!

        db.kidDao().upsert(
            original.copy(name = "Adamsson"),
        )

        val updated = db.kidDao().observeById(id).first()!!
        assertEquals("Adamsson", updated.name)
        assertEquals(original.id, updated.id)
        assertEquals(original.createdAtEpochMillis, updated.createdAtEpochMillis)
        assertEquals(original.sortOrder, updated.sortOrder)

        db.close()
    }

    @Test
    fun deleteCascadesProgress() = runBlocking {
        val db = buildDatabase()
        val catalogId = db.catalogDao().upsertCatalog(
            CatalogEntity(
                code = "simidrott",
                nameSv = "Svensk Simidrott",
                nameEn = "Swedish Swimming",
                catalogVersion = "2026.01.01",
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
                stableId = "stable-ella",
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
        assertEquals(1, db.requirementProgressDao().observeForKid(kidId).first().size)

        db.kidDao().deleteById(kidId)

        assertTrue(db.kidDao().observeById(kidId).first() == null)
        assertEquals(0, db.requirementProgressDao().observeForKid(kidId).first().size)

        db.close()
    }

    private fun buildDatabase(): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        ).build()
}
