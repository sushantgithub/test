# CPMAI Study (Android)

Offline Android app for studying CPMAI algorithm notes in plain language.

## Features

- Browse 19 topics (classification, clustering, learning types, special techniques, metrics)
- Search and category filters
- Layman analogies, exam traps, and sample questions
- 48-question exam drill with self-marking
- Exam-day cheat sheet
- Progress tracking (studied topics + quiz stats)

## Install the APK

1. Download `releases/CPMAI_Study.apk` from this repo (or the file attached to the agent run).
2. On your phone: **Settings → Security → allow install from this source**.
3. Open the APK and install. The app does not need internet.

The APK is a **debug-signed release** build (`com.cpmai.study`) so you can sideload it without Play Store.

## Build from source

Requires JDK 17+ and Android SDK (compileSdk 35).

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:assembleRelease
# APK: app/build/outputs/apk/release/app-release.apk
```
