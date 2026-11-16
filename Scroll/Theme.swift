import SwiftUI

// MARK: - App Theme
enum Theme {

    // MARK: - Colors
    enum Colors {
        static let accent = Color.teal
        static let accentLight = Color.teal.opacity(0.1)
        static let accentMedium = Color.teal.opacity(0.08)

        static let background = Color(.systemBackground)
        static let cardBackground = Color(.secondarySystemBackground)
        static let tertiaryBackground = Color(.tertiarySystemBackground)

        static let textPrimary = Color.primary
        static let textSecondary = Color.secondary

        static let destructive = Color.red
        static let destructiveLight = Color.red.opacity(0.1)

        static let success = Color.green
        static let warning = Color.orange
    }

    // MARK: - Typography
    enum Typography {
        // Headers
        static func appTitle() -> Font {
            .system(size: 34, weight: .bold, design: .rounded)
        }

        static func pageTitle() -> Font {
            .system(size: 28, weight: .semibold, design: .rounded)
        }

        static func sectionTitle() -> Font {
            .system(size: 17, weight: .semibold)
        }

        // Body
        static func body() -> Font {
            .system(size: 16, weight: .regular)
        }

        static func bodyMedium() -> Font {
            .system(size: 16, weight: .medium)
        }

        // Small
        static func caption() -> Font {
            .system(size: 15, weight: .regular)
        }

        static func small() -> Font {
            .system(size: 14, weight: .regular)
        }

        static func smallMedium() -> Font {
            .system(size: 14, weight: .medium)
        }

        static func tiny() -> Font {
            .system(size: 13, weight: .medium)
        }

        static func tinyRegular() -> Font {
            .system(size: 13, weight: .regular)
        }

        // Reference text
        static func reference() -> Font {
            .system(size: 13, weight: .semibold)
        }

        // Verse number
        static func verseNumber() -> Font {
            .system(size: 12, weight: .bold, design: .rounded)
        }
    }

    // MARK: - Spacing
    enum Spacing {
        static let xxs: CGFloat = 4
        static let xs: CGFloat = 6
        static let sm: CGFloat = 8
        static let md: CGFloat = 12
        static let lg: CGFloat = 16
        static let xl: CGFloat = 20
        static let xxl: CGFloat = 24
        static let xxxl: CGFloat = 32
    }

    // MARK: - Radius
    enum Radius {
        static let small: CGFloat = 8
        static let medium: CGFloat = 10
        static let large: CGFloat = 12
        static let xlarge: CGFloat = 14
        static let card: CGFloat = 16
    }

    // MARK: - Layout
    enum Layout {
        static let horizontalPadding: CGFloat = 16
        static let cardPadding: CGFloat = 16
        static let buttonHeight: CGFloat = 48
        static let iconButtonSize: CGFloat = 40
        static let smallIconButtonSize: CGFloat = 36
    }

    // MARK: - Line Spacing
    enum LineSpacing {
        static let verse: CGFloat = 4
    }
}

// MARK: - Button Styles

struct PrimaryButtonStyle: ButtonStyle {
    var isDestructive: Bool = false

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(Theme.Typography.bodyMedium())
            .foregroundColor(.white)
            .frame(maxWidth: .infinity)
            .frame(height: Theme.Layout.buttonHeight)
            .background(isDestructive ? Theme.Colors.destructive : Theme.Colors.accent)
            .cornerRadius(Theme.Radius.large)
            .scaleEffect(configuration.isPressed ? 0.98 : 1.0)
            .animation(.spring(response: 0.3, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

struct SecondaryButtonStyle: ButtonStyle {
    var color: Color = Theme.Colors.accent
    var isActive: Bool = false

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(Theme.Typography.tiny())
            .foregroundColor(isActive ? .white : color)
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(isActive ? color : color.opacity(0.1))
            .cornerRadius(Theme.Radius.small)
            .scaleEffect(configuration.isPressed ? 0.95 : 1.0)
            .animation(.spring(response: 0.3, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

struct IconButtonStyle: ButtonStyle {
    var backgroundColor: Color = Theme.Colors.cardBackground
    var foregroundColor: Color = Theme.Colors.accent

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 16, weight: .medium))
            .foregroundColor(foregroundColor)
            .frame(width: Theme.Layout.iconButtonSize, height: Theme.Layout.iconButtonSize)
            .background(backgroundColor)
            .clipShape(Circle())
            .scaleEffect(configuration.isPressed ? 0.9 : 1.0)
            .animation(.spring(response: 0.3, dampingFraction: 0.7), value: configuration.isPressed)
    }
}

// MARK: - Card Modifier

struct CardStyle: ViewModifier {
    var isHighlighted: Bool = false
    var highlightColor: Color = Theme.Colors.accent

    func body(content: Content) -> some View {
        content
            .padding(Theme.Layout.cardPadding)
            .background(isHighlighted ? highlightColor.opacity(0.08) : Theme.Colors.cardBackground)
            .cornerRadius(Theme.Radius.large)
            .overlay(
                RoundedRectangle(cornerRadius: Theme.Radius.large)
                    .stroke(isHighlighted ? highlightColor.opacity(0.3) : Color.clear, lineWidth: 1.5)
            )
    }
}

extension View {
    func cardStyle(isHighlighted: Bool = false, highlightColor: Color = Theme.Colors.accent) -> some View {
        modifier(CardStyle(isHighlighted: isHighlighted, highlightColor: highlightColor))
    }
}
