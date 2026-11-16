# Scroll v2.2 — Release Sheet

Status: built and tested in the simulator. **Not archived, not uploaded, not submitted.**
Submit after scroll.okeke.us is live on its own domain (shared verses link there).

## Version

| | |
|---|---|
| Marketing version | **2.2** |
| Build number | **7** |
| Bundle ID | `okekedev.Read` |
| App Store ID | `6497410297` |

## What changed

- NIV removed. ESV is the default; KJV, NASB and NLT remain.
- Verses and chapters people saved in NIV convert to the ESV on first launch.
- Share button on every verse; long-press a verse in a chapter to share or copy.
  Shared text ends with a link to the verse on scroll.okeke.us.
- Opening a chapter from a verse scrolls to that verse.
- King James text no longer shows Strong's reference numbers or translator notes.
- Original app icon restored.
- Settings: "Read on the web" and "Privacy Policy" links.

## What's New (v2.2)

```
New in Scroll 2.2:
• Share any verse. Tap the share button on a verse, or press and hold a verse inside a chapter.
• Shared verses include a link that opens the verse on the new Scroll website, scroll.okeke.us
• ESV is now the default translation. NIV has been retired; anything you saved in NIV moves to the ESV.
• Opening a chapter from a verse now takes you straight to that verse
• Cleaner King James text
• The original Scroll icon is back
```

## Description (NIV removed, sharing and website added)

```
Discover Bible verses about the Fruits of the Spirit.

Nine virtues, one home screen:
Love · Joy · Peace · Patience · Kindness · Goodness · Faith · Gentleness · Self-Control

For each virtue, browse a rotating list of scripture in the ESV, KJV, NASB or NLT. Save what moves you and share it with a friend. Tap any verse to jump straight into the full chapter, then walk chapter by chapter with the prev / next arrows.

SHARE A VERSE
Send any verse from the share button, or press and hold a verse inside a chapter. Each shared verse includes a link that opens it on scroll.okeke.us, so anyone can read it in context.

READ AND SAVE
• Tap any verse to open the full chapter
• Save single verses or whole chapters
• Prev / next chapter navigation across the entire Bible

PRIVATE AND FREE
• No account required
• No tracking, no ads, no in-app purchases
• Everything you save stays on your device
```

## Name, subtitle and keywords (the app is read-only; no voice reader)

The live listing is named "Scroll: Bible Narrator" with the subtitle "Bible verses read aloud",
and its keywords include `voice` and `narrator`. The app has no audio. Proposed for 2.2
(needs the owner's OK before it is pushed):

| Field | Live | Proposed |
|---|---|---|
| Name (30 max) | `Scroll: Bible Narrator` | `Scroll: Bible Verses` |
| Subtitle (30 max) | `Bible verses read aloud` | `Read, save and share verses` |

Keywords (100 max):

```
bible,scripture,verse,daily,esv,kjv,christian,gospel,jesus,pray,faith,fruits,love,peace,psalm,share
```

## Promotional Text

```
Discover Bible verses by virtue: Love, Joy, Peace, Faith. Save chapters, share a verse with a link, and read on the web at scroll.okeke.us. Free.
```

## URLs

- Marketing URL: `https://scroll.okeke.us`
- Privacy Policy URL: `https://scroll.okeke.us/privacy`

## Screenshots

`scroll_v2.2_screenshots/iphone_69/` (1320×2868) and `scroll_v2.2_screenshots/ipad_13/` (2064×2752):
home, verse list with the share button, John 3 open on verse 16, saved verses.
The live listing currently has one iPhone screenshot.

## Reviewer notes

```
v2.2 adds verse sharing (system share sheet, text only) and makes the ESV the default
translation. NIV has been removed; items saved in NIV are converted to the ESV.
No account, no purchases, no tracking. Bible text is loaded from the public bolls.life API.
```

## Ship checklist

- [x] MARKETING_VERSION 2.2, CURRENT_PROJECT_VERSION 7
- [x] Builds clean; tested in the simulator (NIV data migration, share text, chapter scroll)
- [x] New screenshots captured
- [ ] scroll.okeke.us live on the custom domain (needs the DNS record)
- [ ] Share sheet and long-press menu checked on a real iPhone
- [ ] Archive, export, upload build 7
- [ ] Push metadata and screenshots
- [ ] Submit for review
