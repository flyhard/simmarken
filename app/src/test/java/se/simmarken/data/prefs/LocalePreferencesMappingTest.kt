package se.simmarken.data.prefs

import androidx.core.os.LocaleListCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LocalePreferencesMappingTest {
    @Test
    fun mapSystemMode_emptyLocaleList() {
        val locales = mapModeToLocaleList(LanguageMode.SYSTEM)
        assertTrue(locales.isEmpty)
    }

    @Test
    fun mapSwedish_svTag() {
        assertEquals("sv", mapLanguageModeToTag(LanguageMode.SWEDISH))
        val locales = mapModeToLocaleList(LanguageMode.SWEDISH)
        assertEquals(LocaleListCompat.forLanguageTags("sv").toLanguageTags(), locales.toLanguageTags())
    }

    @Test
    fun mapEnglish_enTag() {
        assertEquals("en", mapLanguageModeToTag(LanguageMode.ENGLISH))
        val locales = mapModeToLocaleList(LanguageMode.ENGLISH)
        assertEquals(LocaleListCompat.forLanguageTags("en").toLanguageTags(), locales.toLanguageTags())
    }

    @Test
    fun mapSystemMode_nullTag() {
        assertNull(mapLanguageModeToTag(LanguageMode.SYSTEM))
    }

    @Test
    fun fromStoredValue_defaultsToSystemWhenAbsent() {
        assertEquals(LanguageMode.SYSTEM, LanguageMode.fromStoredValue(null))
        assertEquals(LanguageMode.SYSTEM, LanguageMode.fromStoredValue(""))
        assertEquals(LanguageMode.SYSTEM, LanguageMode.fromStoredValue("invalid"))
    }

    @Test
    fun fromStoredValue_parsesEnumNames() {
        assertEquals(LanguageMode.SWEDISH, LanguageMode.fromStoredValue("SWEDISH"))
        assertEquals(LanguageMode.ENGLISH, LanguageMode.fromStoredValue("ENGLISH"))
        assertEquals(LanguageMode.SYSTEM, LanguageMode.fromStoredValue("SYSTEM"))
    }
}
