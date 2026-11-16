import SwiftUI

/// Wraps content in a max-width reading column on iPad, no-op on iPhone.
/// Used to prevent home / chapter / verse UIs from stretching across the
/// full iPad canvas which looks like a scaled-up iPhone app.
struct ReadingColumn<Content: View>: View {
    @Environment(\.horizontalSizeClass) private var hSizeClass
    let maxWidth: CGFloat
    let content: Content

    init(maxWidth: CGFloat = 720, @ViewBuilder content: () -> Content) {
        self.maxWidth = maxWidth
        self.content = content()
    }

    var body: some View {
        content
            .frame(maxWidth: hSizeClass == .regular ? maxWidth : .infinity)
            .frame(maxWidth: .infinity)   // centre the column horizontally
    }
}

extension View {
    /// Apply reading-column max width on iPad; no-op on iPhone.
    func readingColumn(maxWidth: CGFloat = 720) -> some View {
        ReadingColumn(maxWidth: maxWidth) { self }
    }
}
