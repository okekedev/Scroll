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
node --test tests/web/*.test.mjs
node scripts/build-pages.mjs        # rewrite the static pages (see below)
```

Pushing changes under `web/` to `main` runs the tests and deploys to Azure Static Web Apps
(`swa-scroll`) through `.github/workflows/deploy-web.yml`.

**Static pages for search engines.** The site is a single-page app, but the home page, the nine
Fruit of the Spirit pages, About and Privacy are also real HTML files with their own title,
description, canonical address and finished content, so search engines and link previews can
read them without JavaScript. `scripts/build-pages.mjs` writes them, along with `sitemap.xml`,
`robots.txt`, the fallback page (`shell.html`, served for chapters and every other address) and
the IndexNow key file. The words come from `web/js/pages.js` and the titles from
`web/js/meta.js`, the same code the app runs in the browser. After changing either file, run
the script and commit what it writes; the tests fail if the pages on disk are out of date.

Search engines: Google reads the site through the `okeke.us` domain property in Search Console.
Bing is verified by the `msvalidate.01` tag in every page, and each deploy pings IndexNow with
the addresses in the sitemap. Chapters are not in the sitemap; they are found by links. A verse
address such as `/ESV/43/3/16` is canonical to its chapter. Search and Saved are `noindex`.
The site has no analytics, and that is on purpose.

## iPhone and iPad

Open `Read.xcodeproj` in Xcode (scheme `Read`, bundle id `okekedev.Read`). Release notes and
store text for past versions are in `asc_v2.0_metadata.md` and `asc_v2.1_release.md`.

## Android

See `android/README.md`.
