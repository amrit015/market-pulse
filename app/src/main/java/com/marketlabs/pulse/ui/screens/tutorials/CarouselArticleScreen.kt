package com.marketlabs.pulse.ui.screens.tutorials

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.marketlabs.pulse.R
import com.marketlabs.pulse.ui.components.PulseBackTitleRow
import com.marketlabs.pulse.ui.components.tutorials.CenteredCardCarousel
import com.marketlabs.pulse.ui.components.tutorials.DeckPage

/** A Tutorials article shown as the shared centered, equal-height card carousel under a back row. */
@Composable
fun CarouselArticleScreen(
    title: String,
    pages: List<DeckPage>,
    onNavigateUp: () -> Unit,
    onNavigateToRoute: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Bottom))
    ) {
        PulseBackTitleRow(title = title, onNavigateUp = onNavigateUp)
        CenteredCardCarousel(pages = pages, modifier = Modifier.weight(1f), enlargedText = true, onNavigateToRoute = onNavigateToRoute)
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_extra_large)))
    }
}
