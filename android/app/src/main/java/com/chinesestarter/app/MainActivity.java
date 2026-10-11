package com.chinesestarter.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private PermissionRequest pendingRequest;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private SpeechRecognizer recognizer;
    private boolean pendingListening = false;
    private static final int REQUEST_AUDIO = 1001;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        setContentView(webView);
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
                ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED;
                if (!ttsReady) runOnUiThread(() -> Toast.makeText(this,
                    "Install a Mandarin voice in Android Text-to-speech settings.", Toast.LENGTH_LONG).show());
            }
        });

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
                boolean audio = false;
                for (String resource : request.getResources())
                    if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) audio = true;
                if (audio && Build.VERSION.SDK_INT >= 23 &&
                    checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    pendingRequest = request;
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO);
                } else request.grant(request.getResources());
            }
        });
        webView.loadUrl("file:///android_asset/web/index.html");
    }

    private class NativeSpeechBridge {
        @JavascriptInterface public void speak(String text) {
            runOnUiThread(() -> {
                if (tts == null || !ttsReady) {
                    jsCallback("onNativeSpeechError", "Mandarin audio voice is unavailable. Install a Chinese voice in Android Text-to-Speech settings.");
                    return;
                }
                tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
                if (Build.VERSION.SDK_INT >= 21)
                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "zh-" + System.currentTimeMillis());
                else tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
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
    }

    private void startChineseRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            jsCallback("onNativeSpeechError", "No speech recognition service is enabled. Install/enable Google Speech Services or another Android recognition service.");
            return;
        }
        try {
            if (recognizer != null) { recognizer.destroy(); recognizer = null; }
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) { jsCallback("onNativeSpeechStarted", ""); }
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {}
                @Override public void onError(int error) {
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_NO_MATCH:
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "No speech detected. Tap the microphone and try again."; break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: message = "Microphone permission denied. Enable it in Android app settings."; break;
                        case SpeechRecognizer.ERROR_NETWORK:
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: message = "Recognition needs a network connection or an offline Chinese speech pack."; break;
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
            recognizer.startListening(intent);
        } catch (Exception e) {
            jsCallback("onNativeSpeechError", "Could not start recognition. Check microphone permission and speech services.");
        }
    }

    private void jsCallback(String function, String message) {
        if (webView == null) return;
        String safe = (message == null ? "" : message).replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ");
        runOnUiThread(() -> webView.evaluateJavascript("window." + function + "('" + safe + "')", null));
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_AUDIO) {
            boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
            if (pendingRequest != null) {
                if (granted) pendingRequest.grant(pendingRequest.getResources()); else pendingRequest.deny();
                pendingRequest = null;
            }
            if (pendingListening) {
                pendingListening = false;
                if (granted) startChineseRecognition();
                else jsCallback("onNativeSpeechError", "Microphone permission denied. Enable it in Android app settings.");
            }
        }
    }

    @Override protected void onDestroy() {
        if (recognizer != null) { recognizer.destroy(); recognizer = null; }
        if (tts != null) { tts.stop(); tts.shutdown(); tts = null; }
        if (webView != null) { webView.removeJavascriptInterface("NativeSpeech"); webView.destroy(); webView = null; }
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
