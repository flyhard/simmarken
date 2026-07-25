package se.simmarken.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KidNameValidationTest {
    @Test
    fun emptyStringReturnsError() {
        assertEquals(KidNameError.EMPTY, KidNameValidation.validateName(""))
    }

    @Test
    fun whitespaceOnlyReturnsError() {
        assertEquals(KidNameError.EMPTY, KidNameValidation.validateName("   "))
    }

    @Test
    fun nameOver30CharsReturnsError() {
        val longName = "a".repeat(31)
        assertEquals(KidNameError.TOO_LONG, KidNameValidation.validateName(longName))
    }

    @Test
    fun validNameReturnsNull() {
        assertNull(KidNameValidation.validateName("Adam"))
    }
}
