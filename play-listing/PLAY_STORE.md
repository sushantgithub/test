# Publish CPMAI Study on Google Play

Google Play **does not accept a first-time APK** for a new app. You upload a signed **Android App Bundle** (`.aab`). This repo is set up for that.

I cannot log into Play Console or click Publish for you — that requires your Google Play developer account (one-time $25) and identity verification.

## Files to upload

| Play Console field | File |
| --- | --- |
| Production / testing release | `releases/CPMAI_Study.aab` |
| App icon | `play-listing/icon_512.png` |
| Feature graphic | `play-listing/feature_graphic.png` |
| Phone screenshots (min 2) | `play-listing/screenshot_topics.png`, `screenshot_quiz.png`, `screenshot_cheatsheet.png` |
| Privacy policy | Host `docs/privacy.html` on GitHub Pages (or any HTTPS URL) |

Keep **`CPMAI_Play_Upload_Key.zip`** from the agent artifacts (or your own copy of `keystore/upload-keystore.jks`) **forever**. If you lose the upload key, you cannot update the app.

## 1. Developer account

1. Open [Google Play Console](https://play.google.com/console).
2. Pay the one-time registration fee.
3. Complete identity verification (can take hours to a few days).

## 2. Create the app

1. **Create app**.
2. App name: **CPMAI Study**
3. Default language: English (US) or English (India)
4. App or game: **App**
5. Free or paid: **Free**
6. Declarations: agree to the policies.

## 3. Store listing (copy-paste)

**App name:** CPMAI Study

**Short description** (80 characters max):

```
Study CPMAI algorithms in plain language — notes, traps, quiz, cheat sheet.
```

**Full description:**

```
CPMAI Study is an offline companion for Cognitive Project Management for AI exam prep.

Learn the algorithms from the notes in everyday language (desi-style analogies), then drill exam traps.

WHAT’S INSIDE
• 19 topics: KNN, Naive Bayes, SVM, Decision Tree, Random Forest, Linear/Logistic Regression, K-Means, Fuzzy C-Means, supervised vs unsupervised vs RL, deep learning, transfer learning, NLP, computer vision, HMM, GMM, evaluation metrics, and a master cheat sheet
• Search and category filters
• Layman analogies, how-it-works, and CPMAI traps
• Exam-style questions with answers
• 48-question self-marked quiz
• Exam-day keyword → answer cheat sheet
• On-device progress (mark topics studied)

The app works without an internet connection. Progress stays on your phone.

This is an independent study aid. It is not affiliated with PMI, CPM, or the official CPMAI program.
```

**Category:** Education  
**Tags:** exam prep, machine learning, education  
**Contact email:** kulk.sushant@gmail.com  
**Privacy policy URL:** after you host `docs/privacy.html`, paste that HTTPS link.

## 4. Upload the bundle

1. Testing → **Closed testing** (recommended first) or **Production**.
2. Create a new release.
3. Upload `releases/CPMAI_Study.aab`.
4. Let Play App Signing generate the app signing key. Keep using this project’s **upload** keystore for every future build.
5. Release name: `1.0.0`
6. Release notes:

```
First release: CPMAI algorithm notes, quiz, and exam cheat sheet. Fully offline.
```

## 5. App content / Data safety

Fill these so review is not stuck:

- **Privacy policy:** the HTTPS URL above
- **Ads:** No
- **App access:** All features are available without restriction
- **Ads / COVID / News:** No
- **Target audience:** 18 and over (professional exam). Do **not** select Designed for Families.
- **Data safety:** No data collected / shared. Progress is stored only on the device. No encryption-in-transit needed because nothing is sent.
- **Government / Financial / Health:** No

## 6. Submit

Complete every dashboard checklist item (green ticks), then **Send for review**. First review often takes a few days.

If Play rejects the package name `com.sushantgithub.cpmaistudy` as already taken, change `applicationId` in `app/build.gradle.kts` and rebuild **before** the first accepted upload. After the first upload, never change it.

## Rebuild later

```bash
# put keystore.properties next to settings.gradle.kts (never commit it)
./gradlew :app:bundleRelease
# output: app/build/outputs/bundle/release/app-release.aab
```
