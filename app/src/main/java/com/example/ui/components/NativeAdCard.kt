package com.example.ui.components

import android.content.Context
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdManager
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val nativeAd by AdManager.currentNativeAd.collectAsState()

    // Strict user rule: If no ad is loaded, do NOT show any placeholder or space.
    // The space collapses completely so posts move up cleanly.
    if (nativeAd == null) {
        return
    }

    val ad = nativeAd ?: return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141728))
            .border(1.dp, Color(0xFF242A44), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag("native_ad_card")
    ) {
        AndroidView(
            factory = { ctx ->
                createNativeAdView(ctx, ad)
            },
            update = { view ->
                populateNativeAdView(view, ad)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun createNativeAdView(context: Context, nativeAd: NativeAd): NativeAdView {
    val adView = NativeAdView(context)
    val layout = android.widget.LinearLayout(context).apply {
        orientation = android.widget.LinearLayout.VERTICAL
        layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    // Top Bar (Ad badge + Headline)
    val headerLayout = android.widget.LinearLayout(context).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL
        gravity = android.view.Gravity.CENTER_VERTICAL
    }

    val adBadge = TextView(context).apply {
        text = "Ad"
        setTextColor(android.graphics.Color.WHITE)
        textSize = 10f
        setBackgroundColor(android.graphics.Color.parseColor("#7C3AED"))
        setPadding(12, 4, 12, 4)
    }
    headerLayout.addView(adBadge)

    val headlineView = TextView(context).apply {
        setTextColor(android.graphics.Color.WHITE)
        textSize = 14f
        setTypeface(null, android.graphics.Typeface.BOLD)
        setPadding(16, 0, 0, 0)
    }
    adView.headlineView = headlineView
    headerLayout.addView(headlineView)
    layout.addView(headerLayout)

    // Body text
    val bodyView = TextView(context).apply {
        setTextColor(android.graphics.Color.parseColor("#94A3B8"))
        textSize = 12f
        setPadding(0, 8, 0, 8)
        maxLines = 2
    }
    adView.bodyView = bodyView
    layout.addView(bodyView)

    // Call to action button
    val ctaButton = Button(context).apply {
        setTextColor(android.graphics.Color.WHITE)
        textSize = 12f
        setBackgroundColor(android.graphics.Color.parseColor("#7C3AED"))
    }
    adView.callToActionView = ctaButton
    layout.addView(ctaButton)

    adView.addView(layout)
    populateNativeAdView(adView, nativeAd)
    return adView
}

private fun populateNativeAdView(adView: NativeAdView, nativeAd: NativeAd) {
    (adView.headlineView as? TextView)?.text = nativeAd.headline ?: "Sponsored"
    (adView.bodyView as? TextView)?.text = nativeAd.body ?: ""
    (adView.callToActionView as? Button)?.text = nativeAd.callToAction ?: "Learn More"
    adView.setNativeAd(nativeAd)
}
