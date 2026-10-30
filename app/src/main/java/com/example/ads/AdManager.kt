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

/**
 * AdManager handles dynamic AdMob configuration loaded from Firebase Firestore (Settings/admob),
 * initialization, caching, and display of the 3 specified formats:
 * 1. Native advanced (Native ad)
 * 2. Interstitial (Industrial ad - replaces rewarded interstitial)
 * 3. Rewarded (Divided ad)
 *
 * All editable Ad Unit IDs are fetched dynamically from Firebase and are never hardcoded.
 */
object AdManager {
    private const val TAG = "AdManager"

    // Retained in native Android configuration per instructions
    const val APP_ID = "ca-app-pub-8615333775681877~1241116749"

    // Active AdMob configuration (reactive flow)
    private val _adConfig = MutableStateFlow(AdConfig())
    val adConfig: StateFlow<AdConfig> = _adConfig.asStateFlow()

    // 1. Standard Interstitial Ad (Industrial Ad)
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    // 2. Rewarded Ad (Divided Ad)
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    // 3. Native Advanced Ad
    private val _currentNativeAd = MutableStateFlow<NativeAd?>(null)
    val currentNativeAd: StateFlow<NativeAd?> = _currentNativeAd.asStateFlow()
    private var isNativeLoading = false

    // Post click counter for rhythmic interstitial display (joori / paired):
    // 1st click = open post without ad, 2nd click = show ad, 3rd = no ad, 4th = show ad...
    private val postClickCount = AtomicInteger(0)

    private var isMobileAdsInitialized = false
    private var appContext: Context? = null

    /**
     * Initializes AdMob with local cache first for zero-delay startup,
     * then subscribes to Firestore Settings/admob for real-time updates.
     */
    fun initialize(context: Context) {
        appContext = context.applicationContext

        // 1. Immediately read cached configuration (offline-ready, instant startup)
        val cachedConfig = AdConfigRepository.loadFromCache(context)
        _adConfig.value = cachedConfig
        Log.d(TAG, "AdMob loaded initial cached config: isAdsEnabled=${cachedConfig.isAdsEnabled}, hasUnits=${cachedConfig.hasAnyAdUnit}")

        // 2. Initialize MobileAds SDK
        try {
            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob MobileAds SDK initialized successfully: $status")
                isMobileAdsInitialized = true
                if (_adConfig.value.isAdsEnabled) {
                    preloadConfiguredAds(context)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MobileAds SDK", e)
        }

        // 3. Listen to Firestore Settings/admob dynamically (live sync from Firebase)
        AdConfigRepository.listenToConfig(context) { newConfig ->
            onConfigUpdated(newConfig)
        }
    }

    /**
     * Invoked whenever Firestore Settings/admob produces a new configuration snapshot.
     */
    fun onConfigUpdated(newConfig: AdConfig) {
        val oldConfig = _adConfig.value
        _adConfig.value = newConfig
        Log.d(TAG, "Dynamic AdMob configuration updated from Firestore: native=${newConfig.nativeAdUnitId}, inter=${newConfig.interstitialAdUnitId}, rewarded=${newConfig.rewardedAdUnitId}")

        val ctx = appContext ?: return

        if (!newConfig.isAdsEnabled) {
            // Disabled: release in-memory ads
            interstitialAd = null
            rewardedAd = null
            _currentNativeAd.value = null
            Log.d(TAG, "Ads disabled in Settings/admob. In-memory ads cleared.")
            return
        }

        // If ads are enabled, reload ads whenever IDs change
        if (isMobileAdsInitialized) {
            // Native ad update
            val nativeChanged = oldConfig.nativeAdUnitId != newConfig.nativeAdUnitId
            if (nativeChanged || _currentNativeAd.value == null) {
                _currentNativeAd.value = null
                isNativeLoading = false
                if (newConfig.nativeAdUnitId.isNotBlank()) {
                    loadNativeAd(ctx)
                }
            }

            // Interstitial (industrial) ad update
            val interstitialChanged = oldConfig.interstitialAdUnitId != newConfig.interstitialAdUnitId
            if (interstitialChanged || interstitialAd == null) {
                interstitialAd = null
                isInterstitialLoading = false
                if (newConfig.interstitialAdUnitId.isNotBlank()) {
                    loadInterstitial(ctx)
                }
            }

            // Rewarded (divided) ad update - force reset so it always loads newly assigned ID
            val rewardedChanged = oldConfig.rewardedAdUnitId != newConfig.rewardedAdUnitId
            if (rewardedChanged || rewardedAd == null) {
                rewardedAd = null
                isRewardedLoading = false
                if (newConfig.rewardedAdUnitId.isNotBlank()) {
                    loadRewarded(ctx)
                }
            }
        }
    }

    private fun preloadConfiguredAds(context: Context) {
        if (!_adConfig.value.isAdsEnabled) return
        loadNativeAd(context)
        loadInterstitial(context)
        loadRewarded(context)
    }

    // ==========================================
    // 1. Native Advanced Ad Loader
    // ==========================================
    fun loadNativeAd(context: Context) {
        val config = _adConfig.value
        if (!config.isAdsEnabled) return

        val adUnitId = config.nativeAdUnitId.trim()
        if (adUnitId.isBlank()) return

        if (isNativeLoading || _currentNativeAd.value != null) return
        isNativeLoading = true

        try {
            val adLoader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { ad: NativeAd ->
                    _currentNativeAd.value = ad
                    isNativeLoading = false
                    Log.d(TAG, "Native ad loaded successfully ($adUnitId)")
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        _currentNativeAd.value = null
                        isNativeLoading = false
                        Log.w(TAG, "Native ad failed to load ($adUnitId): ${adError.message}")
                    }
                })
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            isNativeLoading = false
            Log.e(TAG, "Failed to build or load NativeAd ($adUnitId)", e)
        }
    }

    // ==========================================
    // 2. Interstitial Ad Loader (Industrial Ad)
    // ==========================================
    fun loadInterstitial(context: Context) {
        val config = _adConfig.value
        if (!config.isAdsEnabled) return

        val adUnitId = config.interstitialAdUnitId.trim()
        if (adUnitId.isBlank()) return

        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        isInterstitialLoading = false
                        Log.d(TAG, "Interstitial (industrial) ad loaded successfully ($adUnitId)")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                        isInterstitialLoading = false
                        Log.w(TAG, "Interstitial (industrial) ad failed to load ($adUnitId): ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isInterstitialLoading = false
            Log.e(TAG, "Failed to load InterstitialAd ($adUnitId)", e)
        }
    }

    // ==========================================
    // 3. Rewarded Ad Loader (Divided Ad)
    // ==========================================
    fun loadRewarded(context: Context) {
        val config = _adConfig.value
        if (!config.isAdsEnabled) return

        val adUnitId = config.rewardedAdUnitId.trim()
        if (adUnitId.isBlank()) return

        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                        Log.d(TAG, "Rewarded (divided) ad loaded successfully ($adUnitId)")
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        Log.w(TAG, "Rewarded (divided) ad failed to load ($adUnitId): ${error.message}")
                    }
                }
            )
        } catch (e: Exception) {
            isRewardedLoading = false
            Log.e(TAG, "Failed to load RewardedAd ($adUnitId)", e)
        }
    }

    /**
     * Paired (Joori) Post Click Ad using standard Interstitial (Industrial) ad:
     * Pattern: 1st click = direct, 2nd click = Interstitial ad, 3rd = direct, 4th = ad.
     * If the ad is not ready on the 2nd click, the turn is preserved so the user gets the exact paired experience.
     */
    fun onPostClicked(activity: Activity, onAction: () -> Unit) {
        if (!_adConfig.value.isAdsEnabled || _adConfig.value.interstitialAdUnitId.isBlank()) {
            onAction()
            return
        }

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
            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Exception showing InterstitialAd", e)
                interstitialAd = null
                loadInterstitial(activity)
                onAction()
            }
        } else {
            if (isAdTurn && ad == null) {
                // If it was the 2nd click turn but ad was still loading, preserve the ad turn
                postClickCount.decrementAndGet()
                loadInterstitial(activity)
            } else if (interstitialAd == null) {
                loadInterstitial(activity)
            }
            onAction()
        }
    }

    /**
     * Shows Rewarded (Divided) Ad for copying prompt or unlocking items / loading more.
     * Guaranteed to use the live Firebase rewarded ad unit.
     */
    fun showRewardedAd(activity: Activity, onReward: () -> Unit) {
        if (!_adConfig.value.isAdsEnabled || _adConfig.value.rewardedAdUnitId.isBlank()) {
            onReward()
            return
        }

        val currentRewarded = rewardedAd

        if (currentRewarded != null) {
            currentRewarded.fullScreenContentCallback = object : FullScreenContentCallback() {
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
            try {
                currentRewarded.show(activity) { _ -> }
            } catch (e: Exception) {
                Log.e(TAG, "Exception showing RewardedAd", e)
                rewardedAd = null
                loadRewarded(activity)
                onReward()
            }
        } else {
            // Trigger load for next time and let the action complete seamlessly
            loadRewarded(activity)
            onReward()
        }
    }
}
