package se.simmarken.data.prefs

import androidx.core.os.LocaleListCompat

enum class LanguageMode {
    SYSTEM,
    SWEDISH,
    ENGLISH,
    ;

    companion object {
        fun fromStoredValue(value: String?): LanguageMode {
            if (value.isNullOrBlank()) return SYSTEM
            return entries.find { it.name == value } ?: SYSTEM
        }
    }
}

/**
 * BCP-47 tag for explicit locales; null means follow system locale (empty locale list).
 */
fun mapLanguageModeToTag(mode: LanguageMode): String? = when (mode) {
    LanguageMode.SYSTEM -> null
    LanguageMode.SWEDISH -> "sv"
    LanguageMode.ENGLISH -> "en"
}

fun mapModeToLocaleList(mode: LanguageMode): LocaleListCompat {
    val tag = mapLanguageModeToTag(mode)
    return if (tag == null) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(tag)
    }
}
