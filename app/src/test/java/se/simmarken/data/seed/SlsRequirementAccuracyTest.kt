package se.simmarken.data.seed

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SlsRequirementAccuracyTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val projectRoot: File by lazy { findProjectRoot() }

    private val seedFile: File by lazy {
        File(projectRoot, "app/src/main/assets/seed/sls.json").also {
            check(it.exists()) { "sls.json not found at ${it.absolutePath}" }
        }
    }

    // Source: https://shop.svenskalivraddningssallskapet.se/products/droppen
    private val expectedDroppenCount = 1

    // Source: https://shop.svenskalivraddningssallskapet.se/products/droppen
    private val expectedDroppenFirstTextSv =
        "För att ta Droppen finns inga kunskapskrav."

    // Source: https://shop.svenskalivraddningssallskapet.se/products/skraddaren
    private val expectedSkraddarenCount = 2

    @Test
    fun droppenRequirementCountMatchesOfficial() {
        assertEquals(expectedDroppenCount, requirementsForBadge("droppen").size)
    }

    @Test
    fun droppenFirstRequirementTextMatchesOfficial() {
        val requirements = requirementsForBadge("droppen")
        assertEquals(expectedDroppenFirstTextSv, requirements.first()["textSv"]!!.jsonPrimitive.content)
    }

    @Test
    fun skraddarenRequirementCountMatchesOfficial() {
        assertEquals(expectedSkraddarenCount, requirementsForBadge("skraddaren").size)
    }

    @Test
    fun allRequirementsHaveBilingualText() {
        val catalog = json.parseToJsonElement(seedFile.readText()).jsonObject
        for (category in catalog.getValue("categories").jsonArray) {
            for (badge in category.jsonObject.getValue("badges").jsonArray) {
                for (requirement in badge.jsonObject.getValue("requirements").jsonArray) {
                    val requirementObject = requirement.jsonObject
                    val textSv = requirementObject.getValue("textSv").jsonPrimitive.content
                    val textEn = requirementObject.getValue("textEn").jsonPrimitive.content
                    assertTrue("textSv blank for ${badge.jsonObject["code"]}", textSv.isNotBlank())
                    assertTrue("textEn blank for ${badge.jsonObject["code"]}", textEn.isNotBlank())
                }
            }
        }
    }

    @Test
    fun categoriesOrderedBySortOrder() {
        val catalog = json.parseToJsonElement(seedFile.readText()).jsonObject
        val sortOrders = catalog.getValue("categories").jsonArray.map {
            it.jsonObject.getValue("sortOrder").jsonPrimitive.content.toInt()
        }
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
