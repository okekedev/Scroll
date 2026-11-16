package com.christianokeke.scroll.data

object VerseTextFormatter {
    private val strongsNumber = Regex("<S>[^<]*</S>")
    private val translatorNote = Regex("<sup>.*?</sup>")
    private val lineBreak = Regex("<br\\s*/?>")
    private val anyTag = Regex("<[^>]+>")

    // \p{Z} as well as \s: iOS's regex engine counts Unicode spaces (such as a
    // no-break space) as whitespace, the JVM's \s on its own does not.
    private val whitespace = Regex("[\\s\\p{Z}]+")

    /**
     * bolls.life returns verse text with light HTML: `<br>` line breaks, `<mark>`
     * search highlights, and in the KJV Strong's numbers (`<S>1063</S>`) and
     * translator notes (`<sup>…</sup>`). Numbers and notes are dropped whole;
     * every other tag is removed and its text kept. Same steps, same order, as
     * `VerseTextFormatter.clean` in the iOS app.
     */
    fun clean(raw: String): String {
        var text = raw
        text = strongsNumber.replace(text, "")
        text = translatorNote.replace(text, "")
        text = lineBreak.replace(text, " ")
        text = anyTag.replace(text, "")
        text = whitespace.replace(text, " ")
        return text.trim()
    }
}

/** The text handed to the share sheet and the clipboard for a single verse. */
object VerseShare {
    const val WEB_BASE: String = "https://scroll.okeke.us"

    fun text(verseText: String, book: Int, chapter: Int, verse: Int, translation: String): String =
        "$verseText\n\n" +
            "${BibleBooks.name(book)} $chapter:$verse ($translation)\n" +
            "$WEB_BASE/$translation/$book/$chapter/$verse"

    fun text(verse: BibleVerse): String =
        text(verse.text, verse.book, verse.chapter, verse.verse, verse.translation)
}
