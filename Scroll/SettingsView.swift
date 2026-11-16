import SwiftUI

struct SettingsView: View {
    private var appVersion: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? ""
    }

    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 24) {
                    VStack(spacing: 4) {
                        Text("Settings")
                            .font(Theme.Typography.pageTitle())
                            .foregroundColor(.primary)
                    }
                    .padding(.top, 16)

                    // About Section
                    VStack(alignment: .leading, spacing: 12) {
                        Label("About", systemImage: "info.circle.fill")
                            .font(Theme.Typography.sectionTitle())
                            .foregroundColor(.primary)

                        Text("Scroll helps you discover Bible verses about the Fruits of the Spirit. Save your favorite verses, save full chapters, and walk book by book with the prev / next arrows.")
                            .font(.system(size: 15))
                            .foregroundColor(.secondary)
                            .lineSpacing(4)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .liquidGlassCard()
                    .padding(.horizontal, 16)

                    // Links
                    VStack(spacing: 0) {
                        settingsLink("Read on the web", systemImage: "globe", url: VerseShare.siteURL)
                        Divider().padding(.leading, 52)
                        settingsLink("Privacy Policy", systemImage: "hand.raised.fill", url: VerseShare.siteURL + "/privacy")
                    }
                    .liquidGlassCard()
                    .padding(.horizontal, 16)

                    // Version footer
                    Text("Scroll · v\(appVersion)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.secondary.opacity(0.6))
                        .padding(.top, 8)

                    Spacer(minLength: 40)
                }
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }

    @ViewBuilder
    private func settingsLink(_ title: String, systemImage: String, url: String) -> some View {
        if let destination = URL(string: url) {
            Link(destination: destination) {
                HStack(spacing: 12) {
                    Image(systemName: systemImage)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(Theme.Colors.accent)
                        .frame(width: 24)
                    Text(title)
                        .font(.system(size: 16))
                        .foregroundColor(.primary)
                    Spacer()
                    Image(systemName: "arrow.up.right")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(.secondary.opacity(0.6))
                }
                .padding(16)
                .contentShape(Rectangle())
            }
        }
    }
}
