package se.simmarken.res

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class StringsParityTest {
    private fun resDirectory(): File {
        val candidates = listOf(
            File("src/main/res"),
            File("app/src/main/res"),
        )
        return candidates.firstOrNull { File(it, "values/strings.xml").exists() }
            ?: error("Could not locate res directory from working dir: ${File(".").absolutePath}")
    }

    @Test
    fun allTranslatableDefaultKeysExistInEnglish() {
        val resDir = resDirectory()
        val defaultKeys = parseStringKeys(File(resDir, "values/strings.xml"))
        val enKeys = parseStringKeys(File(resDir, "values-en/strings.xml")).keys

        defaultKeys.filterValues { it }.keys.forEach { key ->
            assertTrue("Missing values-en key: $key", enKeys.contains(key))
        }
    }

    private fun parseStringKeys(file: File): Map<String, Boolean> {
        val keys = linkedMapOf<String, Boolean>()
        val pattern = Regex("""<string\s+name="([^"]+)"(?:\s+translatable="(false|true)")?""")
        file.readText().lineSequence().forEach { line ->
            val match = pattern.find(line) ?: return@forEach
            val name = match.groupValues[1]
            val translatable = when (match.groupValues.getOrNull(2)) {
                "false" -> false
                else -> true
            }
            keys[name] = translatable
        }
        return keys
    }
}
