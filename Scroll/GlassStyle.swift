import SwiftUI

extension View {
    @ViewBuilder
    func liquidGlassCard(
        cornerRadius: CGFloat = Theme.Radius.card,
        tint: Color? = nil,
        isHighlighted: Bool = false
    ) -> some View {
        if #available(iOS 26.0, *) {
            let shape = RoundedRectangle(cornerRadius: cornerRadius)
            let effect: Glass = tint.map { .regular.tint($0) } ?? .regular
            self
                .background {
                    shape.fill(Color.clear)
                        .glassEffect(effect, in: shape)
                }
                .overlay(
                    shape.stroke(
                        isHighlighted ? Theme.Colors.accent.opacity(0.35) : Color.clear,
                        lineWidth: 1.2
                    )
                )
        } else {
            let shape = RoundedRectangle(cornerRadius: cornerRadius)
            self
                .background(
                    shape.fill(
                        isHighlighted ? Theme.Colors.accent.opacity(0.08) : Theme.Colors.cardBackground
                    )
                )
                .overlay(
                    shape.stroke(
                        isHighlighted ? Theme.Colors.accent.opacity(0.3) : Color.clear,
                        lineWidth: 1.5
                    )
                )
        }
    }

    @ViewBuilder
    func liquidGlassPill(tint: Color = Theme.Colors.accent) -> some View {
        if #available(iOS 26.0, *) {
            self.background {
                Capsule().fill(Color.clear)
                    .glassEffect(.regular.tint(tint), in: Capsule())
            }
        } else {
            self.background(
                Capsule()
                    .fill(tint)
                    .shadow(color: Color.black.opacity(0.15), radius: 8, x: 0, y: 4)
            )
        }
    }

    @ViewBuilder
    func liquidGlassCircle(tint: Color? = nil) -> some View {
        if #available(iOS 26.0, *) {
            let effect: Glass = tint.map { .regular.tint($0) } ?? .regular
            self.background {
                Circle().fill(Color.clear)
                    .glassEffect(effect, in: Circle())
            }
        } else {
            self.background(
                Circle().fill(tint ?? Theme.Colors.cardBackground)
            )
        }
    }
}
