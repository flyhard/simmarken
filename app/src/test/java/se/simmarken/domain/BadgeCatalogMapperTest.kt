package se.simmarken.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.domain.model.BadgeVisualState

class BadgeCatalogMapperTest {
    private fun badge(id: Long = 1L) =
        BadgeEntity(
            id = id,
            categoryId = 10L,
            code = "badge-$id",
            nameSv = "Badge $id",
            nameEn = "Badge $id",
            imageAssetPath = null,
            sortOrder = 0,
        )

    private fun requirement(id: Long, badgeId: Long = 1L) =
        RequirementEntity(
            id = id,
            badgeId = badgeId,
            code = "req-$id",
            textSv = "Req $id",
            textEn = "Req $id",
            sortOrder = id.toInt(),
        )

    @Test
    fun freshKid_allLocked() {
        val requirements = listOf(requirement(1), requirement(2), requirement(3))
        val result =
            BadgeCatalogMapper.toBadgeCellUiModel(
                badge = badge(),
                categoryCode = "sim",
                requirements = requirements,
                requirementProgressById = emptyMap(),
                isGotten = false,
            )

        assertEquals(BadgeVisualState.LOCKED, result.visualState)
        assertEquals(0, result.achievedCount)
        assertEquals(0f, result.progressFraction)
        assertEquals(3, result.totalRequirements)
    }

    @Test
    fun oneRequirement_inProgress() {
        val requirements = listOf(requirement(1), requirement(2), requirement(3))
        val result =
            BadgeCatalogMapper.toBadgeCellUiModel(
                badge = badge(),
                categoryCode = "sim",
                requirements = requirements,
                requirementProgressById = mapOf(1L to true),
                isGotten = false,
            )

        assertEquals(BadgeVisualState.IN_PROGRESS, result.visualState)
        assertEquals(1, result.achievedCount)
    }

    @Test
    fun allAchieved_cartState() {
        val requirements = listOf(requirement(1), requirement(2), requirement(3))
        val result =
            BadgeCatalogMapper.toBadgeCellUiModel(
                badge = badge(),
                categoryCode = "sim",
                requirements = requirements,
                requirementProgressById = mapOf(1L to true, 2L to true, 3L to true),
                isGotten = false,
            )

        assertEquals(BadgeVisualState.ACHIEVED_TO_BUY, result.visualState)
        assertEquals(3, result.achievedCount)
    }

    @Test
    fun gottenOverrides() {
        val requirements = listOf(requirement(1), requirement(2), requirement(3))
        val result =
            BadgeCatalogMapper.toBadgeCellUiModel(
                badge = badge(),
                categoryCode = "sim",
                requirements = requirements,
                requirementProgressById = emptyMap(),
                isGotten = true,
            )

        assertEquals(BadgeVisualState.GOTTEN, result.visualState)
    }

    @Test
    fun absentProgressRow_defaultsFalse() {
        val requirements = listOf(requirement(1), requirement(2))
        val result =
            BadgeCatalogMapper.toBadgeCellUiModel(
                badge = badge(),
                categoryCode = "sim",
                requirements = requirements,
                requirementProgressById = mapOf(99L to true),
                isGotten = false,
            )

        assertEquals(BadgeVisualState.LOCKED, result.visualState)
        assertEquals(0, result.achievedCount)
    }

    @Test
    fun categorySection_preservesSortOrder() {
        val category =
            CategoryEntity(
                id = 10L,
                catalogId = 1L,
                code = "sim",
                nameSv = "Simidrott",
                nameEn = "Simidrott",
                sortOrder = 5,
            )
        val section =
            BadgeCatalogMapper.toCategorySection(
                category = category,
                badges = listOf(badge()),
                allRequirements = listOf(requirement(1)),
                requirementProgressById = emptyMap(),
                gottenByBadgeId = emptyMap(),
            )

        assertEquals(5, section.sortOrder)
        assertEquals("sim", section.categoryCode)
        assertEquals(1, section.badges.size)
    }
}
