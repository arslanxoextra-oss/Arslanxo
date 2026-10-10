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
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicInteger

/**
 * AdManager handles AdMob initialization, loading, and display of:
 * - Native Advanced Ads
 * - Rewarded Interstitial Ads (replacing standard interstitial)
 * - Rewarded Ads
 *
 * Uses solely the user's authentic AdMob units without any test ads.
 * App Open ads have been fully removed per user requirement.
 */
object AdManager {
    private const val TAG = "AdManager"

    // =========================================================================
    // Original AdMob Credentials provided by User
    // =========================================================================
    const val APP_ID = "ca-app-pub-8615333775681877~1241116749"
    const val NATIVE_AD_ID = "ca-app-pub-8615333775681877/8792303703"
    const val REWARDED_AD_ID = "ca-app-pub-8615333775681877/1368185813"
    const val REWARDED_INTERSTITIAL_AD_ID = "ca-app-pub-8615333775681877/2665879053"

    // Rewarded Interstitial Ad
    private var rewardedInterstitialAd: RewardedInterstitialAd? = null
    private var isRewardedInterstitialLoading = false

    // Rewarded Ad
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    // Native Ad
    private val _currentNativeAd = MutableStateFlow<NativeAd?>(null)
    val currentNativeAd: StateFlow<NativeAd?> = _currentNativeAd.asStateFlow()
    private var isNativeLoading = false

    // Post click counter for rhythmic rewarded interstitial display:
    // 1st click = open post without ad, 2nd click = show ad, 3rd = no ad, 4th = show ad...
    private val postClickCount = AtomicInteger(0)

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) {
                Log.d(TAG, "AdMob MobileAds initialized with original IDs")
                loadRewardedInterstitial(context)
                loadRewarded(context)
                loadNativeAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AdMob", e)
        }
    }

    // ==========================================
    // Native Ad Loader
    // ==========================================
    fun loadNativeAd(context: Context) {
        if (isNativeLoading || _currentNativeAd.value != null) return
        isNativeLoading = true

        try {
            val adLoader = AdLoader.Builder(context, NATIVE_AD_ID)
                .forNativeAd { ad: NativeAd ->
                    _currentNativeAd.value = ad
                    isNativeLoading = false
                    Log.d(TAG, "Native ad loaded successfully ($NATIVE_AD_ID)")
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        _currentNativeAd.value = null
                        isNativeLoading = false
                        Log.w(TAG, "Native ad failed to load ($NATIVE_AD_ID): ${adError.message}")
                    }
                })
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            isNativeLoading = false
            Log.e(TAG, "Failed to build AdLoader", e)
        }
    }

    // ==========================================
    // Rewarded Interstitial Ad Loader
    // ==========================================
    fun loadRewardedInterstitial(context: Context) {
        if (rewardedInterstitialAd != null || isRewardedInterstitialLoading) return
        isRewardedInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            REWARDED_INTERSTITIAL_AD_ID,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    rewardedInterstitialAd = ad
                    isRewardedInterstitialLoading = false
                    Log.d(TAG, "Rewarded Interstitial ad loaded successfully ($REWARDED_INTERSTITIAL_AD_ID)")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedInterstitialAd = null
                    isRewardedInterstitialLoading = false
                    Log.w(TAG, "Rewarded Interstitial ad failed to load ($REWARDED_INTERSTITIAL_AD_ID): ${error.message}")
                }
            }
        )
    }

    // ==========================================
    // Standard Rewarded Ad Loader
    // ==========================================
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
                    Log.d(TAG, "Rewarded ad loaded successfully ($REWARDED_AD_ID)")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded ad failed to load ($REWARDED_AD_ID): ${error.message}")
                }
            }
        )
    }

    /**
     * Checks whether this post click qualifies for an ad.
     * Pattern: 1st click -> false, 2nd click -> true, 3rd -> false, 4th -> true
     * Uses Rewarded Interstitial with original AdMob unit ID.
     */
    fun onPostClicked(activity: Activity, onAction: () -> Unit) {
        val count = postClickCount.incrementAndGet()
        val isAdTurn = (count % 2 == 0)

        val ad = rewardedInterstitialAd
        if (isAdTurn && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedInterstitialAd = null
                    loadRewardedInterstitial(activity)
                    onAction()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedInterstitialAd = null
                    loadRewardedInterstitial(activity)
                    onAction()
                }
            }
            ad.show(activity) { _ -> }
        } else {
            if (rewardedInterstitialAd == null) {
                loadRewardedInterstitial(activity)
            }
            onAction()
        }
    }

    /**
     * Shows rewarded ad for copying prompt or unlocking items / loading more.
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
            currentAd.show(activity) { _ -> }
        } else {
            loadRewarded(activity)
            onReward()
        }
    }
}
