# Scroll on Google Play

Package `com.christianokeke.scroll`. Play account: the one that owns Okeke Control.
Build and signing steps are in `README.md`; the general Play playbook is the
`google-play-release` skill. This file is the listing content and the console checklist.

## Store listing (Grow users → Store listing)

**App name** (30 max)

```
Scroll: Bible Verses
```

**Short description** (80 max)

```
Bible verses on the Fruits of the Spirit. Read, save and share. Free, no ads.
```

**Full description**

```
Discover Bible verses about the Fruits of the Spirit.

Nine virtues, one home screen:
Love · Joy · Peace · Patience · Kindness · Goodness · Faith · Gentleness · Self-Control

For each virtue, browse a rotating list of scripture in the ESV, KJV, NASB or NLT. Save what moves you and share it with a friend. Tap any verse to jump straight into the full chapter, then walk chapter by chapter from Genesis 1 to Revelation 22.

SHARE A VERSE
Send any verse from the share button, or press and hold a verse inside a chapter. Each shared verse includes a link that opens it on scroll.okeke.us, so anyone can read it in context.

READ AND SAVE
• Tap any verse to open the full chapter
• Save single verses or whole chapters
• Previous and next chapter navigation across the entire Bible
• Light and dark themes

PRIVATE AND FREE
• No account required
• No tracking, no ads, no in-app purchases
• Everything you save stays on your device

Also on the web at scroll.okeke.us and on the App Store for iPhone and iPad.
```

**Graphics** (all in `play-assets/`)

| Asset | File |
|---|---|
| App icon 512×512 | `icon-512.png` |
| Feature graphic 1024×500 | `feature-graphic.png` |
| Phone screenshots (16:9, at least 2) | `screenshots/` |

**Category** Books & Reference. **Tags**: Bible, Christianity. **Contact email** christian@okeke.us. **Website** https://scroll.okeke.us

## App content (Policy → App content)

| Item | Answer |
|---|---|
| Privacy policy | https://scroll.okeke.us/privacy |
| App access | All functionality is available without special access |
| Ads | No, the app does not contain ads |
| Content rating | Reference / educational. No violence, no user-generated content, no sharing of location. Expect "Everyone" |
| Target audience | 13 and over (keeps the app out of the Families program) |
| Data safety | No data collected, no data shared. The app makes network requests only to load Bible text and sends no identifiers. Saved verses stay on the device |
| Government app / financial features / health | No |
| News app | No |

## Release notes (1.0)

```
The first Android release of Scroll.
• Verses on the nine Fruits of the Spirit in the ESV, KJV, NASB and NLT
• Open any verse in its full chapter and read on, chapter by chapter
• Save verses and whole chapters
• Share a verse with a link
```

## Console checklist (a person has to do these; the AAB cannot be uploaded by automation)

1. **Create app** → name `Scroll: Bible Verses`, default language English (US), App, Free.
2. **Test and release → Internal testing → Create new release** → upload
   `app/build/outputs/bundle/release/app-release.aab` by hand. This first upload enrolls the
   upload key in `keystore/` with Play App Signing. Add yourself as a tester and install from the
   opt-in link to check it on a real phone.
3. Fill in **Store listing** and **App content** from the sections above.
4. **Countries/regions** → select all → Save (otherwise the release errors with "no countries").
5. **Production → Create new release** → *Add from library* (promote the tested bundle), paste
   the release notes, then **Publishing overview → Send for review**. First reviews take about a week.
6. New personal Play accounts must first run a closed test with 12 testers for 14 days before
   production. The account that shipped Okeke Control has already been through production, so this
   should not apply; if the console asks for it, that is the reason.
7. After it is live: add the Play link to the website (`web/js/app.js`, home and About) and to the
   repo README.

## After the first upload

Back up `keystore/upload-keystore.jks` and `keystore/keystore.properties` somewhere off this Mac.
Losing them blocks every future update until Google resets the key.
