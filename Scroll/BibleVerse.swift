import Foundation
// BibleVerse.swift
struct BibleVerse: Codable, Hashable {
    let pk: Int
    let translation: String
    let book: Int
    let chapter: Int
    let verse: Int
    let text: String
    
    var reference: String {
        // Format the reference string based on the book, chapter, and verse
        let bookName = getBibleBookName(book)
        return "\(bookName) \(chapter):\(verse)"
    }
    
    private func getBibleBookName(_ bookNumber: Int) -> String {
        let bibleBooks = [
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
        
        guard bookNumber >= 1 && bookNumber <= bibleBooks.count else {
            return ""
        }
        
        return bibleBooks[bookNumber - 1]
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        pk = try container.decode(Int.self, forKey: .pk)
        translation = try container.decode(String.self, forKey: .translation)
        book = try container.decode(Int.self, forKey: .book)
        chapter = try container.decode(Int.self, forKey: .chapter)
        verse = try container.decode(Int.self, forKey: .verse)
        
        // Remove the <mark> tags from the text
        let rawText = try container.decode(String.self, forKey: .text)
        text = rawText
            .replacingOccurrences(of: "<mark>", with: "")
            .replacingOccurrences(of: "</mark>", with: "")
            .replacingOccurrences(of: "<br/>", with: " ")
    }
    
    enum CodingKeys: String, CodingKey {
        case pk
        case translation
        case book
        case chapter
        case verse
        case text
    }
}
