package se.simmarken.ui.theme

import kotlin.math.abs

object KidAvatarColors {
    val palette: List<Int> = listOf(
        0xFFE53935.toInt(),
        0xFFFB8C00.toInt(),
        0xFFFDD835.toInt(),
        0xFF43A047.toInt(),
        0xFF1E88E5.toInt(),
        0xFF8E24AA.toInt(),
        0xFFD81B60.toInt(),
        0xFF00ACC1.toInt(),
        0xFF6D4C41.toInt(),
        0xFF546E7A.toInt(),
    )

    fun defaultForName(name: String): Int {
        val index = abs(name.trim().hashCode()) % palette.size
        return palette[index]
    }
}
