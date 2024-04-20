// SavedVerses.swift
import SwiftUI

struct SavedVerses: View {
    @ObservedObject var savedVersesModel: SavedVersesModel
    @State private var showUnsavedNotification = false
    
    var body: some View {
        VStack {
            Text("Saved Verses")
                .font(.system(size: 38, weight: .medium, design: .monospaced))
                .foregroundColor(.secondary)
                .padding(.top, 20)
            
            List {
                ForEach(savedVersesModel.savedVerses, id: \.self) { verse in
                    VStack(alignment: .leading) {
                        Text(verse.text)
                            .font(.headline)
                            .foregroundColor(.blue)
                        Text(verse.reference)
                            .font(.subheadline)
                            .foregroundColor(.gray)
                            .padding(.top, 1)
                    }
                    .padding(.vertical, 5)
                    .onTapGesture(count: 2) {
                        savedVersesModel.removeVerse(verse)
                        showUnsavedNotification = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            showUnsavedNotification = false
                        }
                    }
                }
            }
            
            if showUnsavedNotification {
                NotificationView(message: "Verse Unsaved")
                    .padding()
                    .transition(.move(edge: .top))
            }
        }
    }
}
