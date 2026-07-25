package se.simmarken.domain.export

import se.simmarken.data.export.BackupDto
import se.simmarken.data.export.ExportJson

sealed class ValidationResult {
    data class Valid(val dto: BackupDto) : ValidationResult()
    data class Invalid(val reason: InvalidReason) : ValidationResult()
}

enum class InvalidReason {
    InvalidFile,
    UnsupportedVersion,
    ParseFailed,
}

object BackupValidator {
    private const val SUPPORTED_EXPORT_VERSION = 1

    fun validate(bytes: ByteArray): ValidationResult {
        if (bytes.isEmpty()) {
            return ValidationResult.Invalid(InvalidReason.InvalidFile)
        }
        return validate(String(bytes, Charsets.UTF_8))
    }

    fun validate(json: String): ValidationResult {
        if (json.isBlank()) {
            return ValidationResult.Invalid(InvalidReason.InvalidFile)
        }
        val dto = try {
            ExportJson.decode(json)
        } catch (_: Exception) {
            return ValidationResult.Invalid(InvalidReason.ParseFailed)
        }
        if (dto.exportVersion != SUPPORTED_EXPORT_VERSION) {
            return ValidationResult.Invalid(InvalidReason.UnsupportedVersion)
        }
        return ValidationResult.Valid(dto)
    }
}
