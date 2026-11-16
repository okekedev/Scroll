import SwiftUI

enum VerseCardAction: Equatable {
    case save(isSaved: Bool)
    case remove
}

struct VerseCard: View {
    let verse: BibleVerse
    var isPlaying: Bool = false
    var action: VerseCardAction
    let onAction: () -> Void

    var body: some View {
        NavigationLink(destination: ChapterView(verse: verse)) {
            VStack(alignment: .leading, spacing: 12) {
                Text(verse.text)
                    .font(Theme.Typography.body())
                    .foregroundColor(.primary)
                    .lineSpacing(Theme.LineSpacing.verse)
                    .multilineTextAlignment(.leading)

                HStack {
                    Text(verse.reference)
                        .font(Theme.Typography.reference())
                        .foregroundColor(Theme.Colors.accent)

                    if isPlaying {
                        Image(systemName: "speaker.wave.2.fill")
                            .font(.system(size: 12))
                            .foregroundColor(Theme.Colors.accent)
                    }

                    Spacer()

                    shareButton
                    actionButton
                }
            }
            .padding(Theme.Layout.cardPadding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .liquidGlassCard(isHighlighted: isPlaying)
        }
        .buttonStyle(.plain)
    }

    private var shareButton: some View {
        ShareLink(item: VerseShare.text(for: verse)) {
            Image(systemName: "square.and.arrow.up")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(Theme.Colors.accent)
                .frame(width: 34, height: 34)
                .background(Theme.Colors.accentLight, in: RoundedRectangle(cornerRadius: 8))
        }
        .buttonStyle(BorderlessButtonStyle())
        .accessibilityLabel("Share verse")
    }

    private var actionButton: some View {
        Button(action: onAction) {
            HStack(spacing: 6) {
                Image(systemName: actionIcon)
                    .font(.system(size: 14, weight: .medium))
                Text(actionLabel)
                    .font(.system(size: 13, weight: .medium))
            }
            .foregroundColor(actionForeground)
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(actionBackground, in: RoundedRectangle(cornerRadius: 8))
        }
        .buttonStyle(BorderlessButtonStyle())
    }

    private var actionIcon: String {
        switch action {
        case .save(let isSaved): return isSaved ? "bookmark.fill" : "bookmark"
        case .remove: return "bookmark.slash"
        }
    }

    private var actionLabel: String {
        switch action {
        case .save(let isSaved): return isSaved ? "Saved" : "Save"
        case .remove: return "Remove"
        }
    }

    private var actionForeground: Color {
        switch action {
        case .save(let isSaved): return isSaved ? .white : Theme.Colors.accent
        case .remove: return Theme.Colors.destructive
        }
    }

    private var actionBackground: Color {
        switch action {
        case .save(let isSaved): return isSaved ? Theme.Colors.accent : Theme.Colors.accentLight
        case .remove: return Theme.Colors.destructiveLight
        }
    }
}
