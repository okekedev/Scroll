package com.christianokeke.scroll.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

/** Turns bolls.life JSON into the app's models. Kept apart from the network so it can be unit tested. */
object BollsParser {
    /**
     * Search results. Some translations (KJV) also return Apocrypha matches,
     * numbered above 66; the app only covers the 66 books in [BibleBooks].
     */
    fun parseSearch(json: String): List<BibleVerse> {
        val array = JSONArray(json)
        val verses = ArrayList<BibleVerse>(array.length())
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val book = item.optInt("book", 0)
            if (!BibleBooks.isValidBook(book)) continue
            verses += BibleVerse(
                id = item.getInt("pk"),
                translation = item.getString("translation"),
                book = book,
                chapter = item.getInt("chapter"),
                verse = item.getInt("verse"),
                text = VerseTextFormatter.clean(item.getString("text")),
            )
        }
        return verses
    }

    fun parseChapter(json: String): List<ChapterVerse> {
        val array = JSONArray(json)
        val verses = ArrayList<ChapterVerse>(array.length())
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            verses += ChapterVerse(
                id = item.getInt("pk"),
                verse = item.getInt("verse"),
                text = VerseTextFormatter.clean(item.getString("text")),
            )
        }
        return verses
    }
}

/** The only network access in the app: public, key-less reads from bolls.life. */
object BollsClient {
    private const val BASE = "https://bolls.life"
    private const val USER_AGENT = "Scroll-Android/1.0"
    private const val TIMEOUT_MS = 15_000

    fun searchUrl(translation: String, term: String): String {
        val encoded = URLEncoder.encode(term, "UTF-8")
        // The endpoint ignores `limit` and returns every match; kept to mirror iOS.
        return "$BASE/find/$translation/?search=$encoded&match_case=false&match_whole=false&limit=20"
    }

    fun chapterUrl(translation: String, book: Int, chapter: Int): String =
        "$BASE/get-chapter/$translation/$book/$chapter/"

    /** Throws [IOException] (or a JSON exception) when the verses could not be loaded. */
    suspend fun searchVerses(translation: String, term: String): List<BibleVerse> =
        withContext(Dispatchers.IO) { BollsParser.parseSearch(get(searchUrl(translation, term))) }

    /** Throws [IOException] (or a JSON exception) when the chapter could not be loaded. */
    suspend fun fetchChapter(translation: String, book: Int, chapter: Int): List<ChapterVerse> =
        withContext(Dispatchers.IO) { BollsParser.parseChapter(get(chapterUrl(translation, book, chapter))) }

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.setRequestProperty("Accept", "application/json")
            val status = connection.responseCode
            if (status !in 200..299) throw IOException("HTTP $status for $url")
            return connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
