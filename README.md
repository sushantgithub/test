# Ring Companion (Galaxy Ring APK)

Android companion for **Samsung Galaxy Ring**. Sideload the APK on your phone. Pairing and gestures stay in **Galaxy Wearable**; health data is read from **Samsung Health** through **Health Connect**.

## Install the APK

1. Copy `dist/RingCompanion.apk` to your Samsung phone (Drive, USB, or download from this repo).
2. Open the file → **Install**. Allow **Install unknown apps** for your Files/Chrome app if Android asks.
3. Open **Ring Companion**.

## Show live ring data

1. Pair the ring in **Galaxy Wearable** (this app cannot pair hardware).
2. Confirm data appears in **Samsung Health**.
3. Install **Health Connect** (Play Store) if it is missing.
4. Samsung Health → Settings → Health Connect → allow sharing of sleep, heart rate, steps, calories, SpO₂.
5. In Ring Companion tap **Connect Health Connect** and allow the same categories.

Until that is granted, the app shows **sample** numbers so you can still learn the screens.

## What this APK can and cannot do

| Can | Cannot |
| --- | --- |
| Dashboard for sleep, HR, steps, SpO₂, calories | Pair the ring or update firmware |
| Open Galaxy Wearable / Samsung Health | Find My Ring hardware ping (use Wearable) |
| Explain pinch gestures | Change gesture mappings (use Wearable) |
| Work without a git repo on your phone | Replace Samsung Health |

Samsung does not publish a public API that lets a third-party APK talk to the ring over Bluetooth. Official third-party access is **Samsung Health Data SDK** (partner registration for store release) or **Health Connect**.

## Build from source

```bash
export ANDROID_HOME=/path/to/android-sdk
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```
