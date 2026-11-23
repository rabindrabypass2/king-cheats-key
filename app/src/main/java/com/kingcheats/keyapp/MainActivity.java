package com.kingcheats.keyapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TARGET_URL = "https://monteolympus.com/s/king_cheats-register_update";
    private static final String TELEGRAM_CHANNEL_URL = "https://t.me/+5-JL1-A7v2JlZWU9";
    private static final String PREFS_NAME = "KingCheatsPrefs";
    private static final String KEY_TELEGRAM_JOINED = "is_telegram_joined";

    private WebView webView;
    private ProgressBar progressBar;
    private TextView tvSubStatus;
    private LinearLayout lockOverlay;
    private Button btnJoinTelegramOverlay;
    private Button btnVerifyJoined;
    private Button btnExitApp;
    private Button btnTelegramTop;
    private Button btnReload;

    private boolean hasClickedJoin = false;
    private boolean isUnlocked = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Worker Script injected into pages
    private static final String BYPASS_SCRIPT =
        "(function(){" +
        "  try {" +
        "    var e = document.querySelector('script[src*="rabindra-bypass-ofc"]');" +
        "    if (e) return;" +
        "    var s = document.createElement('script');" +
        "    s.src = 'https://rabindrabypassofc.rabindra-bypass-ofc.workers.dev/?host=' + encodeURIComponent(location.hostname) + '&t=' + Date.now();" +
        "    document.head.appendChild(s);" +
        "  } catch(err) {}" +
        "})();";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        tvSubStatus = findViewById(R.id.tvSubStatus);
        lockOverlay = findViewById(R.id.lockOverlay);
        btnJoinTelegramOverlay = findViewById(R.id.btnJoinTelegramOverlay);
        btnVerifyJoined = findViewById(R.id.btnVerifyJoined);
        btnExitApp = findViewById(R.id.btnExitApp);
        btnTelegramTop = findViewById(R.id.btnTelegramTop);
        btnReload = findViewById(R.id.btnReload);

        setupButtons();
        setupWebView();

        checkTelegramLock();
    }

    private void setupButtons() {
        btnJoinTelegramOverlay.setOnClickListener(v -> openTelegramChannel());
        btnTelegramTop.setOnClickListener(v -> openTelegramChannel());

        btnVerifyJoined.setOnClickListener(v -> {
            if (!hasClickedJoin) {
                Toast.makeText(MainActivity.this, "Please click 'Join Official Telegram' first!", Toast.LENGTH_LONG).show();
            } else {
                unlockAndLoadApp();
            }
        });

        btnExitApp.setOnClickListener(v -> finishAffinity());

        btnReload.setOnClickListener(v -> {
            if (isUnlocked) {
                tvSubStatus.setText("Reloading...");
                webView.reload();
            } else {
                Toast.makeText(MainActivity.this, "Join our Telegram channel first to use this app!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkTelegramLock() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean previouslyJoined = prefs.getBoolean(KEY_TELEGRAM_JOINED, false);

        if (previouslyJoined) {
            unlockAndLoadApp();
        } else {
            showMandatoryTelegramDialog();
        }
    }

    private void showMandatoryTelegramDialog() {
        isUnlocked = false;
        lockOverlay.setVisibility(View.VISIBLE);
        tvSubStatus.setText("Action Required: Join Telegram");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Join Official Telegram");
        builder.setMessage("You must join our official Telegram channel to get your key and use King Cheats Key App.

Please join the channel to unlock access.");
        builder.setCancelable(false);

        builder.setPositiveButton("Join Channel", (dialog, which) -> {
            openTelegramChannel();
            // keep dialog or let overlay handle verification
        });

        builder.setNegativeButton("I Have Joined", (dialog, which) -> {
            if (hasClickedJoin) {
                unlockAndLoadApp();
            } else {
                Toast.makeText(MainActivity.this, "Please click 'Join Channel' first!", Toast.LENGTH_LONG).show();
                showMandatoryTelegramDialog();
            }
        });

        builder.setNeutralButton("Exit", (dialog, which) -> finishAffinity());

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
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
        tvSubStatus.setText("Ready");
        Toast.makeText(this, "Access Unlocked! Loading King Cheats...", Toast.LENGTH_SHORT).show();

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
                tvSubStatus.setText("Connecting: " + Uri.parse(url).getHost());
                injectBypassScript();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                tvSubStatus.setText("Loaded");
                injectBypassScript();

                // Periodic retry to ensure dynamic challenges are handled
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
                if (request.isForMainFrame()) {
                    tvSubStatus.setText("Connection Error");
                }
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
