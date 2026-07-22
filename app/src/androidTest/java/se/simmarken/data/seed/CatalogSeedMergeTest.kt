package se.simmarken.data.seed

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
import se.simmarken.data.local.AppDatabase
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementProgressEntity

@RunWith(AndroidJUnit4::class)
class CatalogSeedMergeTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(AppDatabase.DB_NAME)
    }

    @Test
    fun progressPreservedWhenTextChanges() = runBlocking {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DB_NAME,
        ).build()
        val loader = CatalogSeedLoader(context, db)
        val catalogDao = db.catalogDao()

        loader.seedIfNeeded()

        val simidrott = catalogDao.findCatalogByCode("simidrott")!!
        val vattenvana = catalogDao.observeCategories(simidrott.id).first()
            .first { it.code == "vattenvana" }
        val baddarenGron = catalogDao.observeBadges(vattenvana.id).first()
            .first { it.code == "baddaren-gron" }
        val requirement = catalogDao.observeRequirements(baddarenGron.id).first()
            .first { it.code == "baddaren-gron-01" }
        val requirementId = requirement.id

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

        catalogDao.upsertCatalog(
            simidrott.copy(catalogVersion = "2020.01.01"),
        )

        val baseSeed = CatalogSeedParser.loadFromAssets(context, "seed/simidrott.json")
        val bumpedSeed = baseSeed.copy(
            catalogVersion = "2026.04.01",
            categories = baseSeed.categories.map { category ->
                if (category.code != "vattenvana") {
                    category
                } else {
                    category.copy(
                        badges = category.badges.map { badge ->
                            if (badge.code != "baddaren-gron") {
                                badge
                            } else {
                                badge.copy(
                                    requirements = badge.requirements.map { req ->
                                        if (req.code != "baddaren-gron-01") {
                                            req
                                        } else {
                                            req.copy(textSv = "Uppdaterad kravtext för merge-test.")
                                        }
                                    },
                                )
                            }
                        },
                    )
                }
            },
        )

        loader.mergeCatalog(bumpedSeed)

        val updatedRequirement = catalogDao.findRequirementByBadgeAndCode(
            badgeId = baddarenGron.id,
            code = "baddaren-gron-01",
        )!!
        val progressRows = db.requirementProgressDao().observeForKid(kidId).first()

        assertEquals(1, progressRows.size)
        assertTrue(progressRows.first().isAchieved)
        assertEquals(requirementId, updatedRequirement.id)
        assertEquals("Uppdaterad kravtext för merge-test.", updatedRequirement.textSv)

        db.close()
    }
}
