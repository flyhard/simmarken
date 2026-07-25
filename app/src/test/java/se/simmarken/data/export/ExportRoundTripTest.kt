package se.simmarken.data.export

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import se.simmarken.data.local.AppDatabase
import se.simmarken.data.local.dao.CatalogDao
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.data.local.entity.RequirementProgressEntity
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.repository.ProgressRepository

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ExportRoundTripTest {
    private fun createRepository(
        kidRepository: KidRepository,
        progressRepository: ProgressRepository,
        catalogDao: CatalogDao,
    ): ExportRepository {
        val context: Context = ApplicationProvider.getApplicationContext()
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        return ExportRepository(kidRepository, progressRepository, catalogDao, database)
    }

    @Test
    fun exportRoundTrip_preservesProgress() = runTest {
        val stableId = "550e8400-e29b-41d4-a716-446655440000"
        val kid = KidEntity(
            id = 1L,
            stableId = stableId,
            name = "Ella",
            avatarColorArgb = 0xFF2196F3.toInt(),
            createdAtEpochMillis = 1_000L,
            sortOrder = 0,
        )
        val catalog = CatalogEntity(
            id = 10L,
            code = "simidrott",
            nameSv = "Svensk Simidrott",
            nameEn = "Swedish Swimming",
            catalogVersion = "2026.01.01",
            sortOrder = 0,
        )
        val category = CategoryEntity(
            id = 20L,
            catalogId = 10L,
            code = "grund",
            nameSv = "Grund",
            nameEn = "Basic",
            sortOrder = 0,
        )
        val badge = BadgeEntity(
            id = 30L,
            categoryId = 20L,
            code = "simmare",
            nameSv = "Simmare",
            nameEn = "Swimmer",
            imageAssetPath = null,
            sortOrder = 0,
        )
        val requirement = RequirementEntity(
            id = 40L,
            badgeId = 30L,
            code = "req1",
            textSv = "Simma 25 meter",
            textEn = "Swim 25 meters",
            sortOrder = 0,
        )
        val requirementProgress = RequirementProgressEntity(
            kidId = 1L,
            requirementId = 40L,
            isAchieved = true,
            achievedAtEpochMillis = 5_000L,
            updatedAtEpochMillis = 5_000L,
        )
        val badgeProgress = BadgeProgressEntity(
            kidId = 1L,
            badgeId = 30L,
            isGotten = true,
            achievedAtEpochMillis = 4_000L,
            gottenAtEpochMillis = 6_000L,
            updatedAtEpochMillis = 6_000L,
        )

        val exportRepository = createRepository(
            kidRepository = FakeKidRepository(listOf(kid)),
            progressRepository = FakeProgressRepository(
                requirementProgress = listOf(requirementProgress),
                badgeProgress = listOf(badgeProgress),
            ),
            catalogDao = FakeCatalogDao(
                catalogs = listOf(catalog),
                categories = listOf(category),
                badges = listOf(badge),
                requirements = listOf(requirement),
            ),
        )

        val backup = exportRepository.buildBackup(listOf(1L))
        val json = exportRepository.encode(backup)
        val decoded = exportRepository.decode(json)

        assertEquals(1, decoded.exportVersion)
        assertEquals(1, decoded.kids.size)
        val kidBackup = decoded.kids.first()
        assertEquals(stableId, kidBackup.stableId)
        assertEquals("Ella", kidBackup.name)
        assertEquals(1, kidBackup.requirementProgress.size)
        assertEquals("simidrott", kidBackup.requirementProgress.first().catalogCode)
        assertEquals("simmare", kidBackup.requirementProgress.first().badgeCode)
        assertEquals("req1", kidBackup.requirementProgress.first().requirementCode)
        assertTrue(kidBackup.requirementProgress.first().isAchieved)
        assertEquals(1, kidBackup.badgeProgress.size)
        assertTrue(kidBackup.badgeProgress.first().isGotten)
    }

    @Test
    fun exportOmitsCatalog() = runTest {
        val exportRepository = createRepository(
            kidRepository = FakeKidRepository(
                listOf(
                    KidEntity(
                        id = 1L,
                        stableId = "kid-1",
                        name = "Adam",
                        avatarColorArgb = 0xFF1E88E5.toInt(),
                        createdAtEpochMillis = 100L,
                        sortOrder = 0,
                    ),
                ),
            ),
            progressRepository = FakeProgressRepository(),
            catalogDao = FakeCatalogDao(),
        )

        val json = exportRepository.encode(exportRepository.buildBackup(listOf(1L)))

        assertFalse(json.contains("\"categories\""))
        assertFalse(json.contains("\"catalogVersion\""))
        assertTrue(json.contains("\"exportVersion\""))
        assertTrue(json.contains("\"kids\""))
    }

    private class FakeKidRepository(
        private val kids: List<KidEntity>,
    ) : KidRepository {
        override fun observeAll() = kotlinx.coroutines.flow.flowOf(kids)
        override fun observeById(kidId: Long) =
            kotlinx.coroutines.flow.flowOf(kids.find { it.id == kidId })
        override suspend fun findByStableId(stableId: String) = kids.find { it.stableId == stableId }
        override suspend fun findByIds(ids: List<Long>) = kids.filter { it.id in ids }
        override suspend fun findAll() = kids
        override suspend fun upsert(kid: KidEntity) = kid.id
        override suspend fun delete(kidId: Long) = Unit
    }

    private class FakeProgressRepository(
        private val requirementProgress: List<RequirementProgressEntity> = emptyList(),
        private val badgeProgress: List<BadgeProgressEntity> = emptyList(),
    ) : ProgressRepository {
        override fun observeRequirementProgress(kidId: Long) =
            kotlinx.coroutines.flow.flowOf(requirementProgress.filter { it.kidId == kidId })

        override fun observeBadgeProgress(kidId: Long) =
            kotlinx.coroutines.flow.flowOf(badgeProgress.filter { it.kidId == kidId })

        override suspend fun upsertRequirementProgress(progress: RequirementProgressEntity) = Unit
        override suspend fun upsertBadgeProgress(progress: BadgeProgressEntity) = Unit
        override suspend fun applyRequirementToggle(
            requirementProgress: RequirementProgressEntity,
            badgeProgress: BadgeProgressEntity?,
        ) = Unit

        override suspend fun getRequirementProgressForKids(kidIds: List<Long>) =
            requirementProgress.filter { it.kidId in kidIds }

        override suspend fun getBadgeProgressForKids(kidIds: List<Long>) =
            badgeProgress.filter { it.kidId in kidIds }
    }

    private class FakeCatalogDao(
        private val catalogs: List<CatalogEntity> = emptyList(),
        private val categories: List<CategoryEntity> = emptyList(),
        private val badges: List<BadgeEntity> = emptyList(),
        private val requirements: List<RequirementEntity> = emptyList(),
    ) : CatalogDao {
        override fun observeCatalogs() = kotlinx.coroutines.flow.flowOf(catalogs)
        override fun observeCategories(catalogId: Long) =
            kotlinx.coroutines.flow.flowOf(categories.filter { it.catalogId == catalogId })
        override fun observeBadges(categoryId: Long) =
            kotlinx.coroutines.flow.flowOf(badges.filter { it.categoryId == categoryId })
        override fun observeRequirements(badgeId: Long) =
            kotlinx.coroutines.flow.flowOf(requirements.filter { it.badgeId == badgeId })
        override fun observeAllRequirements() = kotlinx.coroutines.flow.flowOf(requirements)
        override fun observeAllBadges() = kotlinx.coroutines.flow.flowOf(badges)
        override fun observeAllCategories() = kotlinx.coroutines.flow.flowOf(categories)
        override fun observeBadgeById(badgeId: Long) =
            kotlinx.coroutines.flow.flowOf(badges.find { it.id == badgeId })
        override fun observeCategoryById(categoryId: Long) =
            kotlinx.coroutines.flow.flowOf(categories.find { it.id == categoryId })
        override suspend fun findCatalogByCode(code: String) = catalogs.find { it.code == code }
        override suspend fun findCategoryByCatalogAndCode(catalogId: Long, code: String) =
            categories.find { it.catalogId == catalogId && it.code == code }
        override suspend fun findBadgeByCategoryAndCode(categoryId: Long, code: String) =
            badges.find { it.categoryId == categoryId && it.code == code }
        override suspend fun findBadgeByCatalogAndCode(catalogId: Long, code: String) =
            badges.find { badge ->
                categories.any { it.id == badge.categoryId && it.catalogId == catalogId } &&
                    badge.code == code
            }
        override suspend fun listCategoriesForCatalog(catalogId: Long) =
            categories.filter { it.catalogId == catalogId }
        override suspend fun deleteCategoryById(categoryId: Long) = Unit
        override suspend fun deleteBadgeById(badgeId: Long) = Unit
        override suspend fun findRequirementByBadgeAndCode(badgeId: Long, code: String) =
            requirements.find { it.badgeId == badgeId && it.code == code }
        override suspend fun findCategoryById(categoryId: Long) = categories.find { it.id == categoryId }
        override suspend fun listBadgesForCategory(categoryId: Long) =
            badges.filter { it.categoryId == categoryId }
        override suspend fun listCatalogs() = catalogs
        override suspend fun listCategories() = categories
        override suspend fun listBadges() = badges
        override suspend fun listRequirements() = requirements
        override suspend fun findRequirementById(requirementId: Long) =
            requirements.find { it.id == requirementId }
        override suspend fun findBadgeById(badgeId: Long) = badges.find { it.id == badgeId }
        override suspend fun countCatalogs() = catalogs.size
        override suspend fun countCategories() = categories.size
        override suspend fun upsertCatalog(catalog: CatalogEntity) = catalog.id
        override suspend fun upsertCategory(category: CategoryEntity) = category.id
        override suspend fun upsertBadge(badge: BadgeEntity) = badge.id
        override suspend fun upsertRequirement(requirement: RequirementEntity) = requirement.id
    }
}
