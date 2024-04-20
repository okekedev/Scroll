import SwiftUI

@main
struct ReadApp: App {
    @State private var isLoading = true
    
    var body: some Scene {
        WindowGroup {
            ZStack {
                ContentView()
                
                if isLoading {
                    LoadingView()
                }
            }
            .onAppear {
                // Simulate a loading delay for demonstration purposes
                DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                    withAnimation {
                        isLoading = false
                    }
                }
            }
        }
    }
}

struct LoadingView: View {
    @State private var loadingProgress = 0.0
    
    var body: some View {
        ZStack {
            Color(.systemBackground).edgesIgnoringSafeArea(.all)
            
            VStack {
                Text("Scroll")
                    .font(.system(size: 38, weight: .medium, design: .monospaced))
                    .foregroundColor(.secondary)
                    .padding(.bottom, 20)
                
                ProgressView()
                    .scaleEffect(2)
                    .progressViewStyle(CircularProgressViewStyle(tint: .teal))
            }
        }
        .onAppear {
            withAnimation(Animation.easeInOut(duration: 2.0).repeatForever()) {
                loadingProgress = 1.0
            }
        }
    }
}
