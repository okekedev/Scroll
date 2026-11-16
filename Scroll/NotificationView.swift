import SwiftUI

struct NotificationView: View {
    var message: String

    private var iconName: String {
        if message.contains("Removed") { return "bookmark.slash.fill" }
        if message.contains("Copied") { return "doc.on.doc.fill" }
        if message.contains("Refreshed") { return "arrow.clockwise" }
        return "bookmark.fill"
    }

    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: iconName)
                .font(.system(size: 14, weight: .semibold))

            Text(message)
                .font(.system(size: 15, weight: .semibold))
        }
        .foregroundColor(.white)
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
        .liquidGlassPill(tint: Theme.Colors.accent)
    }
}
