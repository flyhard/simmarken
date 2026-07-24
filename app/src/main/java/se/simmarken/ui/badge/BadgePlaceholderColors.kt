package se.simmarken.ui.badge

import androidx.compose.ui.graphics.Color

object BadgePlaceholderColors {
    val default: Color = Color(0xFF9E9E9E)

    private val categoryColors: Map<String, Color> = mapOf(
        // Simidrott affisch tiers
        "sjohasten" to Color(0xFF00BCD4),
        "vattenvana" to Color(0xFF4CAF50),
        "nyborjare" to Color(0xFF03A9F4),
        "simsattmarke" to Color(0xFF9C27B0),
        "hajen" to Color(0xFF607D8B),
        "vattenprovet" to Color(0xFF26A69A),
        "simborgarmarke" to Color(0xFF5D4037),
        "kilometermarke" to Color(0xFF8BC34A),
        "jarn-till-kandidaten" to Color(0xFF795548),
        "magistermarke" to Color(0xFFFFC107),
        "vattenpolomarke" to Color(0xFF1565C0),
        "konstsims" to Color(0xFFE91E63),
        "hoppsmarke" to Color(0xFF00ACC1),
        "sarahmarket" to Color(0xFFF06292),
        // SLS shop groupings
        "grund" to Color(0xFF26C6DA),
        "doppingen" to Color(0xFF42A5F5),
        "livbojen" to Color(0xFFEF5350),
        "krabban" to Color(0xFFFF7043),
        "uttern" to Color(0xFF8D6E63),
        "krokodilen" to Color(0xFF66BB6A),
        "grodan" to Color(0xFF7CB342),
        "sal" to Color(0xFF5C6BC0),
    )

    fun forCategoryCode(categoryCode: String): Color = categoryColors[categoryCode] ?: default

    fun forBadgeCode(badgeCode: String): Color = forCategoryCode(badgeCode)
}
