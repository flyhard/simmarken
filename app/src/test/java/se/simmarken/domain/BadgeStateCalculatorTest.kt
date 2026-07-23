package se.simmarken.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import se.simmarken.domain.model.BadgeVisualState

class BadgeStateCalculatorTest {
    @Test
    fun gottenOverridesAll() {
        assertEquals(
            BadgeVisualState.GOTTEN,
            BadgeStateCalculator.compute(totalRequirements = 5, achievedCount = 0, isGotten = true),
        )
    }

    @Test
    fun allRequirementsAchieved_isAchievedToBuy() {
        assertEquals(
            BadgeVisualState.ACHIEVED_TO_BUY,
            BadgeStateCalculator.compute(totalRequirements = 3, achievedCount = 3, isGotten = false),
        )
    }

    @Test
    fun oneRequirement_isInProgress() {
        assertEquals(
            BadgeVisualState.IN_PROGRESS,
            BadgeStateCalculator.compute(totalRequirements = 3, achievedCount = 1, isGotten = false),
        )
    }

    @Test
    fun zeroAchieved_isLocked() {
        assertEquals(
            BadgeVisualState.LOCKED,
            BadgeStateCalculator.compute(totalRequirements = 3, achievedCount = 0, isGotten = false),
        )
    }

    @Test
    fun zeroRequirements_isAchievedToBuy() {
        assertEquals(
            BadgeVisualState.ACHIEVED_TO_BUY,
            BadgeStateCalculator.compute(totalRequirements = 0, achievedCount = 0, isGotten = false),
        )
    }

    @Test
    fun progressFraction_half() {
        assertEquals(0.5f, BadgeStateCalculator.progressFraction(achievedCount = 2, totalRequirements = 4))
    }

    @Test
    fun progressFraction_zeroTotal() {
        assertEquals(0f, BadgeStateCalculator.progressFraction(achievedCount = 0, totalRequirements = 0))
    }
}
