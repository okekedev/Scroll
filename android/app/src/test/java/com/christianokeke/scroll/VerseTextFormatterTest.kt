package com.christianokeke.scroll

import com.christianokeke.scroll.data.VerseTextFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Raw strings below are copied from live bolls.life responses. */
class VerseTextFormatterTest {

    @Test
    fun kjv_strongsNumbersAreRemovedWithTheirDigits() {
        // get-chapter/KJV/43/3/ verse 16
        val raw = "For<S>1063</S> God<S>2316</S> so<S>3779</S> loved<S>25</S> the world<S>2889</S>, " +
            "that<S>5620</S> he gave<S>1325</S> his<S>846</S> only begotten<S>3439</S> Son<S>5207</S>, " +
            "that<S>2443</S> whosoever<S>3956</S> believeth<S>4100</S> in<S>1519</S> him<S>846</S> " +
            "should<S>622</S> not<S>3361</S> perish<S>622</S>, but<S>235</S> have<S>2192</S> " +
            "everlasting<S>166</S> life<S>2222</S>."
        val cleaned = VerseTextFormatter.clean(raw)
        assertEquals(
            "For God so loved the world, that he gave his only begotten Son, that whosoever " +
                "believeth in him should not perish, but have everlasting life.",
            cleaned,
        )
        assertFalse(cleaned.any { it.isDigit() })
    }

    @Test
    fun kjv_translatorNotesAreRemovedWithTheirText() {
        // get-chapter/KJV/43/3/ verse 3: Strong's numbers, a leading-space number and a <sup> note
        val raw = "Jesus<S>2424</S> answered<S>611</S> and<S>2532</S> said<S>2036</S> unto him<S>846</S>, " +
            "Verily<S>281</S>, verily<S>281</S>, I say<S>3004</S> unto thee<S>4671</S>, Except<S>3362</S> " +
            "a man<S>5100</S> be born<S>1080</S> again<S>509</S>, he cannot<S>1410</S> <S>3756</S> " +
            "see<S>1492</S> the kingdom<S>932</S> of God<S>2316</S>. <sup>again: or, from above</sup>"
        assertEquals(
            "Jesus answered and said unto him, Verily, verily, I say unto thee, Except a man be " +
                "born again, he cannot see the kingdom of God.",
            VerseTextFormatter.clean(raw),
        )
    }

    @Test
    fun kjv_verseThatStartsWithAStrongsNumber() {
        // get-chapter/KJV/43/3/ verse 1
        val raw = "<S>1161</S> There was<S>2258</S> a man<S>444</S> of<S>1537</S> the Pharisees<S>5330</S>, " +
            "named<S>3686</S> <S>846</S> Nicodemus<S>3530</S>, a ruler<S>758</S> of the Jews<S>2453</S>:"
        assertEquals(
            "There was a man of the Pharisees, named Nicodemus, a ruler of the Jews:",
            VerseTextFormatter.clean(raw),
        )
    }

    @Test
    fun kjv_noteInTheMiddleOfAVerse() {
        // get-chapter/KJV/1/1/ verse 4
        val raw = "And God<S>430</S> saw<S>7200</S> <S>853</S> the light<S>216</S>, that<S>3588</S> it was " +
            "good<S>2896</S>: and God<S>430</S> divided<S>914</S> <S>996</S> the light<S>216</S> " +
            "from<S>996</S> the darkness<S>2822</S>. " +
            "<sup>the light from: Heb. between the light and between the darkness</sup>"
        assertEquals(
            "And God saw the light, that it was good: and God divided the light from the darkness.",
            VerseTextFormatter.clean(raw),
        )
    }

    @Test
    fun nlt_lineBreaksBecomeSingleSpaces() {
        // get-chapter/NLT/19/23/ verses 1 and 3
        assertEquals(
            "The LORD is my shepherd; I have all that I need.",
            VerseTextFormatter.clean("The LORD is my shepherd;<br>I have all that I need.<br>"),
        )
        assertEquals(
            "He renews my strength. He guides me along right paths, bringing honor to his name.",
            VerseTextFormatter.clean(
                "He renews my strength.<br>He guides me along right paths,<br>bringing honor to his name.<br>"
            ),
        )
    }

    @Test
    fun lineBreakVariantsAreAllSpaces() {
        assertEquals("a b c d", VerseTextFormatter.clean("a<br>b<br/>c<br />d"))
    }

    @Test
    fun esv_leadingAndDoubleSpacesAreCollapsed() {
        // get-chapter/ESV/19/23/ verse 3 has a double space; some ESV verses also start with one
        assertEquals(
            "He restores my soul. He leads me in paths of righteousness for his name's sake.",
            VerseTextFormatter.clean(" He restores my soul. He leads me in paths of righteousness  for his name's sake."),
        )
        assertEquals(
            "Even though I walk through the valley of the shadow of death, I will fear no evil, for you are with me;",
            VerseTextFormatter.clean(
                "  Even though I walk through the valley of the shadow of death,  I will fear no evil,\n for you are with me;  "
            ),
        )
    }

    @Test
    fun search_markHighlightKeepsItsText() {
        // find/ESV/?search=self-control
        assertEquals(
            "Every athlete exercises self-control in all things. They do it to receive a perishable wreath, but we an imperishable.",
            VerseTextFormatter.clean(
                "Every athlete exercises <mark>self-control</mark> in all things. They do it to receive a perishable wreath, but we an imperishable."
            ),
        )
    }

    @Test
    fun search_nltMarkAndLineBreaksTogether() {
        // find/NLT/?search=self-control
        assertEquals(
            "A person without self-control is like a city with broken-down walls.",
            VerseTextFormatter.clean(
                "A person without <mark>self-control</mark><br>is like a city with broken-down walls.<br>"
            ),
        )
    }

    @Test
    fun otherTagsAreStrippedButTheirTextStays() {
        assertEquals("In the beginning", VerseTextFormatter.clean("<i>In</i> the <b>beginning</b>"))
    }

    @Test
    fun noBreakSpacesCountAsWhitespace() {
        assertEquals("grace and peace", VerseTextFormatter.clean(" grace  and peace "))
    }

    @Test
    fun plainTextAndEmptyTextPassThrough() {
        assertEquals("Jesus wept.", VerseTextFormatter.clean("Jesus wept."))
        assertEquals("", VerseTextFormatter.clean(""))
        assertEquals("", VerseTextFormatter.clean("<S>123</S> <br>"))
    }
}
