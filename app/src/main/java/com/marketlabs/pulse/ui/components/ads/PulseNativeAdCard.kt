package com.marketlabs.pulse.ui.components.ads

import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.components.widgets.SignalPill
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme

/**
 * High-fidelity pure-Compose placeholder for Native AdMob cards.
 *
 * Supports both compact (text + logo) and rich media (landscape creative image banner) variants.
 * Applies a distinct stroke border (white on dark mode, darkish-grey on light mode) to visually
 * differentiate sponsored promotions from organic market intelligence.
 */
@Composable
fun PulseNativeAdPlaceholder(
    modifier: Modifier = Modifier,
    advertiser: String = stringResource(id = R.string.ad_fake_advertiser),
    headline: String = stringResource(id = R.string.ad_fake_headline),
    body: String = stringResource(id = R.string.ad_fake_body),
    callToAction: String = stringResource(id = R.string.ad_fake_cta),
    showCreativeImage: Boolean = false,
    creativeImageRes: Int = R.drawable.vanguard_logo,
    logoRes: Int = R.drawable.vanguard_logo,
    creativeTag: String? = null,
    onClick: () -> Unit = {}
) {
    val pulseColors = LocalPulseColors.current
    val paddingLarge = dimensionResource(id = R.dimen.padding_large)
    val paddingMedium = dimensionResource(id = R.dimen.padding_medium)
    val cardShape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_card))
    val isLightMode = MaterialTheme.colorScheme.background.luminance() > 0.5f

    // Deliberate contrast stroke: white on dark mode, darkish-grey on light mode
    val strokeColor = if (isLightMode) {
        Color(0xFF333338).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.45f)
    }

    PulseCard(
        style = PulseCardStyle.DATA,
        shape = cardShape,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = dimensionResource(id = R.dimen.border_thin),
                color = strokeColor,
                shape = cardShape
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingLarge)
        ) {
            // Row 1: Brand Icon + Advertiser Tag & SPONSORED pill badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Small brand logo badge with vanguard_logo or advertiser icon
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip)),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            dimensionResource(id = R.dimen.border_thin),
                            pulseColors.accentSurfaceBorder.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.size(dimensionResource(id = R.dimen.ad_logo_size))
                    ) {
                        Image(
                            painter = painterResource(id = logoRes),
                            contentDescription = advertiser,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.padding_medium)))

                    Text(
                        text = advertiser.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = pulseColors.accentPrimary
                    )
                }

                // Styled with accentSurface background and accentPrimary content to stay clean and compliant
                SignalPill(
                    text = stringResource(id = R.string.ad_sponsored_label),
                    pillColor = pulseColors.accentSurface,
                    contentColor = pulseColors.accentPrimary
                )
            }

            Spacer(modifier = Modifier.height(paddingMedium))

            // Row 2: Headline
            Text(
                text = headline,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            // Optional Creative Image / Media Banner
            if (showCreativeImage) {
                Spacer(modifier = Modifier.height(paddingMedium))
                CreativeImagePlaceholder(
                    imageRes = creativeImageRes,
                    tag = creativeTag,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Hairline divider
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = paddingMedium),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                thickness = dimensionResource(id = R.dimen.border_thin)
            )

            // Row 3: Body copy
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            // Row 4: Call-to-action button
            if (callToAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small)),
                        color = pulseColors.accentPrimary,
                        modifier = Modifier.clickable(onClick = onClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(id = R.dimen.padding_large),
                                vertical = dimensionResource(id = R.dimen.padding_small)
                            )
                        ) {
                            Text(
                                text = callToAction,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = pulseColors.accentOn
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual presentation of a financial creative image banner.
 * Used in placeholders and previews to evaluate media presence within the app layout.
 */
@Composable
private fun CreativeImagePlaceholder(
    modifier: Modifier = Modifier,
    imageRes: Int = R.drawable.vanguard_logo,
    tag: String? = null
) {
    val pulseColors = LocalPulseColors.current
    val imageShape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_small))
    val isLightMode = MaterialTheme.colorScheme.background.luminance() > 0.5f

    Box(
        modifier = modifier
            .height(dimensionResource(id = R.dimen.ad_creative_image_height))
            .fillMaxWidth()
            .clip(imageShape)
            .background(if (isLightMode) Color(0xFFF7F6F2) else Color(0xFF0F1014))
            .border(
                dimensionResource(id = R.dimen.border_thin),
                pulseColors.accentSurfaceBorder.copy(alpha = 0.4f),
                imageShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = stringResource(id = R.string.ad_fake_headline),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_small))
        )

        tag?.let { tagText ->
            Surface(
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_chip)),
                color = pulseColors.accentPrimary.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(dimensionResource(id = R.dimen.padding_medium))
            ) {
                Text(
                    text = tagText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = pulseColors.accentOn,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(id = R.dimen.padding_medium),
                        vertical = dimensionResource(id = R.dimen.padding_tiny)
                    )
                )
            }
        }
    }
}

/**
 * Composable wrapper for Google AdMob [NativeAd].
 *
 * Dynamically binds the ad view hierarchy into AdMob's [NativeAdView] to register impressions,
 * clicks, and Google AdChoices. Includes a [MediaView] for creative images/video.
 */
@Composable
fun PulseNativeAdCard(
    nativeAd: NativeAd?,
    modifier: Modifier = Modifier,
    showPlaceholderIfNull: Boolean = true,
    showCreativeImageInPlaceholder: Boolean = false,
    placeholderAdvertiser: String? = null,
    placeholderHeadline: String? = null,
    placeholderBody: String? = null,
    placeholderCta: String? = null,
    placeholderTag: String? = null,
    placeholderCreativeImageRes: Int = R.drawable.vanguard_logo,
    placeholderLogoRes: Int = R.drawable.vanguard_logo
) {
    if (nativeAd == null) {
        if (showPlaceholderIfNull) {
            PulseNativeAdPlaceholder(
                modifier = modifier,
                advertiser = placeholderAdvertiser ?: stringResource(id = R.string.ad_fake_advertiser),
                headline = placeholderHeadline ?: stringResource(id = R.string.ad_fake_headline),
                body = placeholderBody ?: stringResource(id = R.string.ad_fake_body),
                callToAction = placeholderCta ?: stringResource(id = R.string.ad_fake_cta),
                showCreativeImage = showCreativeImageInPlaceholder,
                creativeTag = placeholderTag,
                creativeImageRes = placeholderCreativeImageRes,
                logoRes = placeholderLogoRes
            )
        }
        return
    }

    val pulseColors = LocalPulseColors.current
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val accentPrimaryColor = pulseColors.accentPrimary
    val accentOnColor = pulseColors.accentOn
    val context = LocalContext.current

    val isLightMode = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val strokeColor = if (isLightMode) {
        Color(0xFF333338).copy(alpha = 0.35f)
    } else {
        Color.White.copy(alpha = 0.45f)
    }

    val defaultCtaText = stringResource(id = R.string.ad_learn_more)
    val sponsoredLabel = stringResource(id = R.string.ad_sponsored_label)

    val paddingPx = (16 * context.resources.displayMetrics.density).toInt()
    val paddingSmallPx = (8 * context.resources.displayMetrics.density).toInt()
    val radiusPx = (12 * context.resources.displayMetrics.density)
    val mediaHeightPx = (140 * context.resources.displayMetrics.density).toInt()

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            val nativeAdView = NativeAdView(ctx)

            // Outer container with contrast stroke border
            val container = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                val shapeDrawable = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = radiusPx
                    setColor(surfaceColor.toArgb())
                    setStroke((1 * ctx.resources.displayMetrics.density).toInt(), strokeColor.toArgb())
                }
                background = shapeDrawable
            }

            // Header row (Advertiser + Sponsored badge)
            val headerRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            val iconIv = ImageView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (20 * ctx.resources.displayMetrics.density).toInt(),
                    (20 * ctx.resources.displayMetrics.density).toInt()
                ).apply {
                    setMargins(0, 0, paddingSmallPx, 0)
                }
                visibility = View.GONE
            }
            nativeAdView.iconView = iconIv
            headerRow.addView(iconIv)

            val advertiserTv = TextView(ctx).apply {
                textSize = 12f
                setTextColor(accentPrimaryColor.toArgb())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            nativeAdView.advertiserView = advertiserTv

            val sponsoredTv = TextView(ctx).apply {
                text = sponsoredLabel
                textSize = 10f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(accentPrimaryColor.toArgb())
                setPadding(paddingSmallPx, paddingSmallPx / 2, paddingSmallPx, paddingSmallPx / 2)
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = (4 * ctx.resources.displayMetrics.density)
                    setColor(pulseColors.accentSurface.toArgb())
                }
            }

            headerRow.addView(advertiserTv)
            headerRow.addView(sponsoredTv)
            container.addView(headerRow)

            // Headline
            val headlineTv = TextView(ctx).apply {
                textSize = 15f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(onSurfaceColor.toArgb())
                setPadding(0, paddingSmallPx, 0, paddingSmallPx)
            }
            nativeAdView.headlineView = headlineTv
            container.addView(headlineTv)

            // Creative Image / MediaView
            val mediaView = MediaView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    mediaHeightPx
                ).apply {
                    setMargins(0, paddingSmallPx, 0, paddingSmallPx)
                }
                visibility = View.GONE
            }
            nativeAdView.mediaView = mediaView
            container.addView(mediaView)

            // Hairline divider
            val divider = View(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (1 * ctx.resources.displayMetrics.density).toInt()
                ).apply {
                    setMargins(0, paddingSmallPx, 0, paddingSmallPx)
                }
                setBackgroundColor(onSurfaceColor.copy(alpha = 0.1f).toArgb())
            }
            container.addView(divider)

            // Body
            val bodyTv = TextView(ctx).apply {
                textSize = 13f
                setTextColor(onSurfaceVariantColor.toArgb())
            }
            nativeAdView.bodyView = bodyTv
            container.addView(bodyTv)

            // Call to action button
            val ctaButton = Button(ctx).apply {
                textSize = 12f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(accentOnColor.toArgb())
                background = android.graphics.drawable.GradientDrawable().apply {
                    cornerRadius = (6 * ctx.resources.displayMetrics.density)
                    setColor(accentPrimaryColor.toArgb())
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.END
                    topMargin = paddingSmallPx
                }
            }
            nativeAdView.callToActionView = ctaButton
            container.addView(ctaButton)

            nativeAdView.addView(container)
            nativeAdView
        },
        update = { adView ->
            // Bind NativeAd content to view assets
            (adView.headlineView as? TextView)?.text = nativeAd.headline
            (adView.bodyView as? TextView)?.apply {
                text = nativeAd.body
                visibility = if (nativeAd.body.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            (adView.advertiserView as? TextView)?.apply {
                text = nativeAd.advertiser?.uppercase() ?: ""
                visibility = if (nativeAd.advertiser.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            (adView.callToActionView as? Button)?.apply {
                text = nativeAd.callToAction ?: defaultCtaText
                visibility = if (nativeAd.callToAction.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            // Bind Icon if present
            val iconIv = adView.iconView as? ImageView
            if (nativeAd.icon?.drawable != null) {
                iconIv?.setImageDrawable(nativeAd.icon?.drawable)
                iconIv?.visibility = View.VISIBLE
            } else {
                iconIv?.visibility = View.GONE
            }

            // Bind MediaView if image or video is present
            val mediaView = adView.mediaView
            if (nativeAd.mediaContent != null) {
                mediaView?.setMediaContent(nativeAd.mediaContent)
                mediaView?.visibility = View.VISIBLE
            } else {
                mediaView?.visibility = View.GONE
            }

            adView.setNativeAd(nativeAd)
        }
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "1. Dark (Lilac) - Compact Text & Vanguard Logo", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPulseNativeAdDarkCompact() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Box(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            PulseNativeAdPlaceholder(
                showCreativeImage = false,
                logoRes = R.drawable.vanguard_logo,
                advertiser = "Vanguard Institutional"
            )
        }
    }
}

@Preview(name = "2. Dark (Lilac) - With Vanguard Creative Image Banner", showBackground = true, backgroundColor = 0xFF0D0E12)
@Composable
private fun PreviewPulseNativeAdDarkWithImage() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        Box(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            PulseNativeAdPlaceholder(
                showCreativeImage = true,
                creativeImageRes = R.drawable.vanguard_logo,
                logoRes = R.drawable.vanguard_logo,
                creativeTag = "ETF ALLOCATION STRATEGY",
                advertiser = "Vanguard Institutional",
                headline = "Commission-Free US Equity & Bond Portfolio ETFs",
                body = "Explore index-tracking ETFs built for quantitative allocators. Real-time liquidity, ultra-low expense ratios, and institutional trade execution.",
                callToAction = "Explore Funds"
            )
        }
    }
}

@Preview(name = "3. Light (Plum) - With Vanguard Creative Image Banner", showBackground = true, backgroundColor = 0xFFF5F3EF)
@Composable
private fun PreviewPulseNativeAdLightWithImage() {
    MarketPulseTheme(theme = MarketPulseTheme.PLUM) {
        Box(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            PulseNativeAdPlaceholder(
                showCreativeImage = true,
                creativeImageRes = R.drawable.vanguard_logo,
                logoRes = R.drawable.vanguard_logo,
                creativeTag = "ETF ALLOCATION STRATEGY",
                advertiser = "Vanguard Institutional",
                headline = "Commission-Free US Equity & Bond Portfolio ETFs",
                body = "Explore index-tracking ETFs built for quantitative allocators. Real-time liquidity, ultra-low expense ratios, and institutional trade execution.",
                callToAction = "Explore Funds"
            )
        }
    }
}

@Preview(name = "4. Light (Navy) - Compact Text & Vanguard Logo", showBackground = true, backgroundColor = 0xFFF0F4F8)
@Composable
private fun PreviewPulseNativeAdNavyCompact() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        Box(modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_large))) {
            PulseNativeAdPlaceholder(
                showCreativeImage = false,
                logoRes = R.drawable.vanguard_logo,
                advertiser = "Vanguard",
                headline = "Automated Index Portfolios & Core Asset Screener",
                body = "Access low-cost index funds and broad-market ETF liquidity directly on mobile.",
                callToAction = "Learn More"
            )
        }
    }
}
