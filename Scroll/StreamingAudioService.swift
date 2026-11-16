import AVFoundation
import Combine
import Foundation

/// Streams chapter audio via `AVPlayer` for HLS/MP3 URLs from Bible Brain.
///
/// Dormant until `BibleBrainConfig.isReady`. `ChapterView` should try this
/// first when it's ready, and fall back to `SpeechService` otherwise.
final class StreamingAudioService: NSObject, ObservableObject {
    static let shared = StreamingAudioService()

    @Published var isPlaying = false
    @Published var isLoading = false

    private var player: AVPlayer?
    private var endObserver: NSObjectProtocol?
    private var onCompletion: (() -> Void)?

    override init() {
        super.init()
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default, options: .duckOthers)
        } catch {
            print("StreamingAudioService audio session error: \(error)")
        }
    }

    /// Play the audio at `url`. When it finishes, `onFinish` fires on the main queue
    /// (unless the caller cancels via `stop()`).
    func play(url: URL, onFinish: @escaping () -> Void) {
        stop()

        do {
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("StreamingAudioService activate error: \(error)")
        }

        let item = AVPlayerItem(url: url)
        let player = AVPlayer(playerItem: item)
        self.player = player
        self.onCompletion = onFinish
        isLoading = true

        endObserver = NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: item,
            queue: .main
        ) { [weak self] _ in
            guard let self else { return }
            self.isPlaying = false
            self.isLoading = false
            self.onCompletion?()
            self.onCompletion = nil
        }

        player.play()
        isPlaying = true
        isLoading = false
    }

    func pause() {
        player?.pause()
        isPlaying = false
    }

    func resume() {
        player?.play()
        isPlaying = true
    }

    func stop() {
        if let obs = endObserver {
            NotificationCenter.default.removeObserver(obs)
            endObserver = nil
        }
        player?.pause()
        player = nil
        onCompletion = nil
        isPlaying = false
        isLoading = false
    }
}
