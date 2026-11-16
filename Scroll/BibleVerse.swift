import Foundation

struct BibleVerse: Codable, Hashable, Identifiable {
    let id: Int
    let translation: String
    let book: Int
    let chapter: Int
    let verse: Int
    let text: String
    let comment: String?

    var reference: String {
        "\(BibleBooks.name(for: book)) \(chapter):\(verse)"
    }

    init(id: Int, translation: String, book: Int, chapter: Int, verse: Int, text: String, comment: String? = nil) {
        self.id = id
        self.translation = translation
        self.book = book
        self.chapter = chapter
        self.verse = verse
        self.text = text
        self.comment = comment
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)

        id = try container.decode(Int.self, forKey: .pk)
        translation = try container.decode(String.self, forKey: .translation)
        book = try container.decode(Int.self, forKey: .book)
        chapter = try container.decode(Int.self, forKey: .chapter)
        verse = try container.decode(Int.self, forKey: .verse)

        let rawText = try container.decode(String.self, forKey: .text)
        text = VerseTextFormatter.clean(rawText)

        comment = try container.decodeIfPresent(String.self, forKey: .comment)
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)

        try container.encode(id, forKey: .pk)
        try container.encode(translation, forKey: .translation)
        try container.encode(book, forKey: .book)
        try container.encode(chapter, forKey: .chapter)
        try container.encode(verse, forKey: .verse)
        try container.encode(text, forKey: .text)
        try container.encodeIfPresent(comment, forKey: .comment)
    }

    enum CodingKeys: String, CodingKey {
        case pk
        case translation
        case book
        case chapter
        case verse
        case text
        case comment
    }
}
