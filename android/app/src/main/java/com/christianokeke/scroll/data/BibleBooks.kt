package com.christianokeke.scroll.data

/** The translations the app offers, in picker order. */
object BibleTranslations {
    val available: List<String> = listOf("ESV", "KJV", "NASB", "NLT")
    const val DEFAULT: String = "ESV"

    fun isAvailable(code: String): Boolean = code in available

    /** Maps any stored code onto one the app offers. */
    fun resolve(code: String?): String = if (code != null && isAvailable(code)) code else DEFAULT
}

/** A chapter of a book; books are numbered 1 (Genesis) to 66 (Revelation), as bolls.life does. */
data class ChapterLocation(val book: Int, val chapter: Int)

object BibleBooks {
    val names: List<String> = listOf(
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth",
        "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
        "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon",
        "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
        "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah",
        "Malachi", "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians",
        "2 Corinthians", "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians",
        "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
        "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation",
    )

    val chapterCounts: List<Int> = listOf(
        50, 40, 27, 36, 34, 24, 21, 4, 31, 24, 22, 25, 29, 36, 10, 13, 10, 42, 150, 31,
        12, 8, 66, 52, 5, 48, 12, 14, 3, 9, 1, 4, 7, 3, 3, 3, 2, 14, 4,
        28, 16, 24, 21, 28,
        16, 16, 13, 6, 6, 4, 4, 5, 3, 6, 4, 3, 1, 13, 5, 5, 3, 5, 1, 1, 1, 22,
    )

    val count: Int get() = names.size

    fun isValidBook(book: Int): Boolean = book in 1..count

    fun name(book: Int): String = if (isValidBook(book)) names[book - 1] else ""

    fun chapters(book: Int): Int = if (isValidBook(book)) chapterCounts[book - 1] else 0

    /** The chapter after this one, crossing into the next book; null after Revelation 22. */
    fun next(book: Int, chapter: Int): ChapterLocation? = when {
        chapter < chapters(book) -> ChapterLocation(book, chapter + 1)
        book < count -> ChapterLocation(book + 1, 1)
        else -> null
    }

    /** The chapter before this one, crossing into the previous book; null before Genesis 1. */
    fun previous(book: Int, chapter: Int): ChapterLocation? = when {
        chapter > 1 -> ChapterLocation(book, chapter - 1)
        book > 1 -> ChapterLocation(book - 1, chapters(book - 1))
        else -> null
    }
}
