// LoveView.swift
import SwiftUI

struct LoveView: View {
    @ObservedObject var savedVersesModel: SavedVersesModel
    @State private var bibleVerses: [BibleVerse] = []
    @State private var selectedVersion = "NIV"
    
    @State private var isVerseSaved = false
    @State private var showNotification = false
    
    let bibleVersions = ["NIV", "KJV", "ESV", "NASB", "NLT"]

    var body: some View {
        VStack {
            HStack {
                Text(".Scroll")
                    .font(.system(size: 28, weight: .medium, design: .monospaced))
                    .foregroundColor(.secondary)
        
                Button(action: {
                    // Refresh the page by shuffling the verses
                    self.bibleVerses.shuffle()
                }) {
                    Image(systemName: "arrow.clockwise.circle")
                        .font(.title)
                        .foregroundColor(.gray)
                }
                .padding()
                
                Picker("Version", selection: $selectedVersion) {
                    ForEach(bibleVersions, id: \.self) { version in
                        Text(version)
                            .font(.system(.headline, design: .serif, weight: .ultraLight))
                            .foregroundColor(.teal)
                            .padding(.top)
                    }
                }
                .onChange(of: selectedVersion) {
                    searchBibleVerses()
                }
            }
            
            if bibleVerses.isEmpty {
                ProgressView()
            }
            else {
                List {
                    ForEach(bibleVerses, id: \.self) { verse in
                        VStack(alignment: .leading) {
                            Text(verse.text)
                                .font(.headline)
                            Text(verse.reference)
                                .font(.subheadline)
                                .foregroundColor(.gray)
                                .padding(.top, 1)
                        }
                        .padding(.vertical, 5)
                        .foregroundColor(.teal)
                        .onTapGesture(count: 2) {
                            saveVerse(verse)
                        }
                    }
                }
            }
            if showNotification {
                NotificationView(message: "Verse Saved")
                    .padding()
                    .transition(.move(edge: .top))
            }
        }
        .onAppear {
            searchBibleVerses()
        }
    }

    func searchBibleVerses() {
        guard let url = URL(string: "https://bolls.life/find/\(selectedVersion)/?search=love&match_case=false&match_whole=false&limit=20") else {
            return
        }
        
        let request = URLRequest(url: url)
        
        let session = URLSession.shared
        
        let task = session.dataTask(with: request) { (data, response, error) in
            if let error = error {
                print("Error fetching Bible verses: \(error)")
                return
            }
            
            guard let data = data else {
                print("No data received from Bible verses request")
                return
            }
            
            let decoder = JSONDecoder()
            do {
                let verses = try decoder.decode([BibleVerse].self, from: data)
                
                DispatchQueue.main.async {
                    self.bibleVerses = verses.shuffled()
                }
            } catch {
                print("Error decoding Bible verses: \(error)")
            }
        }
        
        task.resume()
    }
    
    func saveVerse(_ verse: BibleVerse) {
        savedVersesModel.saveVerse(verse)
        showNotification = true
        isVerseSaved = true
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            showNotification = false
        }
    }
}
