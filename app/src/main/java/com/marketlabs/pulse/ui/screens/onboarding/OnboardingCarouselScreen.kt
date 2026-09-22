package com.marketlabs.pulse.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlinx.coroutines.launch

private const val OnboardingSlideCount = 3

/**
 * 3 lightweight slides, no legal weight of their own —
 * the full disclosure + acceptance gate is the separate screen this leads into. No Skip -- every
 * slide is a forward step (label switches to "Continue" on the last one), so the user always sees
 * all three before reaching the acceptance gate.
 */
@Composable
fun OnboardingCarouselScreen(
    onContinue: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingSlideCount })
    val coroutineScope = rememberCoroutineScope()
    val isLastSlide = pagerState.currentPage == OnboardingSlideCount - 1

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingSlideContent(page = page)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_large)),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(OnboardingSlideCount) { index ->
                    PageIndicatorDot(isSelected = index == pagerState.currentPage)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_xxlarge))
                    .padding(bottom = dimensionResource(id = R.dimen.padding_xxlarge)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isLastSlide) {
                            onContinue()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                ) {
                    Text(
                        text = stringResource(
                            id = if (isLastSlide) R.string.onboarding_continue else R.string.onboarding_next
                        )
                    )
                }
            }
        }
    }
}

/**
 * One illustration per slide
 * (`slide_1/2/3_dark/light`, drawable-xxxhdpi) -- picks its light/dark variant off the selected
 * theme preset's own identity (`LocalPulseColors.current.isDark`), not OS dark mode, matching how
 * every preset-driven color in this app already resolves. Slide 1 = "signal from noise," Slide 2 =
 * the computed-data-vs-AI split (carries the "AI-generated · Not advice" tag inside the art
 * itself), Slide 3 = the document/shield motif -- same order as `titleRes`/`bodyRes` below.
 */
@Composable
private fun OnboardingSlideContent(page: Int, modifier: Modifier = Modifier) {
    val isDark = LocalPulseColors.current.isDark
    val (titleRes, bodyRes, illustrationRes) = when (page) {
        0 -> Triple(
            R.string.onboarding_slide1_title,
            R.string.onboarding_slide1_body,
            if (isDark) R.drawable.slide_1_dark else R.drawable.slide_1_light
        )
        1 -> Triple(
            R.string.onboarding_slide2_title,
            R.string.onboarding_slide2_body,
            if (isDark) R.drawable.slide_2_dark else R.drawable.slide_2_light
        )
        else -> Triple(
            R.string.onboarding_slide3_title,
            R.string.onboarding_slide3_body,
            if (isDark) R.drawable.slide_3_dark else R.drawable.slide_3_light
        )
    }
    // 💡 The whole slide -- illustration plus text -- is one card (this app's card system), centered
    // on the screen. `aspectRatio` matches the source art's own 1170x900 ratio exactly, so
    // `ContentScale.Fit` has nothing to letterbox and the image fills the card's full width; the
    // card's own rounded shape clips the art's top corners. The column scrolls only if a slide ever
    // outgrows a very short screen.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimensionResource(id = R.dimen.padding_xxlarge))
        ) {
            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = illustrationRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1170f / 900f)
                    )
                    Column(
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_extra_large)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = titleRes),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = bodyRes),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = LocalPulseColors.current.onSurfaceMuted,
                            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageIndicatorDot(isSelected: Boolean) {
    val color = if (isSelected) LocalPulseColors.current.accentPrimary else LocalPulseColors.current.onSurfaceMuted
    Box(
        modifier = Modifier
            .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
            .size(if (isSelected) 8.dp else 6.dp)
            .background(color = color, shape = CircleShape)
    )
}

// ============================================================================
// 🎨 PREVIEWS
// ============================================================================

@Preview(name = "Light", showBackground = true)
@Composable
private fun PreviewOnboardingCarouselScreenLight() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        OnboardingCarouselScreen(onContinue = {})
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun PreviewOnboardingCarouselScreenDark() {
    MarketPulseTheme(theme = MarketPulseTheme.LILAC) {
        OnboardingCarouselScreen(onContinue = {})
    }
}
