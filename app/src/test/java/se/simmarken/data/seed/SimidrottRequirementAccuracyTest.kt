package se.simmarken.data.seed

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SimidrottRequirementAccuracyTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val projectRoot: File by lazy { findProjectRoot() }

    private val seedFile: File by lazy {
        File(projectRoot, "app/src/main/assets/seed/simidrott.json").also {
            check(it.exists()) { "simidrott.json not found at ${it.absolutePath}" }
        }
    }

    private val sampleFixtureFile: File by lazy {
        File(projectRoot, "app/src/test/resources/seed/simidrott_sample.json").also {
            check(it.exists()) { "simidrott_sample.json not found at ${it.absolutePath}" }
        }
    }

    // Source: 1.baddaren protokoll.pdf / simmärkesaffisch 2024 bestämmelser
    private val expectedBaddarenGronCount = 2

    // Source: 1.baddaren protokoll.pdf / simmärkesaffisch 2024 bestämmelser
    private val expectedBaddarenGronFirstTextSv =
        "Doppa hakan och ena örat under vattnet. Upprepa fem gånger för varje sida."

    // Source: 6.hajen protokoll.pdf / simmärkesaffisch 2024 bestämmelser
    private val expectedHajenSilverCount = 2

    @Test
    fun sampleFixtureParsesCatalogCode() {
        val catalog = json.parseToJsonElement(sampleFixtureFile.readText()).jsonObject
        assertEquals("simidrott", catalog.getValue("code").jsonPrimitive.content)
    }

    @Test
    fun baddarenGronRequirementCountMatchesOfficial() {
        assertEquals(expectedBaddarenGronCount, requirementsForBadge("baddaren-gron").size)
    }

    @Test
    fun baddarenGronFirstRequirementTextMatchesOfficial() {
        val requirements = requirementsForBadge("baddaren-gron")
        assertEquals(expectedBaddarenGronFirstTextSv, requirements.first()["textSv"]!!.jsonPrimitive.content)
    }

    @Test
    fun hajenSilverRequirementCountMatchesOfficial() {
        assertEquals(expectedHajenSilverCount, requirementsForBadge("hajen-silver").size)
    }

    @Test
    fun hajenSilverRequirementsOrderedBySortOrder() {
        val sortOrders = requirementsForBadge("hajen-silver").map { it["sortOrder"]!!.jsonPrimitive.content.toInt() }
        assertEquals(sortOrders, sortOrders.sorted())
        assertTrue(sortOrders.zipWithNext().all { (left, right) -> left < right })
    }

    private fun requirementsForBadge(badgeCode: String): List<kotlinx.serialization.json.JsonObject> {
        val catalog = json.parseToJsonElement(seedFile.readText()).jsonObject
        val categories = catalog.getValue("categories").jsonArray
        for (category in categories) {
            val badges = category.jsonObject.getValue("badges").jsonArray
            for (badge in badges) {
                val badgeObject = badge.jsonObject
                if (badgeObject.getValue("code").jsonPrimitive.content == badgeCode) {
                    return badgeObject.getValue("requirements").jsonArray.map { it.jsonObject }
                }
            }
        }
        error("Badge not found: $badgeCode")
    }

    private fun findProjectRoot(): File {
        var dir = File(checkNotNull(System.getProperty("user.dir")))
        while (true) {
            if (File(dir, "settings.gradle.kts").exists()) {
                return dir
            }
            val parent = dir.parentFile ?: break
            dir = parent
        }
        error("Could not locate project root from ${System.getProperty("user.dir")}")
    }
}
