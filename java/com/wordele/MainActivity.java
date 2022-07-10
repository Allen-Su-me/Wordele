package com.wordele;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.rewarded.RewardItem;


public class MainActivity extends AppCompatActivity {
    private AdView mAdView;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final String AD_UNIT_ID = "ca-app-pub-9442282694083392/2540746669";
    //ca-app-pub-9442282694083392/2540746669
    //ca-app-pub-3940256099942544/5224354917
    private static final String TAG = "MyActivity";
    private boolean gameOver;
    private boolean gamePaused;
    private RewardedAd rewardedAd;

    private Button showVideoButton;
    boolean isLoading;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefreshLayout = findViewById(R.id.refreshLayout);

        WebView webView = findViewById(R.id.local);
        WebSettings set = webView.getSettings();
        set.setJavaScriptEnabled(true);
        set.setAllowContentAccess(true);
        set.setAppCacheEnabled(true);
        set.setDomStorageEnabled(true);
        set.setSupportMultipleWindows(true);
        set.setJavaScriptCanOpenWindowsAutomatically(true);
        set.setAllowFileAccess(true);
        set.setBuiltInZoomControls(false);
        set.setDisplayZoomControls(false);
        set.setLoadWithOverviewMode(true);
        set.setUseWideViewPort(true);
        set.setAllowFileAccessFromFileURLs(true);
        set.setAllowUniversalAccessFromFileURLs(true);
        set.setSupportZoom(false);
        set.setTextZoom(100);
        set.setDatabaseEnabled(true);





        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);

                webView.reload();

            }
        });
        swipeRefreshLayout.setColorSchemeColors(Color.BLACK);


        loadRewardedAd();
        // Create the "show" button, which shows a rewarded video if one is loaded.
        showVideoButton = findViewById(R.id.show_video_button);
        showVideoButton.setVisibility(View.VISIBLE);
        showVideoButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showRewardedVideo();
                    }
                });



        startGame();
        webView.loadUrl("file:///android_asset/index.html");


    }

    @Override
    public void onPause() {
        super.onPause();
        pauseGame();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!gameOver && gamePaused) {
            resumeGame();
        }
    }

    private void pauseGame() {

        gamePaused = true;
    }

    private void resumeGame() {

        gamePaused = false;
    }



    private void loadRewardedAd() {
        if (rewardedAd == null) {
            isLoading = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(
                    this,
                    AD_UNIT_ID,
                    adRequest,
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error.
                            Log.d(TAG, loadAdError.getMessage());
                            rewardedAd = null;
                            MainActivity.this.isLoading = false;
                            Toast.makeText(MainActivity.this, "ad failed to load", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            MainActivity.this.rewardedAd = rewardedAd;
                            //Log.d(TAG, "onAdLoaded");
                            MainActivity.this.isLoading = false;
                            //Toast.makeText(MainActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    private void startGame() {
        // Hide the retry button, load the ad, and start the timer.
        if (rewardedAd != null && !isLoading) {
            loadRewardedAd();
        }
        gamePaused = false;
        gameOver = false;
    }

    private void showRewardedVideo() {

        if (rewardedAd == null) {
            Toast.makeText(MainActivity.this, "The rewarded ad wasn't ready yet.", Toast.LENGTH_SHORT)
                    .show();
            return;
        }


        rewardedAd.setFullScreenContentCallback(
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {

                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        // Called when ad fails to show.
                        //Log.d(TAG, "onAdFailedToShowFullScreenContent");
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when ad is dismissed.
                        // Don't forget to set the ad reference to null so you
                        // don't show the ad a second time.
                        rewardedAd = null;
                        //Toast.makeText(MainActivity.this, "onAdDismissedFullScreenContent", Toast.LENGTH_SHORT)
                        //        .show();
                        // Preload the next rewarded ad.
                        MainActivity.this.loadRewardedAd();
                    }
                });
        Activity activityContext = MainActivity.this;
        rewardedAd.show(
                activityContext,
                new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        // Handle the reward.
                        int rewardAmount = rewardItem.getAmount();
                        String rewardType = rewardItem.getType();

                        WebView webView = findViewById(R.id.local);
                        webView.loadUrl("javascript:reward();");
                    }
                });


    }



}
