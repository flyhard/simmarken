package se.simmarken.data.seed

import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogSeedParserTest {
    @Test
    fun parseSimidrottSampleFixture() {
        val json = javaClass.classLoader
            ?.getResource("seed/simidrott_sample.json")
            ?.readText()
            ?: error("Missing test fixture seed/simidrott_sample.json")

        val seed = CatalogSeedParser.loadFromString(json)

        assertEquals("simidrott", seed.code)
        assertEquals(2, seed.categories.size)
    }
}
