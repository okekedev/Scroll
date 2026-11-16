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

## Name, subtitle and keywords (needs the owner's OK before it is pushed)

The live listing is "Scroll: Bible Narrator" / "Bible verses read aloud" with `voice` and
`narrator` in the keywords, but the app is read-only. Where Scroll ranked in US App Store
search on 2026-11-16 (Apple's public search API, top 200 results):

| Search term | Scroll's position | Who is on top |
|---|---|---|
| fruits of the spirit | 7 | apps with 0 to 2 ratings (plus Fruit Ninja) |
| bible narrator | 13 | audio Bibles with 130K+ ratings |
| scroll bible | 24 | Scroll The Bible (20K ratings) |
| fruit of the spirit | 64 | apps with 0 to 2 ratings |
| bible verses, verse of the day, daily bible verse, scripture, esv/kjv/nlt/nasb bible, bible study, bible quotes, bible reader | not in top 200 | apps with 10K to 13M ratings |
| bible verses by topic, share bible verse, memory verse, topical bible, christian virtues, galatians | not in top 200 | top results have 0 to a few hundred ratings |

Reading: the big generic terms are out of reach for an app with 10 ratings. The winnable ones are
the Fruit of the Spirit terms (Scroll is already page one for the plural with no help from the
name) and a handful of small topic terms.

| Field | Live | Proposed |
|---|---|---|
| Name (30 max) | `Scroll: Bible Narrator` | `Scroll: Fruit of the Spirit` (27) |
| Subtitle (30 max) | `Bible verses read aloud` | `Bible verses by topic to share` (30) |

Keywords (98/100; no words repeated from the name or subtitle, since Apple combines all three):

```
scripture,esv,kjv,nlt,nasb,topical,virtues,love,joy,peace,patience,kindness,faith,galatians,quotes
```

Not yet known: search volume and difficulty for these terms. Astro (the keyword tool) is only
tracking Today's Desk; add Scroll there with the terms above to get those numbers before locking this in.

Newer App Store options worth using after 2.2 ships (announced at WWDC25; not checked against the
API docs):
- **Custom product pages with keywords.** A product page variant can be tied to keywords so it
  shows for those searches, e.g. a "verses about love" page with love screenshots.
- **App Store Tags.** Apple generates feature tags for each app; they can be reviewed and trimmed
  in App Store Connect.

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
