package se.simmarken.data.seed

object CatalogVersion {
    data class ComparableVersion(
        val year: Int,
        val month: Int,
        val day: Int,
    ) : Comparable<ComparableVersion> {
        override fun compareTo(other: ComparableVersion): Int =
            compareValuesBy(this, other, { it.year }, { it.month }, { it.day })
    }

    fun parse(version: String): ComparableVersion {
        val parts = version.split(".")
        require(parts.size == 3) { "Invalid catalog version format: $version" }
        return ComparableVersion(
            year = parts[0].toInt(),
            month = parts[1].toInt(),
            day = parts[2].toInt(),
        )
    }

    fun shouldMerge(bundled: String, stored: String?): Boolean {
        if (stored == null) return true
        return parse(bundled) > parse(stored)
    }
}
