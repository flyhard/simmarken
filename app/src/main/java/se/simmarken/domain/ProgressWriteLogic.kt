package se.simmarken.domain

import se.simmarken.domain.model.BadgeVisualState

object ProgressWriteLogic {
    fun shouldSetAchievedAt(
        totalRequirements: Int,
        achievedCountAfterToggle: Int,
        existingAchievedAt: Long?,
    ): Boolean =
        totalRequirements > 0 &&
            achievedCountAfterToggle >= totalRequirements &&
            existingAchievedAt == null

    fun preserveAchievedAt(existing: Long?, proposed: Long?): Long? = existing ?: proposed

    fun shouldClearGottenOnUncheck(isGotten: Boolean, flippingToAchieved: Boolean): Boolean =
        isGotten && !flippingToAchieved
}
