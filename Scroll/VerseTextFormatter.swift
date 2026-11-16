import Foundation

enum VerseTextFormatter {
    /// bolls.life returns verse text with light HTML: `<br>` line breaks, `<mark>`
    /// search highlights, and in the KJV Strong's numbers (`<S>1063</S>`) and
    /// translator notes (`<sup>…</sup>`). Numbers and notes are dropped whole;
    /// every other tag is removed and its text kept.
    static func clean(_ raw: String) -> String {
        var text = raw
        text = text.replacingOccurrences(of: "<S>[^<]*</S>", with: "", options: .regularExpression)
        text = text.replacingOccurrences(of: "<sup>.*?</sup>", with: "", options: .regularExpression)
        text = text.replacingOccurrences(of: "<br\\s*/?>", with: " ", options: .regularExpression)
        text = text.replacingOccurrences(of: "<[^>]+>", with: "", options: .regularExpression)
        text = text.replacingOccurrences(of: "\\s+", with: " ", options: .regularExpression)
        return text.trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

/// The text handed to the share sheet (and the clipboard) for a single verse.
enum VerseShare {
    static func text(_ verseText: String, book: Int, chapter: Int, verse: Int, translation: String) -> String {
        "\(verseText)\n\n\(BibleBooks.name(for: book)) \(chapter):\(verse) (\(translation))"
    }

    static func text(for verse: BibleVerse) -> String {
        text(verse.text, book: verse.book, chapter: verse.chapter, verse: verse.verse, translation: verse.translation)
    }
}
