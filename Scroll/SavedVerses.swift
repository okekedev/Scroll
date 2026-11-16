import SwiftUI

struct SavedVerses: View {
    @ObservedObject var savedVersesModel: SavedVersesModel
    @State private var toastMessage: String? = nil

    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                VStack(spacing: 4) {
                    Text("Saved Verses")
                        .font(Theme.Typography.pageTitle())
                        .foregroundColor(.primary)

                    Text("\(savedVersesModel.savedVerses.count) verses")
                        .font(Theme.Typography.small())
                        .foregroundColor(.secondary)
                }
                .padding(.top, 16)
                .padding(.bottom, 16)

                if savedVersesModel.savedVerses.isEmpty {
                    emptyStateView
                } else {
                    versesListView
                }
            }
        }
        .toast(message: $toastMessage)
        .navigationBarTitleDisplayMode(.inline)
    }

    private var emptyStateView: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "bookmark")
                .font(.system(size: 48, weight: .light))
                .foregroundColor(.secondary.opacity(0.4))
            Text("No saved verses yet")
                .font(.system(size: 17, weight: .medium))
                .foregroundColor(.secondary)
            Text("Tap Save on a verse to add it here")
                .font(Theme.Typography.small())
                .foregroundColor(.secondary.opacity(0.7))
            Spacer()
        }
    }

    private var versesListView: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(savedVersesModel.savedVerses) { verse in
                    VerseCard(
                        verse: verse,
                        action: .remove,
                        onAction: { removeVerse(verse) }
                    )
                }
            }
            .padding(.horizontal, Theme.Layout.horizontalPadding)
            .padding(.vertical, 8)
            .readingColumn()
        }
    }

    private func removeVerse(_ verse: BibleVerse) {
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
            savedVersesModel.removeVerse(verse)
        }
        toastMessage = "Verse Removed"
    }
}
