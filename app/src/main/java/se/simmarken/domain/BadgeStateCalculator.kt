package se.simmarken.domain

import se.simmarken.domain.model.BadgeVisualState

object BadgeStateCalculator {
    fun compute(totalRequirements: Int, achievedCount: Int, isGotten: Boolean): BadgeVisualState {
        if (isGotten) return BadgeVisualState.GOTTEN
        if (totalRequirements == 0 || achievedCount >= totalRequirements) {
            return BadgeVisualState.ACHIEVED_TO_BUY
        }
        if (achievedCount > 0) return BadgeVisualState.IN_PROGRESS
        return BadgeVisualState.LOCKED
    }

    fun progressFraction(achievedCount: Int, totalRequirements: Int): Float {
        if (totalRequirements <= 0) return 0f
        return achievedCount.toFloat() / totalRequirements
    }
}
