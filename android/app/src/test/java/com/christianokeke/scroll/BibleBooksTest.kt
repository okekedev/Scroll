package com.christianokeke.scroll

import com.christianokeke.scroll.data.BibleBooks
import com.christianokeke.scroll.data.BibleTranslations
import com.christianokeke.scroll.data.ChapterLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BibleBooksTest {

    @Test
    fun tablesCoverAllSixtySixBooks() {
        assertEquals(66, BibleBooks.names.size)
        assertEquals(66, BibleBooks.chapterCounts.size)
        assertEquals(1189, BibleBooks.chapterCounts.sum())
        assertEquals("Genesis", BibleBooks.name(1))
        assertEquals("Psalms", BibleBooks.name(19))
        assertEquals("John", BibleBooks.name(43))
        assertEquals("Revelation", BibleBooks.name(66))
        assertEquals(150, BibleBooks.chapters(19))
        assertEquals(21, BibleBooks.chapters(43))
    }

    @Test
    fun booksOutsideTheCanonHaveNoNameOrChapters() {
        assertEquals("", BibleBooks.name(0))
        assertEquals("", BibleBooks.name(67))
        assertEquals(0, BibleBooks.chapters(70))
        assertFalse(BibleBooks.isValidBook(0))
        assertFalse(BibleBooks.isValidBook(67))
        assertTrue(BibleBooks.isValidBook(66))
    }

    @Test
    fun nextWithinABook() {
        assertEquals(ChapterLocation(43, 4), BibleBooks.next(43, 3))
        assertEquals(ChapterLocation(1, 2), BibleBooks.next(1, 1))
    }

    @Test
    fun nextCrossesIntoTheFollowingBook() {
        assertEquals(ChapterLocation(2, 1), BibleBooks.next(1, 50))    // Genesis 50 -> Exodus 1
        assertEquals(ChapterLocation(40, 1), BibleBooks.next(39, 4))   // Malachi 4 -> Matthew 1
        assertEquals(ChapterLocation(44, 1), BibleBooks.next(43, 21))  // John 21 -> Acts 1
        assertEquals(ChapterLocation(66, 1), BibleBooks.next(65, 1))   // Jude -> Revelation 1
    }

    @Test
    fun nextStopsAtTheEndOfTheBible() {
        assertEquals(ChapterLocation(66, 22), BibleBooks.next(66, 21))
        assertNull(BibleBooks.next(66, 22))
    }

    @Test
    fun previousWithinABook() {
        assertEquals(ChapterLocation(43, 2), BibleBooks.previous(43, 3))
        assertEquals(ChapterLocation(66, 21), BibleBooks.previous(66, 22))
    }

    @Test
    fun previousCrossesIntoTheLastChapterOfTheBookBefore() {
        assertEquals(ChapterLocation(1, 50), BibleBooks.previous(2, 1))    // Exodus 1 -> Genesis 50
        assertEquals(ChapterLocation(39, 4), BibleBooks.previous(40, 1))   // Matthew 1 -> Malachi 4
        assertEquals(ChapterLocation(19, 150), BibleBooks.previous(20, 1)) // Proverbs 1 -> Psalm 150
        assertEquals(ChapterLocation(65, 1), BibleBooks.previous(66, 1))   // Revelation 1 -> Jude
    }

    @Test
    fun previousStopsAtTheStartOfTheBible() {
        assertEquals(ChapterLocation(1, 1), BibleBooks.previous(1, 2))
        assertNull(BibleBooks.previous(1, 1))
    }

    @Test
    fun walkingForwardVisitsEveryChapterOnceAndBackAgain() {
        var location: ChapterLocation? = ChapterLocation(1, 1)
        var steps = 0
        var last = location!!
        while (location != null) {
            last = location
            steps++
            location = BibleBooks.next(location.book, location.chapter)
        }
        assertEquals(1189, steps)
        assertEquals(ChapterLocation(66, 22), last)

        location = last
        steps = 0
        while (location != null) {
            last = location
            steps++
            location = BibleBooks.previous(location.book, location.chapter)
        }
        assertEquals(1189, steps)
        assertEquals(ChapterLocation(1, 1), last)
    }

    @Test
    fun translationsAreEsvKjvNasbNltWithEsvDefaultAndNoNiv() {
        assertEquals(listOf("ESV", "KJV", "NASB", "NLT"), BibleTranslations.available)
        assertEquals("ESV", BibleTranslations.DEFAULT)
        assertFalse(BibleTranslations.isAvailable("NIV"))
        assertEquals("ESV", BibleTranslations.resolve("NIV"))
        assertEquals("ESV", BibleTranslations.resolve(null))
        assertEquals("KJV", BibleTranslations.resolve("KJV"))
    }
}
