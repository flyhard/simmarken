package se.simmarken.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class KidAvatarColorsTest {
    @Test
    fun paletteHasExactlyTenEntries() {
        assertEquals(10, KidAvatarColors.palette.size)
    }

    @Test
    fun sameTrimmedNameMapsToSameColor() {
        val first = KidAvatarColors.defaultForName("Ella")
        val second = KidAvatarColors.defaultForName("  Ella  ")
        assertEquals(first, second)
    }
}
