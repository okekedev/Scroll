import Foundation

/// Networking client for the Bible Brain (Faith Comes By Hearing) API v4.
///
/// Dormant until `BibleBrainConfig.isReady` is true.
///
/// Endpoints in use:
/// - `GET /bibles?language_code=eng&media=audio_stream` — list English HLS filesets.
/// - `GET /bibles/filesets/{fileset_id}/{book}/{chapter}` — fetch chapter audio URL.
/// - `GET /bibles/{bible_id}/copyright` — fetch attribution text for the About screen.
enum BibleBrainClient {

    // MARK: - Response models

    struct FilesetsResponse: Decodable {
        let data: [Fileset]
    }

    struct Fileset: Decodable, Hashable {
        let id: String            // e.g. "ENGKJVN2SA"
        let type: String          // "audio_stream" | "audio" | ...
        let size: String?         // "NT" | "OT" | "C"
        let bible_id: String?     // e.g. "ENGKJV"
        let volume: String?       // display name
    }

    struct ChapterResponse: Decodable {
        let data: [ChapterMedia]
    }

    struct ChapterMedia: Decodable {
        let book_id: String       // USFM: "GEN", "MAT", "REV"
        let chapter_start: Int
        let path: String          // HLS URL or MP3 URL
        let duration: Double?
    }

    struct CopyrightResponse: Decodable {
        let data: CopyrightPayload
    }

    struct CopyrightPayload: Decodable {
        let copyright: String?
        let organizations: [Organization]?

        struct Organization: Decodable {
            let organization_id: Int?
            let name: String?
        }
    }

    // MARK: - Requests

    static func fetchEnglishAudioStreamFilesets(completion: @escaping ([Fileset]) -> Void) {
        guard BibleBrainConfig.isReady else { completion([]); return }
        var comps = URLComponents(url: BibleBrainConfig.baseURL.appendingPathComponent("bibles"), resolvingAgainstBaseURL: false)!
        comps.queryItems = [
            URLQueryItem(name: "v", value: BibleBrainConfig.apiVersion),
            URLQueryItem(name: "key", value: BibleBrainConfig.apiKey),
            URLQueryItem(name: "language_code", value: "eng"),
            URLQueryItem(name: "media", value: "audio_stream"),
        ]
        get(comps: comps, as: FilesetsResponse.self) { result in
            completion(result?.data ?? [])
        }
    }

    static func fetchChapterAudio(filesetId: String, book: String, chapter: Int, completion: @escaping (ChapterMedia?) -> Void) {
        guard BibleBrainConfig.isReady else { completion(nil); return }
        var comps = URLComponents(
            url: BibleBrainConfig.baseURL
                .appendingPathComponent("bibles/filesets/\(filesetId)/\(book)/\(chapter)"),
            resolvingAgainstBaseURL: false
        )!
        comps.queryItems = [
            URLQueryItem(name: "v", value: BibleBrainConfig.apiVersion),
            URLQueryItem(name: "key", value: BibleBrainConfig.apiKey),
        ]
        get(comps: comps, as: ChapterResponse.self) { result in
            completion(result?.data.first)
        }
    }

    static func fetchCopyright(bibleId: String, completion: @escaping (String?) -> Void) {
        guard BibleBrainConfig.isReady else { completion(nil); return }
        var comps = URLComponents(
            url: BibleBrainConfig.baseURL.appendingPathComponent("bibles/\(bibleId)/copyright"),
            resolvingAgainstBaseURL: false
        )!
        comps.queryItems = [
            URLQueryItem(name: "v", value: BibleBrainConfig.apiVersion),
            URLQueryItem(name: "key", value: BibleBrainConfig.apiKey),
        ]
        get(comps: comps, as: CopyrightResponse.self) { result in
            completion(result?.data.copyright)
        }
    }

    // MARK: - Private

    private static func get<T: Decodable>(comps: URLComponents, as _: T.Type, completion: @escaping (T?) -> Void) {
        guard let url = comps.url else {
            DispatchQueue.main.async { completion(nil) }
            return
        }
        URLSession.shared.dataTask(with: url) { data, _, error in
            if let error = error {
                print("BibleBrainClient error \(url): \(error)")
                DispatchQueue.main.async { completion(nil) }
                return
            }
            guard let data = data else {
                DispatchQueue.main.async { completion(nil) }
                return
            }
            do {
                let decoded = try JSONDecoder().decode(T.self, from: data)
                DispatchQueue.main.async { completion(decoded) }
            } catch {
                print("BibleBrainClient decode error \(url): \(error)")
                DispatchQueue.main.async { completion(nil) }
            }
        }.resume()
    }
}
