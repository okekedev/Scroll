import SwiftUI

struct ToastOverlay: ViewModifier {
    @Binding var message: String?
    var duration: Double = 2.0

    func body(content: Content) -> some View {
        ZStack {
            content

            if let message = message {
                VStack {
                    NotificationView(message: message)
                        .padding(.top, 60)
                    Spacer()
                }
                .transition(.move(edge: .top).combined(with: .opacity))
                .animation(.spring(response: 0.4, dampingFraction: 0.8), value: message)
                .onAppear {
                    let capturedMessage = message
                    DispatchQueue.main.asyncAfter(deadline: .now() + duration) {
                        if self.message == capturedMessage {
                            withAnimation {
                                self.message = nil
                            }
                        }
                    }
                }
            }
        }
    }
}

extension View {
    func toast(message: Binding<String?>, duration: Double = 2.0) -> some View {
        modifier(ToastOverlay(message: message, duration: duration))
    }
}
