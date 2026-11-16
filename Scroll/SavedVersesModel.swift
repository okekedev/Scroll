// SavedVersesModel.swift
import Foundation

class SavedVersesModel: ObservableObject {
    @Published var savedVerses: [BibleVerse] = []

    private let userDefaults = UserDefaults.standard
    private let savedVersesKey = "savedVerses"

    init() {
        loadSavedVerses()
        migrateRetiredTranslations()
    }

    func saveVerse(_ verse: BibleVerse) {
        guard !savedVerses.contains(where: { $0.id == verse.id }) else { return }
        savedVerses.append(verse)
        saveSavedVerses()
    }

    func removeVerse(_ verse: BibleVerse) {
        if let index = savedVerses.firstIndex(of: verse) {
            savedVerses.remove(at: index)
            saveSavedVerses()
        }
    }

    private func loadSavedVerses() {
        if let data = userDefaults.data(forKey: savedVersesKey),
           let verses = try? JSONDecoder().decode([BibleVerse].self, from: data) {
            savedVerses = verses
        }
    }

    private func saveSavedVerses() {
        if let data = try? JSONEncoder().encode(savedVerses) {
            userDefaults.set(data, forKey: savedVersesKey)
        }
    }

    /// Verses saved in a translation the app no longer offers (NIV) are swapped
    /// for the same verse in the default translation. Each verse is re-fetched
    /// on its own; any that fail (offline) stay as they are and are retried on
    /// the next launch.
    private func migrateRetiredTranslations() {
        let target = BibleTranslations.defaultCode
        for old in savedVerses where !BibleTranslations.isAvailable(old.translation) {
            BollsClient.fetchVerse(translation: target, book: old.book, chapter: old.chapter, verse: old.verse) { [weak self] fetched in
                guard let self, let fetched,
                      let index = self.savedVerses.firstIndex(where: { $0.id == old.id }) else { return }
                if self.savedVerses.contains(where: { $0.id == fetched.id }) {
                    // Already saved in the default translation too; drop the old copy.
                    self.savedVerses.remove(at: index)
                } else {
                    self.savedVerses[index] = BibleVerse(
                        id: fetched.id,
                        translation: target,
                        book: old.book,
                        chapter: old.chapter,
                        verse: old.verse,
                        text: fetched.text,
                        comment: nil
                    )
                }
                self.saveSavedVerses()
            }
        }
    }
}
