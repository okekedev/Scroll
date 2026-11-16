# Scroll v2.1 — Release Sheet

Everything shipping to App Review, consolidated. Single source of truth.

## Version

| | |
|---|---|
| Marketing version | **2.1** |
| Build number | **6** (uploaded as 5 previously, bumped after icon + strip) |
| Bundle ID | `okekedev.Read` |
| Team | `TUG3BHLSM4` |
| App Store ID | `6497410297` |

## Icon

**Concept:** Horizontal parchment scroll on deep navy background. Cream body with six scripture-suggesting text lines. Both ends rolled. No letter, no soundwave — clean and iconic.

- **Source (SVG):** `/tmp/scroll_icon_v2.svg`
- **Rendered (PNG 1024×1024):** `/tmp/scroll_icon_v2.png`
- **Installed at:** `Read/Scroll/Assets.xcassets/AppIcon.appiconset/scroll_icon.png`

Previous "S" letter icon (v2.0 era) is superseded.

## Rebrand

| Field | Old | New |
|---|---|---|
| **App Store name** | `.Scroll` | `Scroll: Bible Narrator` |
| **Home screen name** | `.Scroll` | `Scroll` |

## App Store copy

### App Store Name — 21/30

```
Scroll: Bible Narrator
```

### Subtitle — 23/30

```
Bible verses read aloud
```

### Keywords — 100/100

```
bible,scripture,verse,daily,voice,narrator,christian,gospel,jesus,pray,faith,fruits,love,peace,psalm
```

### Promotional Text — 156/170

```
Discover Bible verses by virtue — Love, Joy, Peace, Faith. Save chapters, walk book by book, listen with the voice reader. Free.
```

### What's New (v2.1)

```
New in Scroll 2.1:
• Tap any verse to read the full chapter
• Save whole chapters, not just verses
• Prev / next chapter arrows walk Genesis 1 → Revelation 22
• Refresh button (no more accidental pull-refreshes)
• Cleaner Settings with a guide to install Apple Premium voices
• Under-the-hood polish and stability
```

### Description

```
Discover Bible verses about the Fruits of the Spirit — read to you by a natural voice.

Nine virtues, one home screen:
Love · Joy · Peace · Patience · Kindness · Goodness · Faith · Gentleness · Self-Control

For each virtue, browse a rotating list of scripture across NIV, KJV, ESV, NASB, and NLT. Save what moves you. Tap any verse to jump straight into the full chapter — then walk chapter by chapter with the new prev / next arrows.

NEW IN 2.1
• Tap any verse to open the full chapter
• Save whole chapters (not just verses)
• Prev / next chapter navigation across the entire Bible
• Refresh button replaces accidental pull-refresh
• Guide to install Apple Premium voices (Ava, Zoe, Serena, Malcolm) for warmer narration

VOICE READER
Scroll ships with the natural iOS voice on your device and helps you install Apple's free Premium voices for even warmer narration — no subscriptions, no accounts.

PRIVATE AND FREE
• No account required
• No tracking, no ads, no in-app purchases
• Everything happens on your device
```

**⚠️ NOTE:** The description still mentions voice reader / Apple Premium voices. Since we just stripped voice UI from the app entirely (no play button, no Settings voice section), this copy is now inaccurate. **NEEDS REWRITE** — proposed below.

### Description (rewritten to match stripped-UI app)

```
Discover Bible verses about the Fruits of the Spirit.

Nine virtues, one home screen:
Love · Joy · Peace · Patience · Kindness · Goodness · Faith · Gentleness · Self-Control

For each virtue, browse a rotating list of scripture across NIV, KJV, ESV, NASB, and NLT. Save what moves you. Tap any verse to jump straight into the full chapter — then walk chapter by chapter with the new prev / next arrows.

NEW IN 2.1
• Tap any verse to open the full chapter
• Save whole chapters (not just verses)
• Prev / next chapter navigation across the entire Bible
• Refresh button replaces accidental pull-refresh

PRIVATE AND FREE
• No account required
• No tracking, no ads, no in-app purchases
• Everything happens on your device
```

## Reviewer notes

```
v2.1 refreshes the app around chapter reading and navigation: tap-to-open-chapter,
save-whole-chapter, prev/next arrows, and refresh button.

The App Store display name changes from '.Scroll' to 'Scroll: Bible Narrator' —
same app, same bundle, same team.

Bible Brain streaming (Faith Comes By Hearing) code is present but dormant behind
a compile-time flag; it will be activated in v2.2 once our API key request is
approved. No voice-reader UI is shipped in 2.1 while we finalize that path.
```

## Reviewer contact

- Name: Christian Okeke
- Phone: +1 832 555 0100 (placeholder — replace with real)
- Email: christian@okeke.us

## Ship checklist

- [x] Bump MARKETING_VERSION to 2.1
- [x] Bump CURRENT_PROJECT_VERSION to 5 (need to bump to 6 for reupload after icon + strip)
- [x] Install new scroll icon
- [x] Strip voice UI from Settings
- [x] Strip play button from ChapterView
- [x] Bible Brain scaffolding added (dormant)
- [x] Build succeeds
- [x] Build 5 uploaded (STALE — need to replace with build 6)
- [ ] Bump build to 6
- [ ] Re-archive
- [ ] Re-upload to ASC
- [ ] Push metadata via `asc_push_scroll_v2_1.py --execute`
- [ ] Submit via `--submit`
