package com.example.util

import android.util.Log
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val TAG = "DateUtils"

    private val supportedFormats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    )

    /**
     * Extracts epoch milliseconds from any representation:
     * - Firebase Timestamp
     * - java.util.Date
     * - Number (seconds or milliseconds)
     * - Date String
     */
    fun parseTimestampMillis(raw: Any?): Long {
        if (raw == null) return 0L
        return try {
            when (raw) {
                is Timestamp -> raw.toDate().time
                is Date -> raw.time
                is Number -> {
                    val num = raw.toLong()
                    // If num < 10 billion, it's likely epoch seconds (e.g. 1726000000)
                    if (num in 1..9999999999L) num * 1000L else num
                }
                is String -> {
                    val str = raw.trim()
                    // Check if numeric string
                    str.toLongOrNull()?.let { num ->
                        return if (num in 1..9999999999L) num * 1000L else num
                    }
                    // Try date format parsing
                    for (format in supportedFormats) {
                        try {
                            val parsed = format.parse(str)
                            if (parsed != null) return parsed.time
                        } catch (_: Exception) {
                        }
                    }
                    0L
                }
                else -> {
                    // Reflection fallback for Timestamp object if classloader differs
                    val className = raw.javaClass.name
                    if (className.contains("Timestamp")) {
                        val method = raw.javaClass.getMethod("toDate")
                        val date = method.invoke(raw) as? Date
                        date?.time ?: 0L
                    } else {
                        0L
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse timestamp from: $raw", e)
            0L
        }
    }
}
