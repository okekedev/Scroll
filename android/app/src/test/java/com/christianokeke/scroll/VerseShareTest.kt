package com.christianokeke.scroll

import com.christianokeke.scroll.data.BibleVerse
import com.christianokeke.scroll.data.VerseShare
import org.junit.Assert.assertEquals
import org.junit.Test

class VerseShareTest {

    @Test
    fun shareTextIsVerseBlankLineReferenceThenLink() {
        val text = VerseShare.text(
            "For God so loved the world, that he gave his only Son, that whoever believes in him " +
                "should not perish but have eternal life.",
            book = 43, chapter = 3, verse = 16, translation = "ESV",
        )
        assertEquals(
            "For God so loved the world, that he gave his only Son, that whoever believes in him " +
                "should not perish but have eternal life.\n" +
                "\n" +
                "John 3:16 (ESV)\n" +
                "https://scroll.okeke.us/ESV/43/3/16",
            text,
        )
    }

    @Test
    fun shareTextHasExactlyFourLinesAndNoTrailingNewline() {
        val lines = VerseShare.text("Jesus wept.", 43, 11, 35, "KJV").split("\n")
        assertEquals(listOf("Jesus wept.", "", "John 11:35 (KJV)", "https://scroll.okeke.us/KJV/43/11/35"), lines)
    }

    @Test
    fun linkUsesTheBookNumberNotItsName() {
        val text = VerseShare.text("Love is patient and kind;", 46, 13, 4, "NASB")
        assertEquals("1 Corinthians 13:4 (NASB)", text.split("\n")[2])
        assertEquals("https://scroll.okeke.us/NASB/46/13/4", text.split("\n")[3])
    }

    @Test
    fun aSavedOrSearchedVerseSharesTheSameWay() {
        val verse = BibleVerse(id = 1, translation = "NLT", book = 19, chapter = 23, verse = 1, text = "The LORD is my shepherd; I have all that I need.")
        assertEquals("Psalms 23:1", verse.reference)
        assertEquals(
            "The LORD is my shepherd; I have all that I need.\n\nPsalms 23:1 (NLT)\nhttps://scroll.okeke.us/NLT/19/23/1",
            VerseShare.text(verse),
        )
    }
}
