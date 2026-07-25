package se.simmarken.domain.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import se.simmarken.data.export.BackupDto
import se.simmarken.data.export.ExportJson

class BackupValidatorTest {
    @Test
    fun validator_rejectsUnsupportedVersion() {
        val json = """{"exportVersion":99,"exportedAtEpochMillis":1,"kids":[]}"""
        val result = BackupValidator.validate(json)
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(InvalidReason.UnsupportedVersion, (result as ValidationResult.Invalid).reason)
    }

    @Test
    fun validator_rejectsMalformedJson() {
        val result = BackupValidator.validate("{not json")
        assertTrue(result is ValidationResult.Invalid)
        assertEquals(InvalidReason.ParseFailed, (result as ValidationResult.Invalid).reason)
    }

    @Test
    fun validator_acceptsVersion1() {
        val dto = BackupDto(exportedAtEpochMillis = 1L, kids = emptyList())
        val result = BackupValidator.validate(ExportJson.encode(dto))
        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun validator_rejectsEmptyBytes() {
        val result = BackupValidator.validate(ByteArray(0))
        assertEquals(InvalidReason.InvalidFile, (result as ValidationResult.Invalid).reason)
    }
}
