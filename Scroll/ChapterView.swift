import SwiftUI

struct ChapterView: View {
    private let initialTranslation: String
    private let initialBook: Int
    private let initialChapter: Int
    private let targetVerse: Int?

    @EnvironmentObject private var savedChaptersModel: SavedChaptersModel
    @State private var currentBook: Int
    @State private var currentChapter: Int
    @State private var chapterVerses: [ChapterVerse] = []
    @State private var isLoading = true
    @State private var toastMessage: String? = nil
    @State private var didScrollToTarget = false

    init(verse: BibleVerse) {
        self.initialTranslation = BibleTranslations.resolve(verse.translation)
        self.initialBook = verse.book
        self.initialChapter = verse.chapter
        self.targetVerse = verse.verse
        self._currentBook = State(initialValue: verse.book)
        self._currentChapter = State(initialValue: verse.chapter)
    }

    init(translation: String, book: Int, chapter: Int) {
        self.initialTranslation = BibleTranslations.resolve(translation)
        self.initialBook = book
        self.initialChapter = chapter
        self.targetVerse = nil
        self._currentBook = State(initialValue: book)
        self._currentChapter = State(initialValue: chapter)
    }

    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()

            if isLoading {
                loadingView
            } else {
                chapterContentView
            }
        }
        .toast(message: $toastMessage)
        .navigationTitle(chapterTitle)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                toolbarButton(system: isChapterSaved ? "bookmark.fill" : "bookmark",
                              foreground: isChapterSaved ? .white : Theme.Colors.accent,
                              background: isChapterSaved ? Theme.Colors.accent : Theme.Colors.accentLight,
                              action: toggleSaved)

                toolbarButton(system: "arrow.clockwise",
                              foreground: Theme.Colors.accent,
                              background: Theme.Colors.cardBackground,
                              action: { fetchChapterVerses(showLoading: true) })

                toolbarButton(system: "chevron.left",
                              foreground: canGoPrevious ? Theme.Colors.accent : Color.secondary.opacity(0.4),
                              background: Theme.Colors.cardBackground,
                              action: goPrevious)
                    .disabled(!canGoPrevious)

                toolbarButton(system: "chevron.right",
                              foreground: canGoNext ? Theme.Colors.accent : Color.secondary.opacity(0.4),
                              background: Theme.Colors.cardBackground,
                              action: goNext)
                    .disabled(!canGoNext)
            }
        }
        .onAppear {
            fetchChapterVerses()
        }
    }

    private var chapterTitle: String {
        "\(BibleBooks.name(for: currentBook)) \(currentChapter)"
    }

    private var isChapterSaved: Bool {
        savedChaptersModel.isSaved(translation: initialTranslation, book: currentBook, chapter: currentChapter)
    }

    private var canGoPrevious: Bool {
        BibleBooks.previous(book: currentBook, chapter: currentChapter) != nil
    }

    private var canGoNext: Bool {
        BibleBooks.next(book: currentBook, chapter: currentChapter) != nil
    }

    private func toolbarButton(system: String, foreground: Color, background: Color, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: system)
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(foreground)
                .frame(width: Theme.Layout.smallIconButtonSize, height: Theme.Layout.smallIconButtonSize)
                .background(background)
                .clipShape(Circle())
        }
    }

    private var loadingView: some View {
        VStack(spacing: 12) {
            ProgressView()
                .scaleEffect(1.2)
                .tint(Theme.Colors.accent)
            Text("Loading chapter...")
                .font(Theme.Typography.small())
                .foregroundColor(.secondary)
        }
    }

    private var chapterContentView: some View {
        ScrollViewReader { proxy in
            ScrollView {
                chapterVersesView
            }
            .onAppear { scrollToTargetVerse(with: proxy) }
        }
    }

    /// Opening a chapter from a verse brings that verse into view (once).
    private func scrollToTargetVerse(with proxy: ScrollViewProxy) {
        guard !didScrollToTarget,
              let targetVerse,
              currentBook == initialBook, currentChapter == initialChapter,
              let target = chapterVerses.first(where: { $0.verse == targetVerse }) else { return }
        didScrollToTarget = true
        DispatchQueue.main.async {
            proxy.scrollTo(target.id, anchor: .center)
        }
    }

    private var chapterVersesView: some View {
        LazyVStack(alignment: .leading, spacing: 0) {
            ForEach(Array(chapterVerses.enumerated()), id: \.element.id) { _, chapterVerse in
                let isSelectedVerse = targetVerse != nil && chapterVerse.verse == targetVerse && currentBook == initialBook && currentChapter == initialChapter

                HStack(alignment: .top, spacing: 12) {
                    Text("\(chapterVerse.verse)")
                        .font(Theme.Typography.verseNumber())
                        .foregroundColor(isSelectedVerse ? Theme.Colors.accent : .secondary)
                        .frame(width: 28, alignment: .trailing)

                    Text(chapterVerse.text)
                        .font(Theme.Typography.body())
                        .foregroundColor(.primary)
                        .lineSpacing(Theme.LineSpacing.verse)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, Theme.Layout.horizontalPadding)
                .padding(.vertical, 12)
                .background(
                    RoundedRectangle(cornerRadius: 8)
                        .fill(isSelectedVerse ? Theme.Colors.accentMedium : Color.clear)
                )
                .contentShape(Rectangle())
                .contextMenu {
                    ShareLink(item: shareText(for: chapterVerse)) {
                        Label("Share Verse", systemImage: "square.and.arrow.up")
                    }
                    Button {
                        UIPasteboard.general.string = shareText(for: chapterVerse)
                        toastMessage = "Verse Copied"
                    } label: {
                        Label("Copy Verse", systemImage: "doc.on.doc")
                    }
                }
                .readingColumn()
            }
        }
        .padding(.vertical, 8)
    }

    private func shareText(for chapterVerse: ChapterVerse) -> String {
        VerseShare.text(chapterVerse.text,
                        book: currentBook,
                        chapter: currentChapter,
                        verse: chapterVerse.verse,
                        translation: initialTranslation)
    }

    private func fetchChapterVerses(showLoading: Bool = true) {
        if showLoading { isLoading = true }
        chapterVerses = []
        BollsClient.fetchChapter(translation: initialTranslation, book: currentBook, chapter: currentChapter) { verses in
            isLoading = false
            self.chapterVerses = verses
        }
    }

    private func toggleSaved() {
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
        savedChaptersModel.toggle(translation: initialTranslation, book: currentBook, chapter: currentChapter)
    }

    private func goPrevious() {
        guard let loc = BibleBooks.previous(book: currentBook, chapter: currentChapter) else { return }
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
        currentBook = loc.book
        currentChapter = loc.chapter
        fetchChapterVerses()
    }

    private func goNext() {
        guard let loc = BibleBooks.next(book: currentBook, chapter: currentChapter) else { return }
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
        currentBook = loc.book
        currentChapter = loc.chapter
        fetchChapterVerses()
    }
}
