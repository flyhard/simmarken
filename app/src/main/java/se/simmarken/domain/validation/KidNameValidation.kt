package se.simmarken.domain.validation

object KidNameValidation {
    fun validateName(raw: String): String? {
        val trimmed = raw.trim()
        return when {
            trimmed.isEmpty() -> "Ange ett namn"
            trimmed.length > 30 -> "Namnet får vara högst 30 tecken"
            else -> null
        }
    }
}
