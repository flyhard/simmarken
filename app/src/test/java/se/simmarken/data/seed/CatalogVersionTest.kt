package se.simmarken.data.seed

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogVersionTest {
    @Test
    fun shouldMergeReturnsTrueOnFirstRun() {
        assertTrue(CatalogVersion.shouldMerge("2026.03.02", null))
    }

    @Test
    fun shouldMergeReturnsFalseWhenEqual() {
        assertFalse(CatalogVersion.shouldMerge("2026.03.02", "2026.03.02"))
    }

    @Test
    fun shouldMergeReturnsTrueWhenBundledNewer() {
        assertTrue(CatalogVersion.shouldMerge("2026.03.02", "2026.01.01"))
    }
}
