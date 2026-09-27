package com.marketlabs.pulse.ui.screens.onboarding

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseCard
import com.marketlabs.pulse.ui.components.PulseCardStyle
import com.marketlabs.pulse.ui.theme.LocalPulseColors
import com.marketlabs.pulse.ui.theme.MarketPulseTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val OnboardingSlideCount = 3

// A slide one full page away from center is drawn at this scale/alpha; in between, both follow
// the drag linearly.
private const val SlideMinScale = 0.9f
private const val SlideMinAlpha = 0.5f

// How far the illustration lags behind its card, as a fraction of the card's width, when the slide
// is one full page away from center.
private const val IllustrationParallaxShift = 0.08f

// Typing speed for the slide body, capped in total so the longest slide's paragraph still finishes
// in under two and a half seconds.
private const val BodyTypingMsPerChar = 8
private const val BodyTypingMaxMs = 2400
private const val ButtonLabelFadeMs = 200

/**
 * 3 lightweight slides, no legal weight of their own —
 * the full disclosure + acceptance gate is the separate screen this leads into. No Skip -- every
 * slide is a forward step (label switches to "Continue" on the last one), so the user always sees
 * all three before reaching the acceptance gate.
 *
 * `isObscured` is true while something is drawn over this screen (the app's launch splash), so the
 * first slide's text doesn't type out unseen underneath it.
 */
@Composable
fun OnboardingCarouselScreen(
    onContinue: () -> Unit,
    isObscured: Boolean = false
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingSlideCount })
    val coroutineScope = rememberCoroutineScope()
    val isLastSlide = pagerState.currentPage == OnboardingSlideCount - 1
    // Slides whose text has already started typing. Kept here rather than in each slide because
    // the pager drops a slide once it's off screen, which would forget that it already typed.
    var typedPages by rememberSaveable { mutableStateOf(emptyList<Int>()) }

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
                OnboardingSlideContent(
                    page = page,
                    // Signed distance of this page from the center of the viewport, in pages: 0 when
                    // fully in view, +1 once it has slid a full page off to the left, -1 to the right.
                    // Passed as a lambda so it's only read inside `graphicsLayer`, which redraws every
                    // drag frame without recomposing the slide.
                    pageOffset = {
                        ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceIn(-1f, 1f)
                    },
                    canStartTyping = pagerState.settledPage == page && !isObscured,
                    alreadyTyped = page in typedPages,
                    onTypingStarted = { if (page !in typedPages) typedPages = typedPages + page }
                )
            }

            PageIndicator(
                position = pagerState.currentPage + pagerState.currentPageOffsetFraction,
                modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(id = R.dimen.padding_large))
            )

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
                    // Cross-fades "Next" into "Continue" on the last slide, and `SizeTransform` lets the
                    // button's width ease to the new label's width instead of jumping.
                    AnimatedContent(
                        targetState = isLastSlide,
                        transitionSpec = {
                            fadeIn(tween(ButtonLabelFadeMs)) togetherWith fadeOut(tween(ButtonLabelFadeMs)) using
                                SizeTransform(clip = false)
                        },
                        label = "onboarding_button_label"
                    ) { lastSlide ->
                        Text(
                            text = stringResource(
                                id = if (lastSlide) R.string.onboarding_continue else R.string.onboarding_next
                            )
                        )
                    }
                }
            }
        }
    }
}

private val OnboardingSlideText = listOf(
    R.string.onboarding_slide1_title to R.string.onboarding_slide1_body,
    R.string.onboarding_slide2_title to R.string.onboarding_slide2_body,
    R.string.onboarding_slide3_title to R.string.onboarding_slide3_body
)

/**
 * One illustration per slide
 * (`slide_1/2/3_dark/light`, drawable-xxxhdpi) -- picks its light/dark variant off the selected
 * theme preset's own identity (`LocalPulseColors.current.isDark`), not OS dark mode, matching how
 * every preset-driven color in this app already resolves. Slide 1 = "signal from noise," Slide 2 =
 * the computed-data-vs-AI split (carries the "AI-generated · Not advice" tag inside the art
 * itself), Slide 3 = the document/shield motif -- same order as `OnboardingSlideText`.
 */
@Composable
private fun OnboardingSlideContent(
    page: Int,
    pageOffset: () -> Float,
    canStartTyping: Boolean,
    alreadyTyped: Boolean,
    onTypingStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalPulseColors.current.isDark
    val illustrationRes = when (page) {
        0 -> if (isDark) R.drawable.slide_1_dark else R.drawable.slide_1_light
        1 -> if (isDark) R.drawable.slide_2_dark else R.drawable.slide_2_light
        else -> if (isDark) R.drawable.slide_3_dark else R.drawable.slide_3_light
    }
    // 💡 The whole slide -- illustration plus text -- is one card (this app's card system), centered
    // on the screen. `aspectRatio` matches the source art's own 1170x900 ratio exactly, so
    // `ContentScale.Fit` has nothing to letterbox and the image fills the card's full width; the
    // card's own rounded shape clips the art's top corners. The column scrolls only if a slide ever
    // outgrows a very short screen. `verticalScroll` clips its content at its top and bottom edges,
    // so the padding sits inside it on every side -- without the vertical part, the card's drop
    // shadow (much larger in light mode) gets cut off flat above and below the card.
    //
    // 💡 A slide shrinks and fades as it's dragged away from center, and grows back as it arrives,
    // following the finger so a half-finished swipe shows a half-finished transition.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .graphicsLayer {
                    val distance = abs(pageOffset())
                    val scale = lerp(1f, SlideMinScale, distance)
                    scaleX = scale
                    scaleY = scale
                    alpha = lerp(1f, SlideMinAlpha, distance)
                }
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = dimensionResource(id = R.dimen.padding_xxlarge),
                    vertical = dimensionResource(id = R.dimen.padding_xxlarge)
                )
        ) {
            PulseCard(style = PulseCardStyle.DATA, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    // 💡 Parallax: the art slides a little slower than its card, so it lags behind in the
                    // direction of the swipe. It zooms in by exactly enough to cover the gap that shift
                    // would open at the card's edge, and both return to zero at rest, so the settled
                    // slide shows the full, uncropped art.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1170f / 900f)
                            .clipToBounds()
                    ) {
                        Image(
                            painter = painterResource(id = illustrationRes),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    val offset = pageOffset()
                                    val zoom = 1f + 2f * IllustrationParallaxShift * abs(offset)
                                    scaleX = zoom
                                    scaleY = zoom
                                    translationX = offset * IllustrationParallaxShift * size.width
                                }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(
                                start = dimensionResource(id = R.dimen.padding_extra_large),
                                end = dimensionResource(id = R.dimen.padding_extra_large),
                            )
                            .padding(bottom = dimensionResource(id = R.dimen.padding_xxlarge)),
                        contentAlignment = Alignment.Center
                    ) {
                        OnboardingSlideText.forEachIndexed { index, (titleRes, bodyRes) ->
                            if (index == page) {
                                OnboardingSlideTextBlock(
                                    titleRes = titleRes,
                                    bodyRes = bodyRes,
                                    canStartTyping = canStartTyping,
                                    alreadyTyped = alreadyTyped,
                                    onTypingStarted = onTypingStarted
                                )
                            } else {
                                OnboardingSlideTextBlock(
                                    titleRes = titleRes,
                                    bodyRes = bodyRes,
                                    canStartTyping = false,
                                    alreadyTyped = true,
                                    onTypingStarted = {},
                                    modifier = Modifier.alpha(0f).clearAndSetSemantics {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The title is shown as-is; the first time its slide settles in the center, the body types out
 * beneath it. Only the first visit types: once typing has started the slide is reported through
 * `onTypingStarted`, and on any later visit (`alreadyTyped`) the body is simply there in full. A
 * swipe that pauses the typing partway resumes it from where it stopped if the slide settles again
 * before leaving.
 */
@Composable
private fun OnboardingSlideTextBlock(
    @StringRes titleRes: Int,
    @StringRes bodyRes: Int,
    canStartTyping: Boolean,
    alreadyTyped: Boolean,
    onTypingStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = stringResource(id = titleRes)
    val body = stringResource(id = bodyRes)
    // Captured once: `alreadyTyped` flips to true as soon as typing starts, and that mustn't skip
    // the typing already underway.
    val typedOnEntry = remember { alreadyTyped }
    val bodyChars = remember { Animatable(if (typedOnEntry) body.length.toFloat() else 0f) }
    LaunchedEffect(canStartTyping) {
        if (canStartTyping && !typedOnEntry) {
            onTypingStarted()
            bodyChars.animateTo(
                targetValue = body.length.toFloat(),
                animationSpec = tween(
                    durationMillis = (body.length * BodyTypingMsPerChar).coerceAtMost(BodyTypingMaxMs),
                    easing = LinearEasing
                )
            )
        }
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = typedText(body, bodyChars.value.toInt()),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = LocalPulseColors.current.onSurfaceMuted,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen.padding_large))
        )
    }
}

/**
 * The whole string, with everything past `visibleChars` drawn transparent rather than left out, so
 * the text is laid out at its final size and line breaks from the first character: nothing
 * re-wraps or re-centers as it types, and screen readers still get the full text.
 */
private fun typedText(full: String, visibleChars: Int) = buildAnnotatedString {
    append(full.take(visibleChars))
    withStyle(SpanStyle(color = Color.Transparent)) { append(full.drop(visibleChars)) }
}

/**
 * `position` is the pager's current page plus its drag fraction (1.4 = 40% of the way from page 1
 * to page 2). Each dot's selection is how close `position` is to its own index, so mid-swipe the
 * current dot's pill shrinks and loses its accent color while the next one grows and gains it,
 * both tracking the finger.
 */
@Composable
private fun PageIndicator(position: Float, modifier: Modifier = Modifier) {
    val dotSize = dimensionResource(id = R.dimen.onboarding_dot_size)
    val activeWidth = dimensionResource(id = R.dimen.onboarding_dot_active_width)
    val inactiveColor = LocalPulseColors.current.onSurfaceMuted
    val activeColor = LocalPulseColors.current.accentPrimary
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        repeat(OnboardingSlideCount) { index ->
            val selection = (1f - abs(position - index)).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                    .height(dotSize)
                    .width(lerp(dotSize, activeWidth, selection))
                    .background(color = lerp(inactiveColor, activeColor, selection), shape = CircleShape)
            )
        }
    }
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

@Preview(name = "Indicator mid-swipe", showBackground = true)
@Composable
private fun PreviewPageIndicatorMidSwipe() {
    MarketPulseTheme(theme = MarketPulseTheme.NAVY) {
        PageIndicator(position = 0.5f)
    }
}
