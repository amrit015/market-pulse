package com.marketlabs.pulse.ui.screens.insights.glossary

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.marketlabs.pulse.R
import com.marketlabs.pulse.core.glossary.MetricGlossaryProvider
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_DESCRIPTION
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_METRIC_IDS
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_STATUS
import com.marketlabs.pulse.ui.screens.insights.glossary.GlossaryDetailViewModel.Companion.ARG_TITLE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Drives the pushed `glossaryDetail/{title}/{metricIds}/{description}/{status}` destination for a
 * whole Positioning/Posture card (2026-08-27 convergence pass). `metricIds` is comma-joined (every
 * id in `core/glossary/` is plain lowercase/dot/underscore, so no delimiter collision risk, unlike
 * `title`/`description`/`status` which carry spaces/parens and are `Uri.encode()`-d by the caller
 * -- read back here with no manual decode, since Navigation's own path-segment matching already
 * decodes those; see the earlier single-value version of this class for exactly why a second,
 * mismatched manual decode pass crashed).
 *
 * Still no repository stream or loading state -- every glossary entry is a synchronous in-memory
 * lookup, same reasoning as the single-value version this replaces.
 */
@HiltViewModel
class GlossaryDetailViewModel @Inject constructor(
    @ApplicationContext context: Context,
    glossaryProvider: MetricGlossaryProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState: GlossaryDetailUiState

    init {
        val title: String = checkNotNull(savedStateHandle[ARG_TITLE]) {
            "GlossaryDetailViewModel requires a non-null \"$ARG_TITLE\" nav argument"
        }
        val metricIdsRaw: String = checkNotNull(savedStateHandle[ARG_METRIC_IDS]) {
            "GlossaryDetailViewModel requires a non-null \"$ARG_METRIC_IDS\" nav argument"
        }
        val metricIds: List<String> = metricIdsRaw.split(",")
        val description: String? = (savedStateHandle.get<String>(ARG_DESCRIPTION)).takeUnless { it.isNullOrBlank() }
        val status: String? = (savedStateHandle.get<String>(ARG_STATUS)).takeUnless { it.isNullOrBlank() }

        val sections = metricIds.mapNotNull { metricId ->
            val entry = glossaryProvider.get(metricId) ?: return@mapNotNull null
            GlossarySection(
                label = context.getString(labelResFor(metricId)),
                whatItIs = entry.whatItIs,
                howToRead = entry.howToRead,
                gotchas = entry.gotchas
            )
        }

        // 💡 Bands are collected across ALL of this card's glossary entries, not just one -- see
        // GlossaryDetailUiState's doc comment for why (a short-interest instrument's one overall
        // status is computed from two different fields' worth of thresholds).
        val mergedBands = metricIds
            .mapNotNull { glossaryProvider.get(it) }
            .flatMap { it.bands }
            .distinctBy { it.label }

        val currentBandIndex = status?.let { s ->
            mergedBands.indexOfFirst { it.label.equals(s.trim(), ignoreCase = true) }.takeIf { it >= 0 }
        }

        uiState = GlossaryDetailUiState(
            title = title,
            description = description,
            sections = sections,
            mergedBands = mergedBands,
            currentBandIndex = currentBandIndex
        )
    }

    companion object {
        /** Must match the nav argument names in the `glossaryDetail/{title}/{metricIds}/{description}/{status}` route. */
        const val ARG_TITLE = "title"
        const val ARG_METRIC_IDS = "metricIds"
        const val ARG_DESCRIPTION = "description"
        const val ARG_STATUS = "status"

        /**
         * Static metric-id -> display-label mapping for a merged card's sub-section headers.
         * Duplicated from each card's own title string rather than passed as a further nav
         * argument -- these 9 ids are a fixed, closed set (the same 9 `core/glossary/` entries the
         * 2026-08-27 interpretive-layer spec added), so a compile-time mapping here is simpler
         * than round-tripping more `Uri.encode()`-d strings through the nav route.
         */
        private fun labelResFor(metricId: String): Int = when (metricId) {
            "posture.naaim_exposure" -> R.string.posture_naaim_title
            "posture.dark_pool_index" -> R.string.posture_dix_title
            "posture.net_liquidity" -> R.string.posture_net_liquidity_title
            "positioning.aaii_bull_bear_spread" -> R.string.positioning_retail_sentiment_title
            "positioning.cot_nc_net_pct_oi" -> R.string.positioning_futures_caption
            "positioning.cot_percentile" -> R.string.positioning_cot_percentile_title
            "positioning.short_interest_days_to_cover" -> R.string.positioning_days_to_cover_title
            "positioning.short_interest_shares" -> R.string.positioning_short_shares_title
            "positioning.short_interest_mom_change" -> R.string.positioning_mom_change_title
            else -> R.string.glossary_entry_unavailable
        }
    }
}
