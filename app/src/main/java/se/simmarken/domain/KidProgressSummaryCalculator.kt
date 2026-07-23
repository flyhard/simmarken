package se.simmarken.domain

import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.domain.model.BadgeVisualState
import se.simmarken.domain.model.KidProgressSummary

object KidProgressSummaryCalculator {
    fun compute(
        badges: List<BadgeEntity>,
        categoriesById: Map<Long, CategoryEntity>,
        allRequirements: List<RequirementEntity>,
        requirementProgressById: Map<Long, Boolean>,
        gottenByBadgeId: Map<Long, Boolean>,
    ): KidProgressSummary {
        val requirementsByBadgeId = allRequirements.groupBy { it.badgeId }
        var inProgressCount = 0
        var toBuyCount = 0

        for (badge in badges) {
            val category = categoriesById[badge.categoryId] ?: continue
            val cell = BadgeCatalogMapper.toBadgeCellUiModel(
                badge = badge,
                categoryCode = category.code,
                requirements = requirementsByBadgeId[badge.id].orEmpty(),
                requirementProgressById = requirementProgressById,
                isGotten = gottenByBadgeId[badge.id] == true,
            )
            when (cell.visualState) {
                BadgeVisualState.IN_PROGRESS -> inProgressCount++
                BadgeVisualState.ACHIEVED_TO_BUY -> toBuyCount++
                BadgeVisualState.LOCKED, BadgeVisualState.GOTTEN -> Unit
            }
        }

        return KidProgressSummary(
            kidId = 0,
            inProgressCount = inProgressCount,
            toBuyCount = toBuyCount,
        )
    }
}
