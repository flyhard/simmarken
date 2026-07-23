package se.simmarken.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity

class KidProgressSummaryCalculatorTest {
    @Test
    fun oneInProgress_countsOne() {
        val category = category(id = 1L, catalogId = 10L, code = "sim")
        val badge = badge(id = 100L, categoryId = category.id, code = "crawl")
        val requirements = listOf(
            requirement(id = 1001L, badgeId = badge.id, sortOrder = 0),
            requirement(id = 1002L, badgeId = badge.id, sortOrder = 1),
        )
        val requirementProgressById = mapOf(1001L to true, 1002L to false)

        val summary = KidProgressSummaryCalculator.compute(
            badges = listOf(badge),
            categoriesById = mapOf(category.id to category),
            allRequirements = requirements,
            requirementProgressById = requirementProgressById,
            gottenByBadgeId = emptyMap(),
        )

        assertEquals(1, summary.inProgressCount)
        assertEquals(0, summary.toBuyCount)
    }

    @Test
    fun achievedToBuy_countsToBuy() {
        val category = category(id = 1L, catalogId = 10L, code = "sim")
        val badge = badge(id = 100L, categoryId = category.id, code = "crawl")
        val requirements = listOf(
            requirement(id = 1001L, badgeId = badge.id, sortOrder = 0),
            requirement(id = 1002L, badgeId = badge.id, sortOrder = 1),
        )
        val requirementProgressById = mapOf(1001L to true, 1002L to true)

        val summary = KidProgressSummaryCalculator.compute(
            badges = listOf(badge),
            categoriesById = mapOf(category.id to category),
            allRequirements = requirements,
            requirementProgressById = requirementProgressById,
            gottenByBadgeId = emptyMap(),
        )

        assertEquals(0, summary.inProgressCount)
        assertEquals(1, summary.toBuyCount)
    }

    @Test
    fun gotten_excluded() {
        val category = category(id = 1L, catalogId = 10L, code = "sim")
        val badge = badge(id = 100L, categoryId = category.id, code = "crawl")
        val requirements = listOf(
            requirement(id = 1001L, badgeId = badge.id, sortOrder = 0),
            requirement(id = 1002L, badgeId = badge.id, sortOrder = 1),
        )
        val requirementProgressById = mapOf(1001L to true, 1002L to true)

        val summary = KidProgressSummaryCalculator.compute(
            badges = listOf(badge),
            categoriesById = mapOf(category.id to category),
            allRequirements = requirements,
            requirementProgressById = requirementProgressById,
            gottenByBadgeId = mapOf(badge.id to true),
        )

        assertEquals(0, summary.inProgressCount)
        assertEquals(0, summary.toBuyCount)
    }

    @Test
    fun locked_excluded() {
        val category = category(id = 1L, catalogId = 10L, code = "sim")
        val badge = badge(id = 100L, categoryId = category.id, code = "crawl")
        val requirements = listOf(
            requirement(id = 1001L, badgeId = badge.id, sortOrder = 0),
            requirement(id = 1002L, badgeId = badge.id, sortOrder = 1),
        )

        val summary = KidProgressSummaryCalculator.compute(
            badges = listOf(badge),
            categoriesById = mapOf(category.id to category),
            allRequirements = requirements,
            requirementProgressById = emptyMap(),
            gottenByBadgeId = emptyMap(),
        )

        assertEquals(0, summary.inProgressCount)
        assertEquals(0, summary.toBuyCount)
    }

    @Test
    fun bothCatalogs_summed() {
        val simCategory = category(id = 1L, catalogId = 10L, code = "sim", sortOrder = 0)
        val slsCategory = category(id = 2L, catalogId = 20L, code = "sls", sortOrder = 0)
        val simBadge = badge(id = 100L, categoryId = simCategory.id, code = "crawl", sortOrder = 0)
        val slsBadge = badge(id = 200L, categoryId = slsCategory.id, code = "droppen", sortOrder = 0)
        val simRequirements = listOf(
            requirement(id = 1001L, badgeId = simBadge.id, sortOrder = 0),
            requirement(id = 1002L, badgeId = simBadge.id, sortOrder = 1),
        )
        val slsRequirements = listOf(
            requirement(id = 2001L, badgeId = slsBadge.id, sortOrder = 0),
            requirement(id = 2002L, badgeId = slsBadge.id, sortOrder = 1),
        )

        val summary = KidProgressSummaryCalculator.compute(
            badges = listOf(simBadge, slsBadge),
            categoriesById = mapOf(simCategory.id to simCategory, slsCategory.id to slsCategory),
            allRequirements = simRequirements + slsRequirements,
            requirementProgressById = mapOf(
                1001L to true,
                1002L to false,
                2001L to true,
                2002L to false,
            ),
            gottenByBadgeId = emptyMap(),
        )

        assertEquals(2, summary.inProgressCount)
        assertEquals(0, summary.toBuyCount)
    }

    private fun category(
        id: Long,
        catalogId: Long,
        code: String,
        sortOrder: Int = 0,
    ) = CategoryEntity(
        id = id,
        catalogId = catalogId,
        code = code,
        nameSv = code,
        nameEn = code,
        sortOrder = sortOrder,
    )

    private fun badge(
        id: Long,
        categoryId: Long,
        code: String,
        sortOrder: Int = 0,
    ) = BadgeEntity(
        id = id,
        categoryId = categoryId,
        code = code,
        nameSv = code,
        nameEn = code,
        imageAssetPath = null,
        sortOrder = sortOrder,
    )

    private fun requirement(
        id: Long,
        badgeId: Long,
        sortOrder: Int,
    ) = RequirementEntity(
        id = id,
        badgeId = badgeId,
        code = "req_$id",
        textSv = "Krav $id",
        textEn = "Req $id",
        sortOrder = sortOrder,
    )
}
