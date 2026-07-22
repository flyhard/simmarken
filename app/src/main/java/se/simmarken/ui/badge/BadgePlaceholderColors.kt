package se.simmarken.ui.badge

import androidx.compose.ui.graphics.Color

object BadgePlaceholderColors {
    val default: Color = Color(0xFF9E9E9E)

    private val categoryColors: Map<String, Color> = mapOf(
        // Simidrott affisch tiers
        "vattenvana" to Color(0xFF4CAF50),
        "nyborjare" to Color(0xFF03A9F4),
        "hajen" to Color(0xFF607D8B),
        "jarn" to Color(0xFF795548),
        "brons" to Color(0xFFCD7F32),
        "silver" to Color(0xFFB0BEC5),
        "guld" to Color(0xFFFFC107),
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
