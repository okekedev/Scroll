import SwiftUI

struct VirtueView: View {
    let searchTerm: String
    let title: String

    @ObservedObject var savedVersesModel: SavedVersesModel
    @ObservedObject private var speechService = SpeechService.shared
    @State private var bibleVerses: [BibleVerse] = []
    @State private var isLoading = false
    @State private var selectedVersion = BibleTranslations.defaultCode
    @State private var toastMessage: String? = nil

    let bibleVersions = BibleTranslations.available

    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()

            VStack(spacing: 0) {
                headerView

                if bibleVerses.isEmpty {
                    loadingView
                } else {
                    versesListView
                }
            }
        }
        .toast(message: $toastMessage)
        .onAppear {
            if bibleVerses.isEmpty {
                loadVerses()
            }
        }
        .onDisappear {
            speechService.stop()
        }
        .navigationBarTitleDisplayMode(.inline)
    }

    private var headerView: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                Text(title)
                    .font(Theme.Typography.pageTitle())
                    .foregroundColor(.primary)

                Spacer()

                iconButton(system: "shuffle", foreground: Theme.Colors.accent, background: Theme.Colors.cardBackground) {
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                        bibleVerses.shuffle()
                    }
                }

                Button(action: { loadVerses(showToast: true) }) {
                    Group {
                        if isLoading {
                            ProgressView()
                                .tint(Theme.Colors.accent)
                        } else {
                            Image(systemName: "arrow.clockwise")
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(Theme.Colors.accent)
                        }
                    }
                    .frame(width: Theme.Layout.iconButtonSize, height: Theme.Layout.iconButtonSize)
                    .background(Theme.Colors.cardBackground)
                    .clipShape(Circle())
                }
                .disabled(isLoading)
            }

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(bibleVersions, id: \.self) { version in
                        versionButton(version)
                    }
                }
            }
        }
        .padding(.horizontal, Theme.Layout.horizontalPadding)
        .padding(.vertical, 12)
        .background(Color(.systemBackground))
    }

    private func iconButton(system: String, foreground: Color, background: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: system)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(foreground)
                .frame(width: Theme.Layout.iconButtonSize, height: Theme.Layout.iconButtonSize)
                .background(background)
                .clipShape(Circle())
        }
    }

    private func versionButton(_ version: String) -> some View {
        Button(action: {
            withAnimation(.easeInOut(duration: 0.2)) {
                selectedVersion = version
            }
            loadVerses()
        }) {
            Text(version)
                .font(.system(size: 13, weight: selectedVersion == version ? .semibold : .regular))
                .foregroundColor(selectedVersion == version ? .white : .secondary)
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(
                    Capsule()
                        .fill(selectedVersion == version ? Theme.Colors.accent : Theme.Colors.cardBackground)
                )
        }
    }

    private var loadingView: some View {
        VStack(spacing: 12) {
            Spacer()
            ProgressView()
                .scaleEffect(1.2)
                .tint(Theme.Colors.accent)
            Text("Loading verses...")
                .font(Theme.Typography.small())
                .foregroundColor(.secondary)
            Spacer()
        }
    }

    private var versesListView: some View {
        ScrollView {
            LazyVStack(spacing: 12) {
                ForEach(Array(bibleVerses.enumerated()), id: \.element.id) { index, verse in
                    let isSaved = savedVersesModel.savedVerses.contains(where: { $0.id == verse.id })
                    VerseCard(
                        verse: verse,
                        isPlaying: speechService.isSpeaking && speechService.currentVerseIndex == index,
                        action: .save(isSaved: isSaved),
                        onAction: { saveVerse(verse) }
                    )
                }
            }
            .padding(.horizontal, Theme.Layout.horizontalPadding)
            .padding(.vertical, 8)
            .readingColumn()
        }
    }

    private func loadVerses(showToast: Bool = false) {
        speechService.stop()
        isLoading = true
        BollsClient.searchVerses(version: selectedVersion, term: searchTerm) { verses in
            isLoading = false
            withAnimation(.easeInOut(duration: 0.3)) {
                bibleVerses = verses.shuffled()
            }
            if showToast {
                toastMessage = "Refreshed"
            }
        }
    }

    private func saveVerse(_ verse: BibleVerse) {
        savedVersesModel.saveVerse(verse)
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        toastMessage = "Verse Saved"
    }

}
