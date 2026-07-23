package se.simmarken.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressWriteLogicTest {
    @Test
    fun lastRequirement_setsAchievedAtOnce() {
        assertTrue(
            ProgressWriteLogic.shouldSetAchievedAt(
                totalRequirements = 3,
                achievedCountAfterToggle = 3,
                existingAchievedAt = null,
            ),
        )
    }

    @Test
    fun uncheckAfterAchieved_retainsAchievedAt() {
        val existing = 1_700_000_000_000L
        assertEquals(
            existing,
            ProgressWriteLogic.preserveAchievedAt(existing, 9_999L),
        )
        assertFalse(
            ProgressWriteLogic.shouldSetAchievedAt(
                totalRequirements = 3,
                achievedCountAfterToggle = 2,
                existingAchievedAt = existing,
            ),
        )
    }

    @Test
    fun uncheckWhileGotten_clearsGotten() {
        assertTrue(
            ProgressWriteLogic.shouldClearGottenOnUncheck(
                isGotten = true,
                flippingToAchieved = false,
            ),
        )
        assertFalse(
            ProgressWriteLogic.shouldClearGottenOnUncheck(
                isGotten = true,
                flippingToAchieved = true,
            ),
        )
    }

    @Test
    fun uncheckAll_returnsLocked_predicate() {
        assertFalse(
            ProgressWriteLogic.shouldSetAchievedAt(
                totalRequirements = 3,
                achievedCountAfterToggle = 0,
                existingAchievedAt = 1L,
            ),
        )
        assertEquals(1L, ProgressWriteLogic.preserveAchievedAt(existing = 1L, proposed = null))
    }
}
