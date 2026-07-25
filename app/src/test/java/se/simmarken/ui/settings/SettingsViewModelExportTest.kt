package se.simmarken.ui.settings

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import se.simmarken.data.export.BackupDto
import se.simmarken.data.export.ExportRepository
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.KidEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.data.local.entity.RequirementProgressEntity
import se.simmarken.domain.repository.KidRepository
import se.simmarken.domain.repository.ProgressRepository
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelExportTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun singleKid_skipsPicker() = runTest {
        val kid = KidEntity(
            id = 1L,
            stableId = "kid-1",
            name = "Ella",
            avatarColorArgb = 0xFF2196F3.toInt(),
            createdAtEpochMillis = 1L,
            sortOrder = 0,
        )
        val exportRepository = RecordingExportRepository()
        val viewModel = SettingsViewModel(
            application = RuntimeEnvironment.getApplication(),
            kidRepository = FakeKidRepository(kid),
            exportRepository = exportRepository,
            ioDispatcher = testDispatcher,
        )

        val shareCollector = launch { viewModel.shareExportUri.collect { } }
        val stateCollector = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.kids.size)

        viewModel.onExportClicked()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showExportKidPicker)
        assertTrue(exportRepository.exportCalled)
        shareCollector.cancel()
        stateCollector.cancel()
    }

    private class FakeKidRepository(
        private val kid: KidEntity,
    ) : KidRepository {
        override fun observeAll() = flowOf(listOf(kid))
        override fun observeById(kidId: Long) = flowOf(if (kid.id == kidId) kid else null)
        override suspend fun findByStableId(stableId: String) =
            if (kid.stableId == stableId) kid else null
        override suspend fun findByIds(ids: List<Long>) = listOf(kid).filter { it.id in ids }
        override suspend fun upsert(kid: KidEntity) = kid.id
        override suspend fun delete(kidId: Long) = Unit
    }

    private class RecordingExportRepository : ExportRepository(
        kidRepository = StubKidRepository(),
        progressRepository = StubProgressRepository(),
        catalogDao = StubCatalogDao(),
    ) {
        var exportCalled = false

        override suspend fun buildBackup(selectedKidIds: List<Long>): BackupDto {
            exportCalled = true
            return BackupDto(exportedAtEpochMillis = 1L, kids = emptyList())
        }

        override suspend fun writeExportFile(context: android.content.Context, dto: BackupDto): File {
            val file = File(context.cacheDir, "exports/test.json")
            file.parentFile?.mkdirs()
            file.writeText("{}")
            return file
        }

        override fun fileToShareUri(context: android.content.Context, file: File): Uri =
            Uri.parse("content://se.simmarken.test/exports/test.json")
    }

    private class StubKidRepository : KidRepository {
        override fun observeAll() = flowOf(emptyList<KidEntity>())
        override fun observeById(kidId: Long) = flowOf<KidEntity?>(null)
        override suspend fun findByStableId(stableId: String) = null
        override suspend fun findByIds(ids: List<Long>) = emptyList<KidEntity>()
        override suspend fun upsert(kid: KidEntity) = 0L
        override suspend fun delete(kidId: Long) = Unit
    }

    private class StubProgressRepository : ProgressRepository {
        override fun observeRequirementProgress(kidId: Long) =
            flowOf(emptyList<RequirementProgressEntity>())
        override fun observeBadgeProgress(kidId: Long) = flowOf(emptyList<BadgeProgressEntity>())
        override suspend fun upsertRequirementProgress(progress: RequirementProgressEntity) = Unit
        override suspend fun upsertBadgeProgress(progress: BadgeProgressEntity) = Unit
        override suspend fun applyRequirementToggle(
            requirementProgress: RequirementProgressEntity,
            badgeProgress: BadgeProgressEntity?,
        ) = Unit
        override suspend fun getRequirementProgressForKids(kidIds: List<Long>) =
            emptyList<RequirementProgressEntity>()
        override suspend fun getBadgeProgressForKids(kidIds: List<Long>) =
            emptyList<BadgeProgressEntity>()
    }

    private class StubCatalogDao : se.simmarken.data.local.dao.CatalogDao {
        override fun observeCatalogs() = flowOf(emptyList<CatalogEntity>())
        override fun observeCategories(catalogId: Long) = flowOf(emptyList<CategoryEntity>())
        override fun observeBadges(categoryId: Long) = flowOf(emptyList<BadgeEntity>())
        override fun observeRequirements(badgeId: Long) = flowOf(emptyList<RequirementEntity>())
        override fun observeAllRequirements() = flowOf(emptyList<RequirementEntity>())
        override fun observeAllBadges() = flowOf(emptyList<BadgeEntity>())
        override fun observeAllCategories() = flowOf(emptyList<CategoryEntity>())
        override fun observeBadgeById(badgeId: Long) = flowOf<BadgeEntity?>(null)
        override fun observeCategoryById(categoryId: Long) = flowOf<CategoryEntity?>(null)
        override suspend fun findCatalogByCode(code: String) = null
        override suspend fun findCategoryByCatalogAndCode(catalogId: Long, code: String) = null
        override suspend fun findBadgeByCategoryAndCode(categoryId: Long, code: String) = null
        override suspend fun findBadgeByCatalogAndCode(catalogId: Long, code: String) = null
        override suspend fun listCategoriesForCatalog(catalogId: Long) = emptyList<CategoryEntity>()
        override suspend fun deleteCategoryById(categoryId: Long) = Unit
        override suspend fun deleteBadgeById(badgeId: Long) = Unit
        override suspend fun findRequirementByBadgeAndCode(badgeId: Long, code: String) = null
        override suspend fun findCategoryById(categoryId: Long) = null
        override suspend fun listBadgesForCategory(categoryId: Long) = emptyList<BadgeEntity>()
        override suspend fun listCatalogs() = emptyList<CatalogEntity>()
        override suspend fun listCategories() = emptyList<CategoryEntity>()
        override suspend fun listBadges() = emptyList<BadgeEntity>()
        override suspend fun listRequirements() = emptyList<RequirementEntity>()
        override suspend fun findRequirementById(requirementId: Long) = null
        override suspend fun findBadgeById(badgeId: Long) = null
        override suspend fun countCatalogs() = 0
        override suspend fun countCategories() = 0
        override suspend fun upsertCatalog(catalog: CatalogEntity) = 0L
        override suspend fun upsertCategory(category: CategoryEntity) = 0L
        override suspend fun upsertBadge(badge: BadgeEntity) = 0L
        override suspend fun upsertRequirement(requirement: RequirementEntity) = 0L
    }
}
