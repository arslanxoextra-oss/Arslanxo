package com.example.ads

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Dynamic AdMob configuration model loaded from Firestore `Settings/admob`
 * (managed via Lovable admin panel or Firebase Console).
 * Supports the 3 active formats:
 * 1. Native advanced (Native ad)
 * 2. Interstitial (Industrial ad - replaces rewarded interstitial)
 * 3. Rewarded (Divided ad)
 */
data class AdConfig(
    val appId: String = "ca-app-pub-8615333775681877~1241116749",
    val isAdsEnabled: Boolean = true,
    val nativeAdUnitId: String = "",
    val interstitialAdUnitId: String = "",
    val rewardedAdUnitId: String = ""
) {
    /**
     * Returns true if at least one ad unit ID is configured.
     */
    val hasAnyAdUnit: Boolean
        get() = nativeAdUnitId.isNotBlank() ||
                interstitialAdUnitId.isNotBlank() ||
                rewardedAdUnitId.isNotBlank()
}

/**
 * Handles fetching, parsing, caching, and live synchronization of AdMob
 * configuration from Firebase Firestore collection `Settings`, document `admob`.
 */
object AdConfigRepository {
    private const val TAG = "AdConfigRepository"
    private const val PREFS_NAME = "promptxo_admob_config_prefs"

    private const val PREF_KEY_APP_ID = "admob_app_id"
    private const val PREF_KEY_ADS_ENABLED = "admob_ads_enabled"
    private const val PREF_KEY_NATIVE_ID = "admob_native_id"
    private const val PREF_KEY_INTERSTITIAL_ID = "admob_interstitial_id"
    private const val PREF_KEY_REWARDED_ID = "admob_rewarded_id"
    private const val PREF_KEY_HAS_SAVED = "admob_has_saved_config"

    private val _currentConfig = MutableStateFlow(AdConfig())
    val currentConfig: StateFlow<AdConfig> = _currentConfig.asStateFlow()

    private var fallbackRegistered = false

    /**
     * Loads the last known valid configuration from SharedPreferences cache.
     * Enables offline startup and eliminates unnecessary latency.
     */
    fun loadFromCache(context: Context): AdConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasSaved = prefs.getBoolean(PREF_KEY_HAS_SAVED, false)

        val config = if (hasSaved) {
            AdConfig(
                appId = prefs.getString(PREF_KEY_APP_ID, "ca-app-pub-8615333775681877~1241116749") ?: "ca-app-pub-8615333775681877~1241116749",
                isAdsEnabled = prefs.getBoolean(PREF_KEY_ADS_ENABLED, true),
                nativeAdUnitId = prefs.getString(PREF_KEY_NATIVE_ID, "") ?: "",
                interstitialAdUnitId = prefs.getString(PREF_KEY_INTERSTITIAL_ID, "") ?: "",
                rewardedAdUnitId = prefs.getString(PREF_KEY_REWARDED_ID, "") ?: ""
            )
        } else {
            // Default initial state before first Firestore fetch
            AdConfig(
                appId = "ca-app-pub-8615333775681877~1241116749",
                isAdsEnabled = true,
                nativeAdUnitId = "ca-app-pub-8615333775681877/8792303703",
                interstitialAdUnitId = "ca-app-pub-8615333775681877/2665879053",
                rewardedAdUnitId = "ca-app-pub-8615333775681877/1368185813"
            )
        }

        _currentConfig.value = config
        return config
    }

    /**
     * Persists valid configuration to SharedPreferences for instant retrieval on next launch.
     */
    fun saveToCache(context: Context, config: AdConfig) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(PREF_KEY_APP_ID, config.appId)
                .putBoolean(PREF_KEY_ADS_ENABLED, config.isAdsEnabled)
                .putString(PREF_KEY_NATIVE_ID, config.nativeAdUnitId)
                .putString(PREF_KEY_INTERSTITIAL_ID, config.interstitialAdUnitId)
                .putString(PREF_KEY_REWARDED_ID, config.rewardedAdUnitId)
                .putBoolean(PREF_KEY_HAS_SAVED, true)
                .apply()
            Log.d(TAG, "AdMob configuration cached successfully (native=${config.nativeAdUnitId}, inter=${config.interstitialAdUnitId}, rewarded=${config.rewardedAdUnitId})")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cache AdMob configuration", e)
        }
    }

    /**
     * Starts listening to Firestore document `Settings/admob` (with fallback to `settings/admob`).
     * Automatically updates in real time whenever values are changed on backend.
     */
    fun listenToConfig(context: Context, onConfigUpdated: (AdConfig) -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Primary path: Settings/admob
            db.collection("Settings").document("admob")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Snapshot error for Settings/admob: ${error.message}")
                        registerFallbackListenerIfNeeded(db, context, onConfigUpdated)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val rawData = snapshot.data
                        if (rawData != null) {
                            val parsed = parseDocumentData(rawData)
                            _currentConfig.value = parsed
                            saveToCache(context, parsed)
                            onConfigUpdated(parsed)
                            return@addSnapshotListener
                        }
                    }

                    // If Settings/admob is missing, attempt fallback to lowercase settings/admob
                    registerFallbackListenerIfNeeded(db, context, onConfigUpdated)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firestore listener for Settings/admob", e)
        }
    }

    private fun registerFallbackListenerIfNeeded(
        db: FirebaseFirestore,
        context: Context,
        onConfigUpdated: (AdConfig) -> Unit
    ) {
        if (fallbackRegistered) return
        fallbackRegistered = true

        try {
            db.collection("settings").document("admob")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Snapshot error for fallback settings/admob: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val rawData = snapshot.data
                        if (rawData != null) {
                            val parsed = parseDocumentData(rawData)
                            _currentConfig.value = parsed
                            saveToCache(context, parsed)
                            onConfigUpdated(parsed)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register fallback listener for settings/admob", e)
        }
    }

    /**
     * Parses Firestore document data into AdConfig.
     * Uses fuzzy, case-insensitive key detection to guarantee that updates made via
     * Lovable Admin Panel or Firebase Console (including 'divided', 'industrial', etc.)
     * are reliably captured.
     */
    fun parseDocumentData(data: Map<String, Any?>): AdConfig {
        val flattenedMap = mutableMapOf<String, Any?>()

        fun extractKeys(prefix: String, map: Map<*, *>) {
            for ((rawKey, value) in map) {
                val keyStr = rawKey?.toString() ?: continue
                val fullKey = if (prefix.isEmpty()) keyStr else "$prefix.$keyStr"
                flattenedMap[fullKey] = value
                flattenedMap[keyStr] = value
                if (value is Map<*, *>) {
                    extractKeys(fullKey, value)
                }
            }
        }

        extractKeys("", data)

        fun cleanKey(k: String): String =
            k.lowercase().replace(" ", "").replace("_", "").replace("-", "").replace(".", "")

        fun findString(vararg directKeys: String, predicate: (String) -> Boolean): String {
            // 1. Try exact direct keys first
            for (dk in directKeys) {
                val v = flattenedMap[dk]
                if (v is String && v.isNotBlank()) return v.trim()
                if (v != null && v !is Map<*, *> && v.toString().isNotBlank()) return v.toString().trim()
            }
            // 2. Try cleaned key matching
            for ((k, v) in flattenedMap) {
                if (v != null && v !is Map<*, *>) {
                    val sVal = v.toString().trim()
                    if (sVal.isNotBlank() && predicate(cleanKey(k))) {
                        return sVal
                    }
                }
            }
            return ""
        }

        fun findBoolean(defaultVal: Boolean, vararg directKeys: String): Boolean {
            for (dk in directKeys) {
                val v = flattenedMap[dk]
                if (v is Boolean) return v
                if (v is String) {
                    if (v.equals("true", ignoreCase = true)) return true
                    if (v.equals("false", ignoreCase = true)) return false
                }
                if (v is Number) return v.toInt() == 1
            }
            return defaultVal
        }

        val appId = findString(
            "appId", "app_id", "applicationId", "application_id", "admobAppId", "admob_app_id", "App ID"
        ) { it.contains("appid") || it.contains("applicationid") }
            .ifBlank { "ca-app-pub-8615333775681877~1241116749" }

        val isAdsEnabled = findBoolean(
            true,
            "enabled", "isAdsEnabled", "adsEnabled", "enableAds", "is_enabled", "ads_enabled", "active"
        )

        // 1. Native Advanced
        val native = findString(
            "Native advanced", "Native Advanced", "native_advanced", "native advanced",
            "nativeAdUnitId", "native_ad_unit_id", "nativeAdId", "native_ad_id",
            "nativeId", "native_id", "native", "native_ad", "nativeAd"
        ) { it.contains("native") }

        // 2. Interstitial (Industrial - replacing Rewarded Interstitial)
        val interstitial = findString(
            "Interstitial", "interstitial", "industrial", "Industrial",
            "interstitialAdUnitId", "interstitial_ad_unit_id", "interstitialAdId", "interstitial_ad_id",
            "interstitialId", "interstitial_id", "interstitial_ad", "interstitialAd",
            "inter", "inter_ad", "Rewarded interstitial", "rewarded_interstitial"
        ) { it.contains("interstitial") || it.contains("industrial") }

        // 3. Rewarded (Divided) - robust lookup so it updates without fail!
        val rewarded = findString(
            "Rewarded", "rewarded", "divided", "Divided", "reward", "Reward",
            "rewardedAdUnitId", "rewarded_ad_unit_id", "rewardedAdId", "rewarded_ad_id",
            "rewardedId", "rewarded_id", "rewarded_ad", "rewardedAd",
            "rewardedVideo", "rewarded_video", "rewardedVideoAdUnitId", "rewarded_video_ad_unit_id",
            "divided_ad", "dividedAd", "dividedAdUnitId"
        ) { it.contains("reward") || it.contains("divided") }

        return AdConfig(
            appId = appId,
            isAdsEnabled = isAdsEnabled,
            nativeAdUnitId = native,
            interstitialAdUnitId = interstitial,
            rewardedAdUnitId = rewarded
        )
    }
}
