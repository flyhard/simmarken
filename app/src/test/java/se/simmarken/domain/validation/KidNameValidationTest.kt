package se.simmarken.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KidNameValidationTest {
    @Test
    fun emptyStringReturnsError() {
        assertEquals("Ange ett namn", KidNameValidation.validateName(""))
    }

    @Test
    fun whitespaceOnlyReturnsError() {
        assertEquals("Ange ett namn", KidNameValidation.validateName("   "))
    }

    @Test
    fun nameOver30CharsReturnsError() {
        val longName = "a".repeat(31)
        assertEquals("Namnet får vara högst 30 tecken", KidNameValidation.validateName(longName))
    }

    @Test
    fun validNameReturnsNull() {
        assertNull(KidNameValidation.validateName("Adam"))
    }
}
