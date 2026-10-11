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


## Learning roadmap (v6)
- Start Learning opens the Learning Path screen instead of the vocabulary tab.
- Choose Vietnamese–Chinese or English–Chinese, a level, study frequency, and any of 10 topics.
- Three levels contain 20 lessons each; review tests are available after each block of five lessons.
- Placement check suggests a level, vocabulary can be marked learned and annotated with personal notes, and progress is saved locally.
- Android APK includes a daily reminder scheduler; allow notifications on Android 13+. Android reminders use an approximate daily alarm to respect power-saving restrictions.

After pushing to GitHub, run **Actions → Build Android debug APK**. The APK is an artifact named `chinese-starter-debug-apk`.


## v8 navigation update
- Opening a lesson now replaces the lesson list with a dedicated full-screen lesson view.
- Review tests also open in a dedicated view.
- Back-to-list and close controls restore the lesson list without jumping through a long page.
- Android bundled WebView assets are synchronized with the web source.


## HSK vocabulary update (v11)

Vocabulary practice and lesson content now use HSK-aligned packs mapped to the selected level: Beginner → HSK 1, Intermediate → HSK 2–3, Advanced → HSK 4. This package includes a curated starter pack of 30 entries per HSK level (120 source entries before duplicate consolidation), with Vietnamese meanings and example prompts. It is an educational starter subset, not a complete official HSK syllabus export. Vocabulary pack was curated from commonly taught HSK vocabulary; pinyin and meanings should be reviewed by a Mandarin instructor before exam-critical use.
