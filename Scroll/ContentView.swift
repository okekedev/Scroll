import SwiftUI

struct Virtue: Identifiable {
    let id = UUID()
    let name: String
    let searchTerm: String
    let icon: String
    let color: Color
}

struct ContentView: View {
    @StateObject private var savedVersesModel = SavedVersesModel()
    @StateObject private var savedChaptersModel = SavedChaptersModel()

    private let virtues: [Virtue] = [
        Virtue(name: "Love", searchTerm: "love", icon: "heart.fill", color: .pink),
        Virtue(name: "Joy", searchTerm: "joy", icon: "face.smiling.fill", color: .yellow),
        Virtue(name: "Peace", searchTerm: "peace", icon: "leaf.fill", color: .green),
        Virtue(name: "Patience", searchTerm: "patience", icon: "clock.fill", color: .orange),
        Virtue(name: "Kindness", searchTerm: "kindness", icon: "hand.raised.fill", color: .blue),
        Virtue(name: "Goodness", searchTerm: "goodness", icon: "sparkles", color: .purple),
        Virtue(name: "Faith", searchTerm: "faith", icon: "star.fill", color: .indigo),
        Virtue(name: "Gentleness", searchTerm: "gentleness", icon: "cloud.fill", color: .cyan),
        Virtue(name: "Self-Control", searchTerm: "self-control", icon: "shield.fill", color: .mint)
    ]

    private let columns = [
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12),
        GridItem(.flexible(), spacing: 12)
    ]

    var body: some View {
        NavigationView {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 24) {
                    headerView

                    LazyVGrid(columns: columns, spacing: 12) {
                        ForEach(virtues) { virtue in
                            NavigationLink(destination: VirtueView(
                                searchTerm: virtue.searchTerm,
                                title: virtue.name,
                                savedVersesModel: savedVersesModel
                            )) {
                                VirtueCard(virtue: virtue)
                            }
                            .buttonStyle(ScaleButtonStyle())
                        }
                    }
                    .padding(.horizontal, Theme.Layout.horizontalPadding)

                    savedSection
                        .padding(.horizontal, Theme.Layout.horizontalPadding)
                        .padding(.top, 8)
                        .padding(.bottom, 32)
                }
                .readingColumn()
            }
            .background(Color(.systemBackground))
            .navigationBarHidden(true)
        }
        .navigationViewStyle(.stack)
        .environmentObject(savedChaptersModel)
    }

    private var headerView: some View {
        VStack(spacing: 8) {
            HStack {
                Spacer()
                NavigationLink(destination: SettingsView()) {
                    Image(systemName: "gearshape.fill")
                        .font(.system(size: 22, weight: .regular))
                        .foregroundColor(.secondary)
                        .frame(width: 44, height: 44)
                }
            }
            .padding(.horizontal, Theme.Layout.horizontalPadding)
            .padding(.top, 8)

            Image(systemName: "book.closed.fill")
                .font(.system(size: 36, weight: .regular))
                .foregroundColor(Theme.Colors.accent)

            Text("Scroll")
                .font(Theme.Typography.appTitle())
                .foregroundColor(.primary)

            Text("Fruits of the Spirit")
                .font(.system(size: 15, weight: .regular))
                .foregroundColor(.secondary)
                .padding(.bottom, 8)
        }
    }

    private var savedSection: some View {
        HStack(spacing: 12) {
            NavigationLink(destination: SavedVerses(savedVersesModel: savedVersesModel)) {
                savedTile(
                    icon: "bookmark.fill",
                    title: "Saved Verses",
                    count: savedVersesModel.savedVerses.count
                )
            }
            .buttonStyle(ScaleButtonStyle())

            NavigationLink(destination: SavedChapters(savedChaptersModel: savedChaptersModel)) {
                savedTile(
                    icon: "book.pages.fill",
                    title: "Saved Chapters",
                    count: savedChaptersModel.savedChapters.count
                )
            }
            .buttonStyle(ScaleButtonStyle())
        }
    }

    private func savedTile(icon: String, title: String, count: Int) -> some View {
        VStack(spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                    .font(.system(size: 16, weight: .medium))
                Text(title)
                    .font(.system(size: 15, weight: .semibold))
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
            }
            .foregroundColor(.white)

            Text("\(count)")
                .font(.system(size: 22, weight: .bold, design: .rounded))
                .foregroundColor(.white.opacity(0.9))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .liquidGlassCard(cornerRadius: 14, tint: Theme.Colors.accent)
    }
}

struct VirtueCard: View {
    @Environment(\.horizontalSizeClass) private var hSizeClass
    let virtue: Virtue

    private var isPad: Bool { hSizeClass == .regular }

    var body: some View {
        VStack(spacing: isPad ? 16 : 10) {
            ZStack {
                Circle()
                    .fill(virtue.color.opacity(0.15))
                    .frame(width: isPad ? 88 : 56, height: isPad ? 88 : 56)

                Image(systemName: virtue.icon)
                    .font(.system(size: isPad ? 36 : 24, weight: .medium))
                    .foregroundColor(virtue.color)
            }

            Text(virtue.name)
                .font(.system(size: isPad ? 18 : 13, weight: .medium))
                .foregroundColor(.primary)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, isPad ? 32 : 16)
        .liquidGlassCard()
    }
}

struct ScaleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.spring(response: 0.3, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
