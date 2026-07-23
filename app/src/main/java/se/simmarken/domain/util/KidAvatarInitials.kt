package se.simmarken.domain.util

object KidAvatarInitials {
    fun fromName(name: String): String {
        val trimmed = name.trim()
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when {
            words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
            trimmed.length >= 2 -> trimmed.take(2).uppercase()
            trimmed.isNotEmpty() -> trimmed.first().uppercase()
            else -> "?"
        }
    }
}
