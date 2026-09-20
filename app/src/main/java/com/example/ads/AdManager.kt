package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

object AdManager {
    private const val TAG = "AdManager"

    const val APP_ID = "ca-app-pub-8106043013088313~4327179238"
    const val NATIVE_AD_ID = "ca-app-pub-8106043013088313/5440801967"
    const val INTERSTITIAL_AD_ID = "ca-app-pub-8106043013088313/2147203021"
    const val REWARDED_AD_ID = "ca-app-pub-8106043013088313/2881750523"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInterstitialLoading = false
    private var isRewardedLoading = false

    private val _currentNativeAd = MutableStateFlow<NativeAd?>(null)
    val currentNativeAd: StateFlow<NativeAd?> = _currentNativeAd.asStateFlow()

    private var isNativeLoading = false

    // Post click counter for rhythmic interstitial display:
    // 1st click = no ad, 2nd click = show ad, 3rd = no ad, 4th = show ad...
    private val postClickCount = AtomicInteger(0)

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) {
                Log.d(TAG, "AdMob MobileAds initialized")
                loadInterstitial(context)
                loadRewarded(context)
                loadNativeAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob", e)
        }
    }

    fun loadNativeAd(context: Context) {
        if (isNativeLoading || _currentNativeAd.value != null) return
        isNativeLoading = true

        try {
            val adLoader = AdLoader.Builder(context, NATIVE_AD_ID)
                .forNativeAd { ad: NativeAd ->
                    _currentNativeAd.value = ad
                    isNativeLoading = false
                    Log.d(TAG, "Native ad loaded successfully")
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        _currentNativeAd.value = null
                        isNativeLoading = false
                        Log.w(TAG, "Native ad failed to load: ${adError.message}")
                    }
                })
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            isNativeLoading = false
            Log.e(TAG, "Failed to build AdLoader", e)
        }
    }

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            REWARDED_AD_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Checks whether this post click qualifies for an interstitial ad.
     * Pattern: 1st click -> false, 2nd click -> true, 3rd -> false, 4th -> true
     * Zero-delay guarantee: If no ad is loaded, opens post immediately with zero lag!
     */
    fun onPostClicked(activity: Activity, onAction: () -> Unit) {
        val count = postClickCount.incrementAndGet()
        val isAdTurn = (count % 2 == 0)

        val ad = interstitialAd
        if (isAdTurn && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAction()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAction()
                }
            }
            ad.show(activity)
        } else {
            // Immediate transition without delay
            if (interstitialAd == null) {
                loadInterstitial(activity)
            }
            onAction()
        }
    }

    /**
     * Shows rewarded ad for copying prompt or loading more posts.
     * Zero-delay guarantee: If ad is not ready, executes reward immediately without blocking the user!
     */
    fun showRewardedAd(activity: Activity, onReward: () -> Unit) {
        val currentAd = rewardedAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity)
                    onReward()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewarded(activity)
                    onReward()
                }
            }

            currentAd.show(activity) { _ ->
                // Reward earned
            }
        } else {
            // No ad available -> execute immediately with 0 wait time
            loadRewarded(activity)
            onReward()
        }
    }
}
