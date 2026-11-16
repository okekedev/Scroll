import Foundation

struct SavedChapter: Codable, Hashable, Identifiable {
    let translation: String
    let book: Int
    let chapter: Int
    let savedAt: Date

    var id: String { "\(translation)-\(book)-\(chapter)" }
    var bookName: String { BibleBooks.name(for: book) }
    var reference: String { "\(bookName) \(chapter)" }
}
