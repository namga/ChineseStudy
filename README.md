# Chinese Starter v2 — Beginner Mandarin Learning App

A friendly, responsive Mandarin starter app for Web/PWA and Android. Includes a cute panda reading a Chinese book icon, an expanded searchable vocabulary set, speaking/listening practice, writing checks, and practical conversation role-play.

## What's new in v2
- Cute panda-with-book app icon for the web app and Android launcher.
- Expanded beginner vocabulary across greetings, daily life, food, shopping, directions, office tasks, meetings, and customer service.
- Search vocabulary by Chinese characters, pinyin, English meaning, or example text.
- Added role-play scenarios: phone calls, asking a colleague for help, project updates, customer requirements, price negotiation, lunch plans, and complaint follow-up.
- Easier mobile controls, more comfortable tap targets, clearer scenario guidance, responsive search/filter controls.
- Updated offline cache version to refresh new assets.

## Run on the web
Open `web/index.html`, or host the `web/` folder on GitHub Pages or Netlify. PWA install/offline caching requires HTTPS or localhost.

## Build an Android debug APK
Push the ZIP contents to GitHub with `.github/`, `android/`, and `web/` at the repository root. In **Actions**, run **Build Android debug APK**. Download artifact `chinese-starter-debug-apk`.

The Android WebView loads bundled local assets so core lessons work offline. Pronunciation uses the device/browser Chinese speech voice. Microphone speech recognition varies by Android WebView and device support; test permissions on the target device.

## Progress
Vocabulary learned, completed dialogues, ratings, and daily practice activity are stored locally on the device. This is a starter curriculum and is not a complete HSK course.

## v3 audio and microphone fix
The Android app uses native Android TextToSpeech for Mandarin playback and Android SpeechRecognizer (`zh-CN`) for speaking practice. The microphone permission is requested when the user taps the microphone. If audio does not play, install/enable a Mandarin voice in Android Text-to-speech settings. If recording cannot start, enable a speech recognition service (such as Google Speech Services) and allow microphone permission. Browser-only use still depends on browser support and HTTPS for speech recognition.

## v4 audio reliability update
- Added a visible **Stop audio** control.
- Improved native Android TTS initialization, playback error reporting, speech rate, and audio attributes.
- Added a native bridge diagnostic method and clearer messages for missing Mandarin voice data.
- Improved microphone error messages and recognition lifecycle cleanup.
- Please build a fresh APK after replacing the repository files. Existing APKs do not receive source changes automatically.
- Device requirements: an installed/enabled Mandarin TTS voice for audio playback, and an installed/enabled speech recognition service plus microphone permission for recognition. These services can be missing on some Android builds.
