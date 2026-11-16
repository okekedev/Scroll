import Foundation

/// The translations the app offers, in picker order.
enum BibleTranslations {
    static let available = ["ESV", "KJV", "NASB", "NLT"]
    static let defaultCode = "ESV"

    static func isAvailable(_ code: String) -> Bool {
        available.contains(code)
    }

    /// Maps a stored translation code onto one the app still offers, so items
    /// saved in a since-removed translation open in the default instead.
    static func resolve(_ code: String) -> String {
        isAvailable(code) ? code : defaultCode
    }
}

enum BibleBooks {
    static let names: [String] = [
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth",
        "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
        "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon",
        "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
        "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah",
        "Malachi", "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians",
        "2 Corinthians", "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians",
        "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
        "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation"
    ]

    static let chapterCounts: [Int] = [
        50, 40, 27, 36, 34, 24, 21, 4, 31, 24, 22, 25, 29, 36, 10, 13, 10, 42, 150, 31,
        12, 8, 66, 52, 5, 48, 12, 14, 3, 9, 1, 4, 7, 3, 3, 3, 2, 14, 4,
        28, 16, 24, 21, 28,
        16, 16, 13, 6, 6, 4, 4, 5, 3, 6, 4, 3, 1, 13, 5, 5, 3, 5, 1, 1, 1, 22
    ]

    /// USFM 2.4 book codes — matches Bible Brain's `book_id` field.
    /// bolls.life uses `protestant_order` (1=GEN … 66=REV) which lines up 1:1
    /// with this table.
    static let usfmCodes: [String] = [
        "GEN", "EXO", "LEV", "NUM", "DEU", "JOS", "JDG", "RUT",
        "1SA", "2SA", "1KI", "2KI", "1CH", "2CH", "EZR",
        "NEH", "EST", "JOB", "PSA", "PRO", "ECC", "SNG",
        "ISA", "JER", "LAM", "EZK", "DAN", "HOS", "JOL", "AMO",
        "OBA", "JON", "MIC", "NAM", "HAB", "ZEP", "HAG", "ZEC",
        "MAL", "MAT", "MRK", "LUK", "JHN", "ACT", "ROM", "1CO",
        "2CO", "GAL", "EPH", "PHP", "COL", "1TH",
        "2TH", "1TI", "2TI", "TIT", "PHM", "HEB", "JAS",
        "1PE", "2PE", "1JN", "2JN", "3JN", "JUD", "REV",
    ]

    static func usfm(for book: Int) -> String? {
        guard book >= 1, book <= usfmCodes.count else { return nil }
        return usfmCodes[book - 1]
    }

    static func name(for book: Int) -> String {
        guard book >= 1, book <= names.count else { return "" }
        return names[book - 1]
    }

    static func chapters(in book: Int) -> Int {
        guard book >= 1, book <= chapterCounts.count else { return 0 }
        return chapterCounts[book - 1]
    }

    struct Location: Equatable {
        let book: Int
        let chapter: Int
    }

    static func next(book: Int, chapter: Int) -> Location? {
        let maxChapters = chapters(in: book)
        if chapter < maxChapters {
            return Location(book: book, chapter: chapter + 1)
        }
        if book < names.count {
            return Location(book: book + 1, chapter: 1)
        }
        return nil
    }

    static func previous(book: Int, chapter: Int) -> Location? {
        if chapter > 1 {
            return Location(book: book, chapter: chapter - 1)
        }
        if book > 1 {
            return Location(book: book - 1, chapter: chapters(in: book - 1))
        }
        return nil
    }
}
