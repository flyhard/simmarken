package se.simmarken.domain

import se.simmarken.domain.model.BadgeVisualState

object BadgeStateCalculator {
    fun compute(totalRequirements: Int, achievedCount: Int, isGotten: Boolean): BadgeVisualState =
        BadgeVisualState.LOCKED

    fun progressFraction(achievedCount: Int, totalRequirements: Int): Float = 0f
}
