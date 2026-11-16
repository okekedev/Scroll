import Foundation

enum BollsClient {
    static func searchVerses(version: String, term: String, completion: @escaping ([BibleVerse]) -> Void) {
        let encoded = term.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? term
        let urlString = "https://bolls.life/find/\(version)/?search=\(encoded)&match_case=false&match_whole=false&limit=20"
        fetch(urlString: urlString) { (verses: [BibleVerse]?) in
            // Some translations (KJV) also return Apocrypha matches; the app
            // only covers the 66 books in `BibleBooks`.
            completion((verses ?? []).filter { $0.book >= 1 && $0.book <= BibleBooks.names.count })
        }
    }

    static func fetchChapter(translation: String, book: Int, chapter: Int, completion: @escaping ([ChapterVerse]) -> Void) {
        let urlString = "https://bolls.life/get-chapter/\(translation)/\(book)/\(chapter)/"
        fetch(urlString: urlString) { (verses: [ChapterVerse]?) in
            completion(verses ?? [])
        }
    }

    /// A single verse in the given translation, or `nil` if it could not be loaded.
    static func fetchVerse(translation: String, book: Int, chapter: Int, verse: Int, completion: @escaping (ChapterVerse?) -> Void) {
        let urlString = "https://bolls.life/get-verse/\(translation)/\(book)/\(chapter)/\(verse)/"
        fetch(urlString: urlString, completion: completion)
    }

    private static func fetch<T: Decodable>(urlString: String, completion: @escaping (T?) -> Void) {
        guard let url = URL(string: urlString) else {
            DispatchQueue.main.async { completion(nil) }
            return
        }
        URLSession.shared.dataTask(with: url) { data, _, error in
            if let error = error {
                print("BollsClient error \(urlString): \(error)")
                DispatchQueue.main.async { completion(nil) }
                return
            }
            guard let data = data else {
                DispatchQueue.main.async { completion(nil) }
                return
            }
            do {
                let result = try JSONDecoder().decode(T.self, from: data)
                DispatchQueue.main.async { completion(result) }
            } catch {
                print("BollsClient decode error \(urlString): \(error)")
                DispatchQueue.main.async { completion(nil) }
            }
        }.resume()
    }
}
