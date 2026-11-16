package com.christianokeke.scroll

import com.christianokeke.scroll.data.BollsClient
import com.christianokeke.scroll.data.BollsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BollsParserTest {

    @Test
    fun searchDropsApocryphaAndCleansText() {
        // Shape of find/KJV/?search=love: book 70 (Wisdom) and 67+ are Apocrypha.
        val json = """
            [
              {"pk": 2633405, "translation": "KJV", "book": 70, "chapter": 6, "verse": 17,
               "text": "For the very true beginning of her is the desire of discipline; and the care of discipline is <mark>love</mark>;"},
              {"pk": 2626312, "translation": "KJV", "book": 43, "chapter": 3, "verse": 16,
               "text": "For<S>1063</S> God<S>2316</S> so<S>3779</S> <mark>love</mark>d<S>25</S> the world<S>2889</S>"},
              {"pk": 3, "translation": "KJV", "book": 67, "chapter": 1, "verse": 1, "text": "x"},
              {"pk": 4, "translation": "KJV", "book": 66, "chapter": 2, "verse": 4, "text": "thou hast left thy first <mark>love</mark>."},
              {"pk": 5, "translation": "KJV", "book": 0, "chapter": 1, "verse": 1, "text": "x"}
            ]
        """.trimIndent()

        val verses = BollsParser.parseSearch(json)

        assertEquals(listOf(43, 66), verses.map { it.book })
        assertEquals("For God so loved the world", verses[0].text)
        assertEquals(2626312, verses[0].id)
        assertEquals("John 3:16", verses[0].reference)
        assertEquals("KJV", verses[0].translation)
        assertEquals("thou hast left thy first love.", verses[1].text)
    }

    @Test
    fun chapterParsesVersesAndIgnoresComments() {
        val json = """
            [
              {"pk": 1, "verse": 1, "text": "The LORD is my shepherd;<br>I have all that I need.<br>"},
              {"pk": 2, "verse": 2, "text": "He lets me rest in green meadows;<br>he leads me beside peaceful streams.<br>", "comment": "<a>note</a>"}
            ]
        """.trimIndent()

        val verses = BollsParser.parseChapter(json)

        assertEquals(2, verses.size)
        assertEquals(1, verses[0].verse)
        assertEquals("The LORD is my shepherd; I have all that I need.", verses[0].text)
        assertEquals("He lets me rest in green meadows; he leads me beside peaceful streams.", verses[1].text)
    }

    @Test
    fun emptyResultsParseToEmptyLists() {
        assertTrue(BollsParser.parseSearch("[]").isEmpty())
        assertTrue(BollsParser.parseChapter("[]").isEmpty())
    }

    @Test
    fun urlsMatchTheBollsEndpoints() {
        assertEquals(
            "https://bolls.life/find/ESV/?search=self-control&match_case=false&match_whole=false&limit=20",
            BollsClient.searchUrl("ESV", "self-control"),
        )
        assertEquals("https://bolls.life/get-chapter/KJV/43/3/", BollsClient.chapterUrl("KJV", 43, 3))
    }
}
