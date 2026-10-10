package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.ads.AdManager
import okhttp3.Cache
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

class MyApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Initialize AdManager at the Application level for App Open Ad lifecycle support
        AdManager.initialize(this)
    }

    override fun newImageLoader(): ImageLoader {
        // High concurrency OkHttpClient for blazing fast image downloads
        val dispatcher = Dispatcher().apply {
            maxRequests = 64
            maxRequestsPerHost = 16
        }

        // Cache HTTP responses directly on disk for 250 MB
        val httpCacheDirectory = File(cacheDir, "http_image_cache")
        val httpResponseCache = Cache(httpCacheDirectory, 250L * 1024 * 1024)

        val okHttpClient = OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .cache(httpResponseCache)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    // Allocate 35% of available app RAM for instant memory hit
                    .maxSizePercent(0.35)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_disk_cache"))
                    // 350 MB disk cache for instant offline & zero-wait repeat image loading
                    .maxSizeBytes(350L * 1024 * 1024)
                    .build()
            }
            // Aggressive caching policy for instant loading
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Force-cache images even if origin doesn't specify Cache-Control
            .crossfade(true)
            .crossfade(150)
            .build()
    }
}
