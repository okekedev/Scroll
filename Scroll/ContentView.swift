import SwiftUI

struct ContentView: View {
    @StateObject private var savedVersesModel = SavedVersesModel()
    @State private var isActive = false

    var body: some View {
        
        NavigationView {
            ScrollView(showsIndicators: false) {
                VStack {
                    Image(systemName: "applescript.fill")
                        .imageScale(.large)
                        .symbolRenderingMode(.hierarchical)
                        .foregroundColor(.secondary)
                        .font(.system(size: 33, weight: .regular, design: .default))
                        .padding(.bottom, 0)
                    Text(".Scroll")
                        .font(.system(size: 38, weight: .medium, design: .monospaced))
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                        .clipped()
                        .padding(.leading, 0)
                        .padding(0)
                        .foregroundColor(.secondary)
                    Text("Looking for...")
                        .font(.system(.headline, design: .serif, weight: .ultraLight))
                        .foregroundColor(.teal)
                        .padding(.top)
                    HStack {
                        NavigationLink(destination:LoveView(savedVersesModel: savedVersesModel)) {
                            VStack {
                                Image(systemName: "figure.and.child.holdinghands")
                                    .imageScale(.large)
                                    .symbolRenderingMode(.hierarchical)
                                    .font(.system(size: 50, weight: .regular, design: .default))
                                    .foregroundColor(.secondary)
                                Text("Love")
                                    .font(.system(size: 20, weight: .regular, design: .serif))
                                    .padding(.top, 0)
                                    .foregroundColor(.secondary)
                            }
                            
                        } .padding()
                        NavigationLink(destination: JoyView(savedVersesModel:savedVersesModel)) {
                            VStack {
                                Image(systemName: "face.smiling")
                                    .imageScale(.large)
                                    .symbolRenderingMode(.hierarchical)
                                    .font(.system(size: 50, weight: .regular, design: .default))
                                    .foregroundColor(.blue)
                                Text("Joy")
                                    .font(.system(size: 20, weight: .regular, design: .serif))
                                    .padding(.top, 0)
                                    .foregroundColor(.secondary)
                            }
                        } .padding()
                        NavigationLink(destination: PeaceView(savedVersesModel: savedVersesModel)) {                        VStack {
                            Image(systemName: "powersleep")
                                .imageScale(.large)
                                .symbolRenderingMode(.multicolor)
                                .font(.system(size: 50, weight: .regular, design: .default))
                                .foregroundColor(Color(.teal))
                            Text("Peace")
                                .font(.system(size: 20, weight: .regular, design: .serif))
                                .padding(.top, 0)
                                .foregroundColor(.secondary)
                        }
                        } .padding()
                    }
                    .padding()
                    HStack {
                        NavigationLink(destination: PatienceView(savedVersesModel:savedVersesModel)) {
                            VStack {
                                Image(systemName: "clock")
                                    .imageScale(.large)
                                    .symbolRenderingMode(.hierarchical)
                                    .font(.system(size: 50, weight: .regular, design: .default))
                                    .foregroundColor(.secondary)
                                Text("Patience")
                                    .font(.system(size: 20, weight: .regular, design: .serif))
                                    .padding(.top, 0)
                                    .foregroundColor(.secondary)
                            }} .padding()
                        NavigationLink(destination: KindnessView(savedVersesModel:savedVersesModel)) {
                            VStack {
                                Image(systemName: "heart")
                                    .imageScale(.large)
                                    .symbolRenderingMode(.hierarchical)
                                    .font(.system(size: 50, weight: .regular, design: .default))
                                    .foregroundColor(.blue)
                                Text("Kindness")
                                    .font(.system(size: 20, weight: .regular, design: .serif))
                                    .padding(.top, 0)
                                    .foregroundColor(.secondary)
                            }} .padding()
                        NavigationLink(destination: GoodnessView(savedVersesModel:savedVersesModel)) {
                            VStack {
                                Image(systemName: "globe.europe.africa")
                                    .imageScale(.large)
                                    .symbolRenderingMode(.hierarchical)
                                    .font(.system(size: 50, weight: .regular, design: .default))
                                    .foregroundColor(.teal)
                                Text("Goodness")
                                    .font(.system(size: 20, weight: .regular, design: .serif))
                                    .padding(.top, 0)
                                    .foregroundColor(.secondary)
                            }
                        } .padding()
                    }
                    .padding()
                    HStack {
                        
                        NavigationLink(destination: FaithfullView(savedVersesModel:savedVersesModel)) {
                            VStack {
                                Image(systemName: "fish")
                                    .imageScale(.large)
                                    .symbolRenderingMode(.hierarchical)
                                    .font(.system(size: 50, weight: .regular, design: .default))
                                    .foregroundColor(.secondary)
                                Text("Faith")
                                    .font(.system(size: 20, weight: .regular, design: .serif))
                                    .padding(.top, 0)
                                    .foregroundColor(.secondary)
                            }
                        } .padding()
                        NavigationLink(destination: GentlenessView(savedVersesModel:savedVersesModel)) {
                            VStack {
                                Image(systemName: "person.icloud")
                                    .imageScale(.large)
                                    .symbolRenderingMode(.hierarchical)
                                    .font(.system(size: 50, weight: .regular, design: .default))
                                    .foregroundColor(.blue)
                                Text("Gentleness")
                                    .font(.system(size: 14, weight: .regular, design: .serif))
                                    .padding(.top, 0)
                                    .foregroundColor(.secondary)
                            }
                        } .padding()
                        NavigationLink(destination: SelfControlView(savedVersesModel:savedVersesModel)) {
                            VStack {
                                Image(systemName: "shareplay")
                                    .imageScale(.large)
                                    .symbolRenderingMode(.hierarchical)
                                    .font(.system(size: 50, weight: .regular, design: .default))
                                    .foregroundColor(.teal)
                                Text("Self-Control")
                                    .font(.system(size: 14, weight: .regular, design: .serif))
                                    .padding(.top, 0)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding()
                    }
                    Text("Double Tap to save a verse")
                        .font(.system(.caption2, weight: .ultraLight))
                        .foregroundColor(.secondary)
                        .padding()
                    NavigationLink(destination: SavedVerses(savedVersesModel: savedVersesModel)) {
                        Capsule(style: .continuous)
                            .fill(.teal)
                            .frame(minWidth: 240, idealWidth: 280, maxWidth: .infinity, minHeight: 40, idealHeight: 50, maxHeight: 60)
                            .overlay(
                                Text("Saved Verses")
                                    .foregroundColor(.white)
                                    .font(.headline)
                                    .padding(.horizontal, 70)
                            )
                            .padding(.horizontal, 20)
                    }

                
                }
                .frame(maxWidth: .infinity)
                .clipped()
                .padding(.top, 58)
            }
            .overlay(alignment: .top) {
                HStack {
                    Spacer()
                }
                .padding(.horizontal)
                .padding(.top, 5)
                .frame(maxWidth: .infinity)
                .clipped()
                .frame(height: 53)
                .clipped()
                .background(.clear)
            }
            .overlay(alignment: .bottom) {
                Group {
                    
                }
            }
            .background {
                RoundedRectangle(cornerRadius: 4, style: .continuous)
                    .fill(Color(.systemGray6))
            }
            .ignoresSafeArea()
        }
    }
    
    struct ContentView_Previews: PreviewProvider {
        static var previews: some View {
            ContentView()
        }
    }
}
