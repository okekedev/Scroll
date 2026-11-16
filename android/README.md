# Scroll for Android

Native Android version of the Scroll iPhone app (`../Scroll`). Kotlin, Jetpack Compose, Material 3, one activity.
Package `com.christianokeke.scroll`, minSdk 26, compile and target SDK 37.

The only network calls are reads from `https://bolls.life`. Saved verses, saved chapters and the last translation
picked live in one SharedPreferences file on the device. The only permission is `INTERNET`.

## Build

Needs JDK 17 and the Android SDK (platform `android-37.0`, build-tools `36.0.0`). Point `local.properties`
(git-ignored) at the SDK:

```
sdk.dir=/opt/homebrew/share/android-commandlinetools
```

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
./gradlew :app:assembleDebug        # debug APK, no keystore needed
./gradlew :app:bundleRelease        # signed AAB for Google Play
./gradlew :app:assembleRelease      # signed, minified APK for a device or emulator
```

Outputs:

- `app/build/outputs/bundle/release/app-release.aab` (upload this to Play)
- `app/build/outputs/apk/release/app-release.apk`

## Test

```sh
./gradlew :app:testDebugUnitTest    # text cleaner, chapter navigation, share text, API parsing
./gradlew :app:lintRelease          # report in app/build/reports/
```

## Signing

Release builds are signed with the Play **upload key**:

- `keystore/upload-keystore.jks` (alias `upload`)
- `keystore/keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`)

Both are git-ignored and exist only on this machine. **Back them up somewhere off this machine** (a password
manager works for both the file and the password). If they are lost, Play uploads stop until Google resets the
upload key through Play Console support.

Without `keystore.properties`, debug builds, tests and lint still work, and packaging a release fails with
"Release signing is not set up". It never falls back to the debug key.

Print the upload certificate fingerprint:

```sh
keytool -printcert -jarfile app/build/outputs/bundle/release/app-release.aab
```

## Releasing an update

In `app/build.gradle.kts`:

- `versionCode`: add 1 for every upload to Play. Play rejects a code it has already seen.
- `versionName`: the version people see (also shown on the Settings screen), e.g. `"1.1"`.

Then `./gradlew :app:bundleRelease` and upload the AAB.

## Play listing assets

- `play-assets/icon-512.png`: 512x512 listing icon
- `play-assets/screenshots/`: 1080x1920 phone screenshots

Launcher icons are generated from the iOS icon photo with `python3 tools/make_icons.py` (needs Pillow).
