package se.simmarken.ui.badge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.BadgeProgressEntity
import se.simmarken.data.local.entity.CatalogEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.data.local.entity.RequirementProgressEntity
import se.simmarken.domain.model.BadgeVisualState
import se.simmarken.domain.repository.CatalogRepository
import se.simmarken.domain.repository.ProgressRepository

@OptIn(ExperimentalCoroutinesApi::class)
class BadgeDetailViewModelProgressTest {
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
    fun toggleFirstRequirement_lockedToInProgress() = runTest {
        val catalogRepo = FakeCatalogRepository()
        val progressRepo = FakeProgressRepository()
        val kidId = 1L
        val badgeId = 10L
        val requirements = listOf(
            requirement(id = 101L, badgeId = badgeId, sortOrder = 0, text = "Req 1"),
            requirement(id = 102L, badgeId = badgeId, sortOrder = 1, text = "Req 2"),
            requirement(id = 103L, badgeId = badgeId, sortOrder = 2, text = "Req 3"),
        )
        catalogRepo.seedBadgeDetail(
            badgeId = badgeId,
            categoryId = 5L,
            requirements = requirements,
        )

        val viewModel = BadgeDetailViewModel(
            kidId = kidId,
            badgeId = badgeId,
            catalogRepository = catalogRepo,
            progressRepository = progressRepo,
            ioDispatcher = testDispatcher,
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(BadgeVisualState.LOCKED, viewModel.uiState.value.visualState)
        assertEquals(0, viewModel.uiState.value.achievedCount)

        viewModel.toggleRequirement(101L)
        advanceUntilIdle()

        val afterToggle = viewModel.uiState.value
        assertEquals(BadgeVisualState.IN_PROGRESS, afterToggle.visualState)
        assertEquals(1, afterToggle.achievedCount)
        assertTrue(afterToggle.requirements.first { it.id == 101L }.isAchieved)

        collector.cancel()
    }

    @Test
    fun toggleRequirement_persistsUpsert() = runTest {
        val catalogRepo = FakeCatalogRepository()
        val progressRepo = FakeProgressRepository()
        val kidId = 1L
        val badgeId = 10L
        catalogRepo.seedBadgeDetail(
            badgeId = badgeId,
            categoryId = 5L,
            requirements = listOf(
                requirement(id = 101L, badgeId = badgeId, sortOrder = 0, text = "Req 1"),
            ),
        )

        val viewModel = BadgeDetailViewModel(
            kidId = kidId,
            badgeId = badgeId,
            catalogRepository = catalogRepo,
            progressRepository = progressRepo,
            ioDispatcher = testDispatcher,
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.toggleRequirement(101L)
        advanceUntilIdle()

        val upsert = progressRepo.lastRequirementUpsert
        assertNotNull(upsert)
        assertEquals(kidId, upsert!!.kidId)
        assertEquals(101L, upsert.requirementId)
        assertTrue(upsert.isAchieved)
        assertNotNull(upsert.achievedAtEpochMillis)

        collector.cancel()
    }

    @Test
    fun uiState_mapsRequirements() = runTest {
        val catalogRepo = FakeCatalogRepository()
        val progressRepo = FakeProgressRepository()
        val kidId = 1L
        val badgeId = 10L
        val requirements = listOf(
            requirement(id = 102L, badgeId = badgeId, sortOrder = 1, text = "Second"),
            requirement(id = 101L, badgeId = badgeId, sortOrder = 0, text = "First"),
        )
        catalogRepo.seedBadgeDetail(
            badgeId = badgeId,
            categoryId = 5L,
            requirements = requirements,
        )
        progressRepo.setRequirementProgress(
            listOf(
                RequirementProgressEntity(
                    kidId = kidId,
                    requirementId = 101L,
                    isAchieved = true,
                    achievedAtEpochMillis = 1L,
                ),
            ),
        )

        val viewModel = BadgeDetailViewModel(
            kidId = kidId,
            badgeId = badgeId,
            catalogRepository = catalogRepo,
            progressRepository = progressRepo,
            ioDispatcher = testDispatcher,
        )

        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val rows = viewModel.uiState.value.requirements
        assertEquals(listOf(101L, 102L), rows.map { it.id })
        assertEquals("First", rows[0].textSv)
        assertTrue(rows[0].isAchieved)
        assertEquals("Second", rows[1].textSv)
        assertEquals(false, rows[1].isAchieved)

        collector.cancel()
    }

    @Test
    fun lastRequirement_setsAchievedToBuy() = runTest {
        val catalogRepo = FakeCatalogRepository()
        val progressRepo = FakeProgressRepository()
        val kidId = 1L
        val badgeId = 10L
        val requirements = listOf(
            requirement(id = 101L, badgeId = badgeId, sortOrder = 0, text = "Req 1"),
            requirement(id = 102L, badgeId = badgeId, sortOrder = 1, text = "Req 2"),
        )
        catalogRepo.seedBadgeDetail(badgeId, categoryId = 5L, requirements = requirements)
        progressRepo.setRequirementProgress(
            listOf(
                RequirementProgressEntity(kidId, 101L, isAchieved = true, achievedAtEpochMillis = 1L),
            ),
        )

        val viewModel = BadgeDetailViewModel(
            kidId = kidId,
            badgeId = badgeId,
            catalogRepository = catalogRepo,
            progressRepository = progressRepo,
            ioDispatcher = testDispatcher,
        )
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.toggleRequirement(102L)
        advanceUntilIdle()

        assertEquals(BadgeVisualState.ACHIEVED_TO_BUY, viewModel.uiState.value.visualState)
        assertNotNull(progressRepo.lastBadgeUpsert?.achievedAtEpochMillis)

        collector.cancel()
    }

    @Test
    fun uncheckWhileGotten_clearsGotten() = runTest {
        val catalogRepo = FakeCatalogRepository()
        val progressRepo = FakeProgressRepository()
        val kidId = 1L
        val badgeId = 10L
        catalogRepo.seedBadgeDetail(
            badgeId = badgeId,
            categoryId = 5L,
            requirements = listOf(
                requirement(id = 101L, badgeId = badgeId, sortOrder = 0, text = "Req 1"),
            ),
        )
        progressRepo.setRequirementProgress(
            listOf(
                RequirementProgressEntity(kidId, 101L, isAchieved = true, achievedAtEpochMillis = 1L),
            ),
        )
        progressRepo.setBadgeProgress(
            listOf(
                BadgeProgressEntity(
                    kidId = kidId,
                    badgeId = badgeId,
                    isGotten = true,
                    achievedAtEpochMillis = 1L,
                    gottenAtEpochMillis = 2L,
                ),
            ),
        )

        val viewModel = BadgeDetailViewModel(
            kidId = kidId,
            badgeId = badgeId,
            catalogRepository = catalogRepo,
            progressRepository = progressRepo,
            ioDispatcher = testDispatcher,
        )
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(BadgeVisualState.GOTTEN, viewModel.uiState.value.visualState)

        viewModel.toggleRequirement(101L)
        advanceUntilIdle()

        assertEquals(BadgeVisualState.LOCKED, viewModel.uiState.value.visualState)
        assertEquals(false, progressRepo.lastBadgeUpsert?.isGotten)
        assertEquals(null, progressRepo.lastBadgeUpsert?.gottenAtEpochMillis)
        assertNotNull(progressRepo.lastBadgeUpsert?.achievedAtEpochMillis)

        collector.cancel()
    }

    @Test
    fun setGotten_true_upsertsGottenFields() = runTest {
        val catalogRepo = FakeCatalogRepository()
        val progressRepo = FakeProgressRepository()
        val kidId = 1L
        val badgeId = 10L
        catalogRepo.seedBadgeDetail(
            badgeId = badgeId,
            categoryId = 5L,
            requirements = emptyList(),
        )
        progressRepo.setBadgeProgress(
            listOf(
                BadgeProgressEntity(
                    kidId = kidId,
                    badgeId = badgeId,
                    achievedAtEpochMillis = 1L,
                ),
            ),
        )

        val viewModel = BadgeDetailViewModel(
            kidId = kidId,
            badgeId = badgeId,
            catalogRepository = catalogRepo,
            progressRepository = progressRepo,
            ioDispatcher = testDispatcher,
        )
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.setGotten(true)
        advanceUntilIdle()

        assertTrue(progressRepo.lastBadgeUpsert!!.isGotten)
        assertNotNull(progressRepo.lastBadgeUpsert!!.gottenAtEpochMillis)
        assertEquals(BadgeVisualState.GOTTEN, viewModel.uiState.value.visualState)

        collector.cancel()
    }

    @Test
    fun confirmClearGotten_clearsGottenRetainsAchievedAt() = runTest {
        val catalogRepo = FakeCatalogRepository()
        val progressRepo = FakeProgressRepository()
        val kidId = 1L
        val badgeId = 10L
        catalogRepo.seedBadgeDetail(
            badgeId = badgeId,
            categoryId = 5L,
            requirements = emptyList(),
        )
        progressRepo.setBadgeProgress(
            listOf(
                BadgeProgressEntity(
                    kidId = kidId,
                    badgeId = badgeId,
                    isGotten = true,
                    achievedAtEpochMillis = 42L,
                    gottenAtEpochMillis = 99L,
                ),
            ),
        )

        val viewModel = BadgeDetailViewModel(
            kidId = kidId,
            badgeId = badgeId,
            catalogRepository = catalogRepo,
            progressRepository = progressRepo,
            ioDispatcher = testDispatcher,
        )
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.requestClearGotten()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showUncheckPurchaseDialog)

        viewModel.confirmClearGotten()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showUncheckPurchaseDialog)
        assertEquals(false, progressRepo.lastBadgeUpsert?.isGotten)
        assertEquals(null, progressRepo.lastBadgeUpsert?.gottenAtEpochMillis)
        assertEquals(42L, progressRepo.lastBadgeUpsert?.achievedAtEpochMillis)

        collector.cancel()
    }

    @Test
    fun isPurchaseEnabled_trueForZeroRequirementBadge() = runTest {
        val catalogRepo = FakeCatalogRepository()
        val progressRepo = FakeProgressRepository()
        catalogRepo.seedBadgeDetail(
            badgeId = 10L,
            categoryId = 5L,
            requirements = emptyList(),
        )

        val viewModel = BadgeDetailViewModel(
            kidId = 1L,
            badgeId = 10L,
            catalogRepository = catalogRepo,
            progressRepository = progressRepo,
            ioDispatcher = testDispatcher,
        )
        val collector = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(BadgeVisualState.ACHIEVED_TO_BUY, viewModel.uiState.value.visualState)
        assertTrue(viewModel.uiState.value.isPurchaseEnabled)

        collector.cancel()
    }

    private fun requirement(
        id: Long,
        badgeId: Long,
        sortOrder: Int,
        text: String,
    ) = RequirementEntity(
        id = id,
        badgeId = badgeId,
        code = "req_$id",
        textSv = text,
        textEn = text,
        sortOrder = sortOrder,
    )

    private class FakeCatalogRepository : CatalogRepository {
        private val catalogs = MutableStateFlow<List<CatalogEntity>>(emptyList())
        private val categories = MutableStateFlow<List<CategoryEntity>>(emptyList())
        private val badges = MutableStateFlow<List<BadgeEntity>>(emptyList())
        private val badgeById = MutableStateFlow<BadgeEntity?>(null)
        private val categoryById = MutableStateFlow<CategoryEntity?>(null)
        private val requirementsForBadge = MutableStateFlow<List<RequirementEntity>>(emptyList())

        fun seedBadgeDetail(
            badgeId: Long,
            categoryId: Long,
            requirements: List<RequirementEntity>,
        ) {
            val category = CategoryEntity(
                id = categoryId,
                catalogId = 1L,
                code = "test",
                nameSv = "Test",
                nameEn = "Test",
                sortOrder = 0,
            )
            val badge = BadgeEntity(
                id = badgeId,
                categoryId = categoryId,
                code = "badge",
                nameSv = "Test Badge",
                nameEn = "Test Badge",
                imageAssetPath = null,
                sortOrder = 0,
            )
            categories.value = listOf(category)
            badges.value = listOf(badge)
            categoryById.value = category
            badgeById.value = badge
            requirementsForBadge.value = requirements
        }

        override fun observeCatalogs(): Flow<List<CatalogEntity>> = catalogs

        override fun observeCategories(catalogId: Long): Flow<List<CategoryEntity>> = categories

        override fun observeBadges(categoryId: Long): Flow<List<BadgeEntity>> = badges

        override fun observeRequirements(badgeId: Long): Flow<List<RequirementEntity>> =
            requirementsForBadge

        override fun observeAllRequirements(): Flow<List<RequirementEntity>> = requirementsForBadge

        override fun observeBadgeById(badgeId: Long): Flow<BadgeEntity?> = badgeById

        override fun observeCategoryById(categoryId: Long): Flow<CategoryEntity?> = categoryById

        override suspend fun upsertCatalog(catalog: CatalogEntity): Long = catalog.id

        override suspend fun upsertCategory(category: CategoryEntity): Long = category.id

        override suspend fun upsertBadge(badge: BadgeEntity): Long = badge.id

        override suspend fun upsertRequirement(requirement: RequirementEntity): Long = requirement.id
    }

    private class FakeProgressRepository : ProgressRepository {
        private val requirementProgress = MutableStateFlow<List<RequirementProgressEntity>>(emptyList())
        private val badgeProgress = MutableStateFlow<List<BadgeProgressEntity>>(emptyList())
        var lastRequirementUpsert: RequirementProgressEntity? = null
        var lastBadgeUpsert: BadgeProgressEntity? = null

        fun setRequirementProgress(progress: List<RequirementProgressEntity>) {
            requirementProgress.value = progress
        }

        fun setBadgeProgress(progress: List<BadgeProgressEntity>) {
            badgeProgress.value = progress
        }

        override fun observeRequirementProgress(kidId: Long): Flow<List<RequirementProgressEntity>> =
            requirementProgress

        override fun observeBadgeProgress(kidId: Long): Flow<List<BadgeProgressEntity>> =
            badgeProgress

        override suspend fun upsertRequirementProgress(progress: RequirementProgressEntity) {
            lastRequirementUpsert = progress
            val updated = requirementProgress.value
                .filterNot { it.kidId == progress.kidId && it.requirementId == progress.requirementId } +
                progress
            requirementProgress.value = updated
        }

        override suspend fun upsertBadgeProgress(progress: BadgeProgressEntity) {
            lastBadgeUpsert = progress
            val updated = badgeProgress.value
                .filterNot { it.kidId == progress.kidId && it.badgeId == progress.badgeId } +
                progress
            badgeProgress.value = updated
        }
    }
}
