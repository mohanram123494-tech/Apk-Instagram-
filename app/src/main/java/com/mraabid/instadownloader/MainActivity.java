package com.mraabid.instadownloader;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;
    private AdView mAdView;

    // AdMob IDs
    private static final String BANNER_ID      = "ca-app-pub-4589986408147092/7739748620";
    private static final String INTERSTITIAL_ID = "ca-app-pub-4589986408147092/6589328803";
    private static final String REWARDED_ID     = "ca-app-pub-4589986408147092/5746570718";

    private static final int PERMISSION_REQUEST = 101;

    // Pending rewarded download
    private String pendingRewardUrl = "";
    private String pendingRewardExt = "mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init AdMob
        MobileAds.initialize(this, initializationStatus -> {});

        setupBannerAd();
        setupWebView();
        loadInterstitialAd();
        loadRewardedAd();
        requestStoragePermission();
    }

    // ─── BANNER ─────────────────────────────────────────────────────────────
    private void setupBannerAd() {
        mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(BANNER_ID);
        LinearLayout bannerContainer = findViewById(R.id.banner_container);
        bannerContainer.addView(mAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    // ─── INTERSTITIAL ────────────────────────────────────────────────────────
    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, INTERSTITIAL_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                mInterstitialAd = null;
                                loadInterstitialAd(); // reload
                            }
                        });
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                    }
                });
    }

    public void showInterstitialAd() {
        runOnUiThread(() -> {
            if (mInterstitialAd != null) {
                mInterstitialAd.show(MainActivity.this);
            } else {
                loadInterstitialAd();
            }
        });
    }

    // ─── REWARDED ────────────────────────────────────────────────────────────
    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, REWARDED_ID, adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                    }
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mRewardedAd = null;
                    }
                });
    }

    public void showRewardedAdAndDownload(String mediaUrl, String ext) {
        pendingRewardUrl = mediaUrl;
        pendingRewardExt = ext;
        runOnUiThread(() -> {
            if (mRewardedAd != null) {
                mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        mRewardedAd = null;
                        loadRewardedAd();
                    }
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        // Reward anyway
                        downloadToGallery(pendingRewardUrl, pendingRewardExt);
                    }
                });
                mRewardedAd.show(MainActivity.this, rewardItem -> {
                    // User earned reward — download
                    downloadToGallery(pendingRewardUrl, pendingRewardExt);
                });
            } else {
                // No ad loaded — download anyway
                downloadToGallery(pendingRewardUrl, pendingRewardExt);
                loadRewardedAd();
            }
        });
    }

    // ─── DOWNLOAD MANAGER ───────────────────────────────────────────────────
    public void downloadToGallery(String url, String ext) {
        runOnUiThread(() -> {
            try {
                String filename = "MrAabid_" + System.currentTimeMillis() + "." + ext;
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setTitle("Insta Downloader");
                request.setDescription("Downloading " + filename);
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS, filename);
                request.allowScanningByMediaScanner();
                DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(this, "Download started! Check notification.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ─── WEBVIEW ─────────────────────────────────────────────────────────────
    private void setupWebView() {
        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());

        // JavaScript Interface
        webView.addJavascriptInterface(new AndroidInterface(), "AndroidInterface");
        webView.loadUrl("file:///android_asset/index.html");
    }

    // ─── JS INTERFACE ────────────────────────────────────────────────────────
    private class AndroidInterface {

        @JavascriptInterface
        public void downloadMedia(String url, String filename) {
            String ext = filename.endsWith(".mp4") ? "mp4" : "jpg";
            downloadToGallery(url, ext);
            showInterstitialAd();
        }

        @JavascriptInterface
        public void showInterstitialAd() {
            MainActivity.this.showInterstitialAd();
        }

        @JavascriptInterface
        public void showRewardedAd(String mediaUrl, String ext) {
            showRewardedAdAndDownload(mediaUrl, ext);
        }
    }

    // ─── PERMISSIONS ─────────────────────────────────────────────────────────
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) mAdView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) mAdView.resume();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) mAdView.destroy();
        super.onDestroy();
    }
}
