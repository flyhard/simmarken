package se.simmarken.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class KidAvatarInitialsTest {
    @Test
    fun twoWordsUsesFirstLetters() {
        assertEquals("AB", KidAvatarInitials.fromName("Anna Berg"))
    }

    @Test
    fun singleWordUsesFirstTwoLetters() {
        assertEquals("EL", KidAvatarInitials.fromName("Ella"))
    }

    @Test
    fun emptyNameReturnsQuestionMark() {
        assertEquals("?", KidAvatarInitials.fromName(""))
    }
}
