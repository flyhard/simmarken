package se.simmarken.ui.badge

import androidx.compose.ui.graphics.Color

/**
 * Tier-color fallback palette when a badge has no bundled image asset.
 *
 * Phase 4 Coil loading uses these colors as placeholder backgrounds; the UI overlays
 * badge nameSv or a short code on top. This object supplies background colors only.
 */
object BadgePlaceholderColors {

    private val categoryColors: Map<String, Color> = mapOf(
        // Svensk Simidrott tiers (D-07 affisch names)
        "vattenvana" to Color(0xFF00897B),   // teal
        "nyborjare" to Color(0xFF1976D2),    // blue
        "hajen" to Color(0xFFF57C00),        // orange
        "jarn" to Color(0xFF546E7A),         // blue-grey metallic
        "brons" to Color(0xFF8D6E63),        // bronze
        "silver" to Color(0xFF90A4AE),       // silver
        "guld" to Color(0xFFFFB300),         // gold
        // SLS categories
        "grund" to Color(0xFF42A5F5),
        "doppingen" to Color(0xFF26C6DA),
        "livbojen" to Color(0xFFEF5350),
        "krabban" to Color(0xFFEC407A),
        "uttern" to Color(0xFF7E57C2),
        "krokodilen" to Color(0xFF66BB6A),
        "grodan" to Color(0xFF9CCC65),
        "sal" to Color(0xFF5C6BC0),
    )

    private val badgeOverrides: Map<String, Color> = mapOf(
        "kandidaten" to Color(0xFFFFC107),
        "simsattmarke-1" to categoryColors.getValue("nyborjare"),
        "simsattmarke-2" to categoryColors.getValue("nyborjare"),
        "simsattmarke-3" to categoryColors.getValue("nyborjare"),
        "simsattmarke-4" to categoryColors.getValue("nyborjare"),
    )

    /**
     * Returns a distinct Material-friendly color for the given category [categoryCode].
     * Unknown codes fall back to neutral grey.
     */
    fun forCategoryCode(categoryCode: String): Color =
        categoryColors[categoryCode] ?: Color(0xFF9E9E9E)

    /**
     * Returns a badge-specific override when defined, otherwise delegates to [categoryCode].
     */
    fun forBadgeCode(badgeCode: String, categoryCode: String): Color =
        badgeOverrides[badgeCode] ?: forCategoryCode(categoryCode)

    /**
     * Convenience overload when category is embedded in badge code prefix (e.g. hajen-silver).
     */
    fun forBadgeCode(badgeCode: String): Color {
        badgeOverrides[badgeCode]?.let { return it }
        val prefix = badgeCode.substringBefore('-')
        return categoryColors[prefix] ?: forCategoryCode(prefix)
    }
}
