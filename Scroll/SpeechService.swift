import AVFoundation
import SwiftUI

struct AvailableVoice: Identifiable, Equatable {
    let id: String
    let name: String
    let quality: String

    var displayName: String {
        quality.isEmpty ? name : "\(name) (\(quality))"
    }
}

/// Apple-TTS-only speech service. Ships with a single natural voice (Samantha,
/// installed by default on every iPhone). Users can install Premium voices in
/// iOS Settings → Accessibility → Spoken Content → Voices; those show up
/// automatically the next time the app launches.
final class SpeechService: NSObject, ObservableObject, AVSpeechSynthesizerDelegate {
    static let shared = SpeechService()

    private let synthesizer = AVSpeechSynthesizer()

    @Published var isSpeaking = false
    @Published var isLoadingVoice = false
    @Published var currentVerseIndex: Int = 0
    @Published var selectedVoiceId: String = "" {
        didSet { UserDefaults.standard.set(selectedVoiceId, forKey: "selectedVoiceId") }
    }
    @Published var availableVoices: [AvailableVoice] = []

    private var versesToRead: [String] = []
    private var onComplete: (() -> Void)?

    override init() {
        super.init()
        synthesizer.delegate = self

        loadAvailableVoices()

        if let saved = UserDefaults.standard.string(forKey: "selectedVoiceId"),
           availableVoices.contains(where: { $0.id == saved }) {
            selectedVoiceId = saved
        } else if let first = availableVoices.first {
            selectedVoiceId = first.id
        }

        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: .duckOthers)
        } catch {
            print("Audio session error: \(error)")
        }
    }

    // MARK: - Voice list

    // Prefer Samantha (the classic universal iOS voice) as default.
    // Then any downloaded Premium/Enhanced English voices.
    // Then a couple built-in Enhanced voices as fallback.
    func loadAvailableVoices() {
        let all = AVSpeechSynthesisVoice.speechVoices()

        var chosen: [AvailableVoice] = []
        var seen = Set<String>()

        // Samantha as the default anchor — universally present on iOS.
        if let samantha = all.first(where: { $0.identifier == "com.apple.voice.compact.en-US.Samantha" || $0.name == "Samantha" }) {
            chosen.append(AvailableVoice(id: samantha.identifier, name: samantha.name, quality: quality(samantha.quality)))
            seen.insert(samantha.name)
        }

        // Every downloaded Premium/Enhanced English voice.
        let premiums = all
            .filter { $0.language.hasPrefix("en") && ($0.quality == .premium || $0.quality == .enhanced) }
            .sorted { qualityRank($0.quality) > qualityRank($1.quality) }
        for v in premiums where !seen.contains(v.name) {
            chosen.append(AvailableVoice(id: v.identifier, name: v.name, quality: quality(v.quality)))
            seen.insert(v.name)
        }

        // Ensure we have at least one voice even on a stripped device.
        if chosen.isEmpty, let fallback = all.first(where: { $0.language.hasPrefix("en") }) {
            chosen.append(AvailableVoice(id: fallback.identifier, name: fallback.name, quality: quality(fallback.quality)))
        }

        availableVoices = chosen
    }

    private func quality(_ q: AVSpeechSynthesisVoiceQuality) -> String {
        switch q {
        case .premium: return "Premium"
        case .enhanced: return "Enhanced"
        default: return ""
        }
    }

    private func qualityRank(_ q: AVSpeechSynthesisVoiceQuality) -> Int {
        switch q {
        case .premium: return 3
        case .enhanced: return 2
        default: return 1
        }
    }

    private func voiceForCurrent() -> AVSpeechSynthesisVoice? {
        if !selectedVoiceId.isEmpty, let v = AVSpeechSynthesisVoice(identifier: selectedVoiceId) { return v }
        return AVSpeechSynthesisVoice(language: "en-US")
    }

    // MARK: - Public

    func speak(_ text: String) {
        stop()
        versesToRead = [text]
        currentVerseIndex = 0
        speakCurrentVerse()
    }

    func speakVerses(_ verses: [String], startingAt index: Int = 0, onComplete: (() -> Void)? = nil) {
        stop()
        versesToRead = verses
        currentVerseIndex = index
        self.onComplete = onComplete
        speakCurrentVerse()
    }

    func stop() {
        synthesizer.stopSpeaking(at: .immediate)
        isSpeaking = false
        isLoadingVoice = false
        versesToRead = []
        currentVerseIndex = 0
        onComplete = nil
    }

    func pause() {
        synthesizer.pauseSpeaking(at: .word)
        isSpeaking = false
    }

    func resume() {
        if synthesizer.isPaused {
            synthesizer.continueSpeaking()
            isSpeaking = true
        }
    }

    func togglePlayPause() {
        if synthesizer.isPaused { resume() }
        else if isSpeaking { pause() }
    }

    var isPaused: Bool { synthesizer.isPaused }

    // MARK: - Private

    private func speakCurrentVerse() {
        guard currentVerseIndex < versesToRead.count else {
            isSpeaking = false
            onComplete?()
            onComplete = nil
            return
        }

        let text = versesToRead[currentVerseIndex]
        let utterance = AVSpeechUtterance(string: text)
        utterance.voice = voiceForCurrent()
        utterance.rate = AVSpeechUtteranceDefaultSpeechRate * 0.9
        utterance.pitchMultiplier = 1.0
        utterance.volume = 1.0
        utterance.preUtteranceDelay = 0
        utterance.postUtteranceDelay = 0

        do {
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Audio session activate error: \(error)")
        }

        isSpeaking = true
        synthesizer.speak(utterance)
    }

    // MARK: - Delegate

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didFinish utterance: AVSpeechUtterance) {
        currentVerseIndex += 1
        if currentVerseIndex < versesToRead.count {
            speakCurrentVerse()
        } else {
            DispatchQueue.main.async {
                self.isSpeaking = false
                self.onComplete?()
                self.onComplete = nil
            }
        }
    }

    func speechSynthesizer(_ synthesizer: AVSpeechSynthesizer, didCancel utterance: AVSpeechUtterance) {
        DispatchQueue.main.async { self.isSpeaking = false }
    }
}
