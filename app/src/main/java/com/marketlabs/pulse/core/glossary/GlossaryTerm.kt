package com.marketlabs.pulse.core.glossary

/**
 * A single `term`/`definition` pair -- the shape every flat, list-rendered glossary in this app
 * (Market/Risk/Stock Analysis/Dashboard) uses, as opposed to `MetricGlossaryEntry`'s richer
 * what-it-is/how-to-read/bands/gotchas shape for the Indicators/Positioning/Posture per-metric
 * detail page. Shared by all 4 of these glossaries, which are bundled JSON, like
 * `MetricGlossaryEntry`.
 */
data class GlossaryTerm(
    val term: String,
    val definition: String
)

/** `{"KEY": "definition", ...}` (JSON object, order-preserving) -> the `List<GlossaryTerm>` every existing glossary sheet already iterates. */
internal fun Map<String, String>.toGlossaryTerms(): List<GlossaryTerm> = map { (term, definition) -> GlossaryTerm(term, definition) }
