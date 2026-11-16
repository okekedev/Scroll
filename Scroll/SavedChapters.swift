import SwiftUI

struct SavedChapters: View {
    @ObservedObject var savedChaptersModel: SavedChaptersModel

    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                VStack(spacing: 4) {
                    Text("Saved Chapters")
                        .font(Theme.Typography.pageTitle())
                        .foregroundColor(.primary)

                    Text("\(savedChaptersModel.savedChapters.count) chapters")
                        .font(Theme.Typography.small())
                        .foregroundColor(.secondary)
                }
                .padding(.top, 16)
                .padding(.bottom, 16)

                if savedChaptersModel.savedChapters.isEmpty {
                    emptyStateView
                } else {
                    chaptersListView
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }

    private var emptyStateView: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "book.pages")
                .font(.system(size: 48, weight: .light))
                .foregroundColor(.secondary.opacity(0.4))
            Text("No saved chapters yet")
                .font(.system(size: 17, weight: .medium))
                .foregroundColor(.secondary)
            Text("Tap the bookmark in a chapter to save it")
                .font(Theme.Typography.small())
                .foregroundColor(.secondary.opacity(0.7))
            Spacer()
        }
    }

    private var chaptersListView: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(savedChaptersModel.savedChapters) { chapter in
                    NavigationLink(destination: ChapterView(translation: chapter.translation, book: chapter.book, chapter: chapter.chapter)) {
                        SavedChapterRow(chapter: chapter, onRemove: {
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                                savedChaptersModel.remove(chapter)
                            }
                        })
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, Theme.Layout.horizontalPadding)
            .padding(.vertical, 8)
            .readingColumn()
        }
    }
}

private struct SavedChapterRow: View {
    let chapter: SavedChapter
    let onRemove: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "book.pages.fill")
                .font(.system(size: 20, weight: .medium))
                .foregroundColor(Theme.Colors.accent)
                .frame(width: 44, height: 44)
                .background(Theme.Colors.accentLight, in: Circle())

            VStack(alignment: .leading, spacing: 2) {
                Text(chapter.reference)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.primary)
                Text(chapter.translation)
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.secondary)
            }

            Spacer()

            Button(action: onRemove) {
                Image(systemName: "bookmark.slash")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(Theme.Colors.destructive)
                    .frame(width: 36, height: 36)
                    .background(Theme.Colors.destructiveLight, in: Circle())
            }
            .buttonStyle(.borderless)
        }
        .padding(12)
        .liquidGlassCard()
    }
}
