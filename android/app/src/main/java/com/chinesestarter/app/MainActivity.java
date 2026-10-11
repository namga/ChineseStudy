package com.chinesestarter.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;
import java.util.Calendar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech.Engine;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private PermissionRequest pendingWebPermission;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private String pendingText = null;
    private SpeechRecognizer recognizer;
    private boolean pendingListening = false;
    private static final int REQUEST_AUDIO = 1001;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        setContentView(webView);

        if (Build.VERSION.SDK_INT >= 21) {
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        }
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        webView.addJavascriptInterface(new NativeSpeechBridge(), "NativeSpeech");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                boolean wantsAudio = false;
                for (String resource : request.getResources()) {
                    if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) wantsAudio = true;
                }
                if (wantsAudio && Build.VERSION.SDK_INT >= 23 &&
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    pendingWebPermission = request;
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
                } else {
                    request.grant(request.getResources());
                }
            }
        });

        initTts();
        webView.loadUrl("file:///android_asset/web/index.html");
    }


    private void scheduleDailyReminder(boolean enabled, int hour, int minute) {
        if (enabled && Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1002);
        }
        AlarmManager alarms = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, DailyReminderReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(this, 6107, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarms == null) return;
        if (!enabled) {
            alarms.cancel(pending);
            getSharedPreferences("learning_reminder", MODE_PRIVATE).edit().putBoolean("enabled", false).apply();
            jsCallback("onReminderScheduled", "off");
            return;
        }
        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, Math.max(0, Math.min(23, hour)));
        next.set(Calendar.MINUTE, Math.max(0, Math.min(59, minute)));
        next.set(Calendar.SECOND, 0); next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) next.add(Calendar.DAY_OF_YEAR, 1);
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pending);
        getSharedPreferences("learning_reminder", MODE_PRIVATE).edit().putBoolean("enabled", true).putInt("hour", hour).putInt("minute", minute).apply();
        jsCallback("onReminderScheduled", String.format(Locale.US, "%02d:%02d", hour, minute));
    }

    private void initTts() {
        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status != TextToSpeech.SUCCESS || tts == null) {
                ttsReady = false;
                jsCallback("onNativeSpeechError", "Android could not start Text-to-Speech. Check that a TTS engine is installed and enabled.");
                return;
            }
            if (Build.VERSION.SDK_INT >= 21) {
                tts.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            }
            int languageStatus = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
            ttsReady = languageStatus != TextToSpeech.LANG_MISSING_DATA &&
                       languageStatus != TextToSpeech.LANG_NOT_SUPPORTED;
            tts.setSpeechRate(0.82f);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override public void onStart(String id) {}
                @Override public void onDone(String id) {}
                @Override public void onError(String id) {
                    jsCallback("onNativeSpeechError", "The TTS engine could not speak Chinese. Install Mandarin voice data in Android Text-to-Speech settings.");
                }
            });
            if (ttsReady && pendingText != null) {
                String text = pendingText;
                pendingText = null;
                speakNow(text);
            } else if (!ttsReady) {
                jsCallback("onNativeSpeechError", "Mandarin voice data is missing. Open Android Settings > Text-to-speech output and install a Chinese voice.");
            }
        });
    }

    private class NativeSpeechBridge {
        @JavascriptInterface public void speak(String text) {
            runOnUiThread(() -> {
                if (tts == null) {
                    pendingText = text;
                    initTts();
                    return;
                }
                if (!ttsReady) {
                    pendingText = text;
                    jsCallback("onNativeSpeechError", "Mandarin voice is not ready. Check Android Text-to-speech settings and try again.");
                    return;
                }
                speakNow(text);
            });
        }
        @JavascriptInterface public void stopSpeaking() {
            runOnUiThread(() -> { if (tts != null) tts.stop(); });
        }
        @JavascriptInterface public void startListening() {
            runOnUiThread(() -> {
                if (Build.VERSION.SDK_INT >= 23 &&
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    pendingListening = true;
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
                } else startChineseRecognition();
            });
        }
        @JavascriptInterface public void setDailyReminder(boolean enabled, int hour, int minute) {
            runOnUiThread(() -> scheduleDailyReminder(enabled, hour, minute));
        }
        @JavascriptInterface public String diagnostics() {
            return "ttsCreated=" + (tts != null) + ";ttsReady=" + ttsReady +
                   ";recognitionAvailable=" + SpeechRecognizer.isRecognitionAvailable(MainActivity.this);
        }
    }

    private void speakNow(String text) {
        if (tts == null || !ttsReady) {
            jsCallback("onNativeSpeechError", "Mandarin voice is unavailable. Install a Chinese voice in Android Text-to-speech settings.");
            return;
        }
        tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
        tts.stop();
        if (Build.VERSION.SDK_INT >= 21) {
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "zh-" + System.currentTimeMillis());
            if (result == TextToSpeech.ERROR)
                jsCallback("onNativeSpeechError", "Android TTS rejected playback. Check your selected speech engine and media volume.");
        } else {
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            if (result == TextToSpeech.ERROR)
                jsCallback("onNativeSpeechError", "Android TTS rejected playback. Check your selected speech engine and media volume.");
        }
    }

    private void startChineseRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            jsCallback("onNativeSpeechError", "No speech recognition service is enabled. Install or enable Google Speech Services, then try again.");
            return;
        }
        try {
            if (recognizer != null) { recognizer.cancel(); recognizer.destroy(); recognizer = null; }
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) { jsCallback("onNativeSpeechStarted", ""); }
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() { jsCallback("onNativeSpeechStarted", "Processing speech…"); }
                @Override public void onError(int error) {
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_NO_MATCH:
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "No speech detected. Tap the microphone and speak a little louder."; break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: message = "Microphone permission denied. Enable it in Android app settings."; break;
                        case SpeechRecognizer.ERROR_NETWORK:
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: message = "Recognition needs internet or an installed offline Chinese speech pack."; break;
                        case SpeechRecognizer.ERROR_AUDIO: message = "Microphone unavailable. Check permissions and whether another app is using it."; break;
                        default: message = "Speech recognition failed (code " + error + "). Check Chinese speech services.";
                    }
                    jsCallback("onNativeSpeechError", message);
                }
                @Override public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) jsCallback("onNativeSpeechResult", matches.get(0));
                    else jsCallback("onNativeSpeechError", "No speech detected. Please try again.");
                }
                @Override public void onPartialResults(Bundle results) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            recognizer.startListening(intent);
        } catch (Exception e) {
            jsCallback("onNativeSpeechError", "Could not start recognition. Check microphone permission and speech services.");
        }
    }

    private void jsCallback(String function, String message) {
        if (webView == null) return;
        String safe = (message == null ? "" : message)
            .replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ");
        runOnUiThread(() -> {
            if (webView != null) webView.evaluateJavascript("window." + function + "('" + safe + "')", null);
        });
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_AUDIO) {
            boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
            if (pendingWebPermission != null) {
                if (granted) pendingWebPermission.grant(pendingWebPermission.getResources());
                else pendingWebPermission.deny();
                pendingWebPermission = null;
            }
            if (pendingListening) {
                pendingListening = false;
                if (granted) startChineseRecognition();
                else jsCallback("onNativeSpeechError", "Microphone permission denied. Enable it in Android app settings.");
            }
        }
    }

    @Override protected void onDestroy() {
        if (recognizer != null) { recognizer.cancel(); recognizer.destroy(); recognizer = null; }
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        if (webView != null) { webView.removeJavascriptInterface("NativeSpeech"); webView.destroy(); webView = null; }
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
