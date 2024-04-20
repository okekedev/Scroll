// SavedVersesModel.swift
import Foundation

class SavedVersesModel: ObservableObject {
    @Published var savedVerses: [BibleVerse] = []
    
    private let userDefaults = UserDefaults.standard
    private let savedVersesKey = "savedVerses"
    
    init() {
        loadSavedVerses()
    }
    
    func saveVerse(_ verse: BibleVerse) {
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
}
