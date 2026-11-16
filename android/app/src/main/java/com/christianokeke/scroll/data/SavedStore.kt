package com.christianokeke.scroll.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Everything the app remembers: saved verses, saved chapters and the last
 * translation picked. Lives in one SharedPreferences file as JSON, on the
 * device only.
 */
class SavedStore(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    private val _verses = MutableStateFlow(loadVerses())
    val verses: StateFlow<List<BibleVerse>> = _verses.asStateFlow()

    private val _chapters = MutableStateFlow(loadChapters())
    val chapters: StateFlow<List<SavedChapter>> = _chapters.asStateFlow()

    var translation: String
        get() = BibleTranslations.resolve(prefs.getString(KEY_TRANSLATION, null))
        set(value) = prefs.edit { putString(KEY_TRANSLATION, BibleTranslations.resolve(value)) }

    fun isVerseSaved(id: Int): Boolean = _verses.value.any { it.id == id }

    fun saveVerse(verse: BibleVerse) {
        if (isVerseSaved(verse.id)) return
        _verses.value = _verses.value + verse
        persistVerses()
    }

    fun removeVerse(verse: BibleVerse) {
        _verses.value = _verses.value.filterNot { it.id == verse.id }
        persistVerses()
    }

    fun isChapterSaved(translation: String, book: Int, chapter: Int): Boolean =
        _chapters.value.any { it.translation == translation && it.book == book && it.chapter == chapter }

    fun toggleChapter(translation: String, book: Int, chapter: Int) {
        _chapters.value = if (isChapterSaved(translation, book, chapter)) {
            _chapters.value.filterNot { it.translation == translation && it.book == book && it.chapter == chapter }
        } else {
            _chapters.value + SavedChapter(translation, book, chapter, System.currentTimeMillis())
        }
        persistChapters()
    }

    fun removeChapter(chapter: SavedChapter) {
        _chapters.value = _chapters.value.filterNot { it.id == chapter.id }
        persistChapters()
    }

    private fun loadVerses(): List<BibleVerse> = readArray(KEY_VERSES) { item ->
        BibleVerse(
            id = item.getInt("pk"),
            translation = item.getString("translation"),
            book = item.getInt("book"),
            chapter = item.getInt("chapter"),
            verse = item.getInt("verse"),
            text = item.getString("text"),
        )
    }

    private fun loadChapters(): List<SavedChapter> = readArray(KEY_CHAPTERS) { item ->
        SavedChapter(
            translation = item.getString("translation"),
            book = item.getInt("book"),
            chapter = item.getInt("chapter"),
            savedAt = item.optLong("savedAt", 0L),
        )
    }

    private fun persistVerses() {
        val array = JSONArray()
        for (verse in _verses.value) {
            array.put(
                JSONObject()
                    .put("pk", verse.id)
                    .put("translation", verse.translation)
                    .put("book", verse.book)
                    .put("chapter", verse.chapter)
                    .put("verse", verse.verse)
                    .put("text", verse.text)
            )
        }
        prefs.edit { putString(KEY_VERSES, array.toString()) }
    }

    private fun persistChapters() {
        val array = JSONArray()
        for (chapter in _chapters.value) {
            array.put(
                JSONObject()
                    .put("translation", chapter.translation)
                    .put("book", chapter.book)
                    .put("chapter", chapter.chapter)
                    .put("savedAt", chapter.savedAt)
            )
        }
        prefs.edit { putString(KEY_CHAPTERS, array.toString()) }
    }

    /** Reads a stored JSON array, skipping any entry that does not parse. */
    private fun <T> readArray(key: String, parse: (JSONObject) -> T): List<T> {
        val stored = prefs.getString(key, null) ?: return emptyList()
        val array = try {
            JSONArray(stored)
        } catch (_: JSONException) {
            return emptyList()
        }
        val items = ArrayList<T>(array.length())
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            try {
                items += parse(item)
            } catch (_: JSONException) {
                // One unreadable entry must not cost the user the rest of the list.
            }
        }
        return items
    }

    private companion object {
        const val FILE = "scroll"
        const val KEY_VERSES = "savedVerses"
        const val KEY_CHAPTERS = "savedChapters"
        const val KEY_TRANSLATION = "translation"
    }
}
