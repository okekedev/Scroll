import SwiftUI

struct VoicePicker: View {
    @ObservedObject var speechService: SpeechService
    var compact: Bool = false
    var onSelect: ((AvailableVoice) -> Void)? = nil

    var body: some View {
        VStack(spacing: 8) {
            ForEach(speechService.availableVoices) { voice in
                VoicePickerRow(
                    voice: voice,
                    isSelected: speechService.selectedVoiceId == voice.id,
                    compact: compact
                ) {
                    speechService.selectedVoiceId = voice.id
                    onSelect?(voice)
                }
            }
        }
    }
}

private struct VoicePickerRow: View {
    let voice: AvailableVoice
    let isSelected: Bool
    let compact: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(voice.name)
                        .font(.system(size: compact ? 15 : 16, weight: isSelected ? .semibold : .regular))
                        .foregroundColor(.primary)

                    if !voice.quality.isEmpty {
                        Text(voice.quality)
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(Theme.Colors.accent)
                    }
                }

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .font(.system(size: compact ? 20 : 22))
                        .foregroundColor(Theme.Colors.accent)
                } else {
                    Circle()
                        .stroke(Color.secondary.opacity(0.3), lineWidth: compact ? 1.5 : 2)
                        .frame(width: compact ? 20 : 22, height: compact ? 20 : 22)
                }
            }
            .padding(12)
            .liquidGlassCard(cornerRadius: 10, isHighlighted: isSelected)
        }
        .buttonStyle(.plain)
    }
}
