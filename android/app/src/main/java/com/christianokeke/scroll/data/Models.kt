package com.christianokeke.scroll.data

/** A verse from a search result, or one the user saved. [id] is bolls.life's `pk`, unique per translation. */
data class BibleVerse(
    val id: Int,
    val translation: String,
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val text: String,
) {
    val reference: String get() = "${BibleBooks.name(book)} $chapter:$verse"
}

/** One verse of a loaded chapter. */
data class ChapterVerse(
    val id: Int,
    val verse: Int,
    val text: String,
)

data class SavedChapter(
    val translation: String,
    val book: Int,
    val chapter: Int,
    val savedAt: Long,
) {
    val id: String get() = "$translation-$book-$chapter"
    val reference: String get() = "${BibleBooks.name(book)} $chapter"
}
