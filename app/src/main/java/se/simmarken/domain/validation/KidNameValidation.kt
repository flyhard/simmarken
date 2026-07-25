package se.simmarken.domain.validation

enum class KidNameError {
    EMPTY,
    TOO_LONG,
}

object KidNameValidation {
    fun validateName(raw: String): KidNameError? {
        val trimmed = raw.trim()
        return when {
            trimmed.isEmpty() -> KidNameError.EMPTY
            trimmed.length > 30 -> KidNameError.TOO_LONG
            else -> null
        }
    }
}
