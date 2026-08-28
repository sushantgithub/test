# CPMAI Study (Android)

Offline Android app for studying CPMAI algorithm notes in plain language.

## Features

- Browse 19 topics (classification, clustering, learning types, special techniques, metrics)
- Search and category filters
- Layman analogies, exam traps, and sample questions
- 48-question exam drill with self-marking
- Exam-day cheat sheet
- Progress tracking (studied topics + quiz stats)

## Google Play

Play requires a signed **.aab**, not a debug APK. See **[play-listing/PLAY_STORE.md](play-listing/PLAY_STORE.md)** for listing copy, Data safety answers, and upload steps.

Package name: `com.sushantgithub.cpmaistudy`  
Bundle: `releases/CPMAI_Study.aab`

## Sideload APK (not for Play)

`releases/CPMAI_Study.apk` is for installing without Play. Play Console must use the AAB.

## Build

Requires JDK 17+, Android SDK 36, and `keystore.properties` (see `keystore.properties.example`).

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:bundleRelease :app:assembleRelease
```
