import Foundation

class SavedChaptersModel: ObservableObject {
    @Published var savedChapters: [SavedChapter] = []

    private let userDefaults = UserDefaults.standard
    private let key = "savedChapters"

    init() {
        load()
    }

    func isSaved(translation: String, book: Int, chapter: Int) -> Bool {
        savedChapters.contains { $0.translation == translation && $0.book == book && $0.chapter == chapter }
    }

    func toggle(translation: String, book: Int, chapter: Int) {
        if isSaved(translation: translation, book: book, chapter: chapter) {
            savedChapters.removeAll { $0.translation == translation && $0.book == book && $0.chapter == chapter }
        } else {
            savedChapters.append(SavedChapter(translation: translation, book: book, chapter: chapter, savedAt: Date()))
        }
        persist()
    }

    func remove(_ chapter: SavedChapter) {
        savedChapters.removeAll { $0.id == chapter.id }
        persist()
    }

    private func load() {
        guard let data = userDefaults.data(forKey: key),
              let chapters = try? JSONDecoder().decode([SavedChapter].self, from: data) else {
            return
        }
        savedChapters = chapters
        migrateRetiredTranslations()
    }

    /// Chapters saved in a translation the app no longer offers (NIV) move to
    /// the default translation; a chapter saved in both collapses to one entry.
    private func migrateRetiredTranslations() {
        guard savedChapters.contains(where: { !BibleTranslations.isAvailable($0.translation) }) else { return }
        var seen = Set<String>()
        savedChapters = savedChapters.compactMap { chapter in
            let migrated = BibleTranslations.isAvailable(chapter.translation)
                ? chapter
                : SavedChapter(translation: BibleTranslations.defaultCode, book: chapter.book, chapter: chapter.chapter, savedAt: chapter.savedAt)
            return seen.insert(migrated.id).inserted ? migrated : nil
        }
        persist()
    }

    private func persist() {
        if let data = try? JSONEncoder().encode(savedChapters) {
            userDefaults.set(data, forKey: key)
        }
    }
}
