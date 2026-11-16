import Foundation

struct ChapterVerse: Decodable, Identifiable {
    let id: Int
    let verse: Int
    let text: String
    let comment: String?

    enum CodingKeys: String, CodingKey {
        case id = "pk"
        case verse
        case text
        case comment
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(Int.self, forKey: .id)
        verse = try container.decode(Int.self, forKey: .verse)
        text = VerseTextFormatter.clean(try container.decode(String.self, forKey: .text))
        comment = try container.decodeIfPresent(String.self, forKey: .comment)
    }
}
