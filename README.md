# Chinese Starter — Beginner Chinese Learning App

A responsive offline-first web app / PWA for beginner Mandarin (Simplified Chinese), with an Android Studio WebView wrapper and GitHub Actions workflow to build a debug APK.

## Features
- Vocabulary cards with Hanzi, pinyin, English meaning, examples, and audio pronunciation (device/browser Chinese speech voice when available).
- Speaking practice: listen and repeat, optional microphone speech recognition when supported by the browser.
- Writing practice: trace/copy Hanzi, type pinyin/English meaning, and self-check.
- Conversation role-play for daily life, workplace tasks, and customer meetings.
- Progress saved locally on the device, daily streak and completion counts.
- Light/dark themes and accent color selection.
- PWA manifest and service worker for offline use after first load.

## Run as a web app
Open `web/index.html` in a browser, or serve the `web` folder from any static host (GitHub Pages, Netlify, etc.). For PWA install/offline caching, use HTTPS or localhost.

## Build Android APK with GitHub Actions
1. Push the ZIP contents to a GitHub repository (the `.github/` and `android/` folders must be at repository root).
2. Open **Actions** and run **Build Android debug APK** (or push to `main`).
3. Download `app-debug.apk` from the workflow artifact named `chinese-starter-debug-apk`.

The Android project uses a local WebView asset bundle, so the main lessons work offline. Microphone speech recognition depends on Android WebView/device support and permissions; pronunciation audio depends on an installed Chinese speech voice.

## Local Android build
Open `android/` in Android Studio and sync Gradle, then build the `app` debug variant.

## Content scope
This is a starter curriculum rather than a complete HSK course. Vocabulary and dialogues can be extended in `web/app.js`.
