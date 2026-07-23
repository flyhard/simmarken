package se.simmarken.domain

import se.simmarken.data.local.entity.BadgeEntity
import se.simmarken.data.local.entity.CategoryEntity
import se.simmarken.data.local.entity.RequirementEntity
import se.simmarken.domain.model.BadgeCellUiModel
import se.simmarken.domain.model.CategorySection

object BadgeCatalogMapper {
    fun toBadgeCellUiModel(
        badge: BadgeEntity,
        categoryCode: String,
        requirements: List<RequirementEntity>,
        requirementProgressById: Map<Long, Boolean>,
        isGotten: Boolean,
    ): BadgeCellUiModel {
        val totalRequirements = requirements.size
        val achievedCount = requirements.count { requirement ->
            requirementProgressById[requirement.id] == true
        }
        return BadgeCellUiModel(
            id = badge.id,
            nameSv = badge.nameSv,
            imageAssetPath = badge.imageAssetPath,
            categoryCode = categoryCode,
            visualState = BadgeStateCalculator.compute(
                totalRequirements = totalRequirements,
                achievedCount = achievedCount,
                isGotten = isGotten,
            ),
            progressFraction = BadgeStateCalculator.progressFraction(
                achievedCount = achievedCount,
                totalRequirements = totalRequirements,
            ),
            achievedCount = achievedCount,
            totalRequirements = totalRequirements,
        )
    }

    fun toCategorySection(
        category: CategoryEntity,
        badges: List<BadgeEntity>,
        allRequirements: List<RequirementEntity>,
        requirementProgressById: Map<Long, Boolean>,
        gottenByBadgeId: Map<Long, Boolean>,
    ): CategorySection {
        val requirementsByBadgeId = allRequirements.groupBy { it.badgeId }
        return CategorySection(
            categoryId = category.id,
            categoryNameSv = category.nameSv,
            categoryCode = category.code,
            sortOrder = category.sortOrder,
            badges = badges.map { badge ->
                toBadgeCellUiModel(
                    badge = badge,
                    categoryCode = category.code,
                    requirements = requirementsByBadgeId[badge.id].orEmpty(),
                    requirementProgressById = requirementProgressById,
                    isGotten = gottenByBadgeId[badge.id] == true,
                )
            },
        )
    }
}
