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
                    .padding(16)
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
}
