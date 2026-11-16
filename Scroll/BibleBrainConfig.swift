import Foundation

/// Feature flag and configuration for Bible Brain (Faith Comes By Hearing) audio streaming.
///
/// When `isEnabled` is `true` AND `apiKey` is set to a real value, `ChapterView`
/// will stream professionally-narrated chapter audio via the Bible Brain API
/// instead of using on-device Apple TTS. When either is missing, the app falls
/// back to Apple TTS transparently.
///
/// The API key must be requested manually at https://4.dbt.io/api_key/request —
/// approval takes a few business days.
enum BibleBrainConfig {
    /// Master switch. Flip to `true` after `apiKey` is populated to activate
    /// streaming narration for all users.
    static let isEnabled: Bool = false

    /// The Bible Brain API key. Empty until issued. When populated, add the
    /// literal string here — it is a per-app key, not a secret in the security
    /// sense (all API requests are attributed to it).
    static let apiKey: String = ""

    /// Fixed API base — v4 endpoints, JSON.
    static let baseURL = URL(string: "https://4.dbt.io/api")!

    /// Version parameter required on every call.
    static let apiVersion = "4"

    /// Runtime check consumers should use — false while dormant.
    static var isReady: Bool { isEnabled && !apiKey.isEmpty }
}
