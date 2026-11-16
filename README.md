# Scroll

Bible verses on the Fruits of the Spirit, and a quiet place to read any chapter.
Free, no account, no ads, no tracking.

| | Where | Source |
|---|---|---|
| iPhone and iPad | [App Store](https://apps.apple.com/app/id6497410297) | `Read.xcodeproj`, `Scroll/` (SwiftUI) |
| Web | [scroll.okeke.us](https://scroll.okeke.us) | `web/` (plain HTML, CSS and JavaScript, no build step) |
| Android | Google Play (in progress) | `android/` (Kotlin, Jetpack Compose) |

All three read Bible text from the public [bolls.life](https://bolls.life) API and offer
the ESV (default), KJV, NASB and NLT. Saved verses and chapters stay on the device.

## Shared rules

These behave the same on every platform. Change them everywhere or nowhere.

- **Addresses.** `https://scroll.okeke.us/<TRANSLATION>/<book>/<chapter>/` opens a chapter and
  `…/<chapter>/<verse>` (or `<verse>-<verse>`) opens it with those verses highlighted. Books are
  numbered 1 (Genesis) to 66 (Revelation).
- **Share text.** Verse text, a blank line, `John 3:16 (ESV)`, then the verse's address.
- **Verse text cleaning.** Drop KJV Strong's numbers (`<S>…</S>`) and translator notes
  (`<sup>…</sup>`) with their contents, turn `<br>` into a space, strip any other tag but keep
  its text, collapse whitespace. KJV searches also return Apocrypha; keep books 1 to 66 only.

## Web

```
node scripts/serve-web.mjs          # preview at http://localhost:4173
node --test tests/web/format.test.mjs
```

Pushing changes under `web/` to `main` runs the tests and deploys to Azure Static Web Apps
(`swa-scroll`) through `.github/workflows/deploy-web.yml`.

## iPhone and iPad

Open `Read.xcodeproj` in Xcode (scheme `Read`, bundle id `okekedev.Read`). Release notes and
store text for past versions are in `asc_v2.0_metadata.md` and `asc_v2.1_release.md`.

## Android

See `android/README.md`.
