package se.simmarken.ui.child

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BadgeGridItemChromeTest {
    private fun badgeGridItemSource(): File {
        val candidates = listOf(
            File("src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt"),
            File("app/src/main/java/se/simmarken/ui/child/components/BadgeGridItem.kt"),
        )
        return candidates.firstOrNull { it.exists() }
            ?: error("Could not locate BadgeGridItem.kt from working dir: ${File(".").absolutePath}")
    }

    @Test
    fun badgeGridItem_usesStringResources_notHardcodedSwedishStateLiterals() {
        val source = badgeGridItemSource().readText()

        val hardcodedSwedish = Regex(""""(låst|pågår|klar att köpa|köpt)"""")
        assertFalse(
            "BadgeGridItem must not hardcode Swedish state literals",
            hardcodedSwedish.containsMatchIn(source),
        )

        assertTrue(source.contains("R.string.badge_pin_content_description"))
        assertTrue(source.contains("R.string.badge_state_locked"))
        assertTrue(source.contains("R.string.badge_state_in_progress"))
        assertTrue(source.contains("R.string.badge_state_achieved_to_buy"))
        assertTrue(source.contains("R.string.badge_state_gotten"))
        assertTrue(source.contains("stringResource("))
        assertTrue(source.contains("): Int = when"))
        assertFalse(source.contains("): String = when"))
    }
}
