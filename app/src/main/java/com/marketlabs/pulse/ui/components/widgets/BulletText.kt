package com.marketlabs.pulse.ui.components.widgets

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

/**
 * Joins 2+ text parts with this app's shared bullet separator (`R.string.bullet_separator`),
 * rendered a size up from [baseFontSize] -- a plain-size bullet sitting between two runs of body
 * text reads as a low, easy-to-miss dot rather than a clear break. Hardcoding a literal "•" in
 * each `"$a • $b"`-style join would let each one drift independently in size; this is the one
 * place that glyph is joined in.
 *
 * Plain (non-`@Composable`) so callers resolve `bullet` (`stringResource(R.string.bullet_separator)`)
 * and `baseFontSize` (the `TextStyle.fontSize` the result will be rendered with) themselves and pass
 * them in, rather than this reaching into `stringResource`/`MaterialTheme` on its own -- keeps it
 * usable from a plain (non-composable) formatting helper too, as long as the composable call site
 * does the resolving.
 */
fun buildBulletJoinedText(parts: List<String?>, bullet: String, baseFontSize: TextUnit): AnnotatedString {
    val nonBlankParts = parts.filterNot { it.isNullOrBlank() }

    return buildAnnotatedString {
        nonBlankParts.forEachIndexed { index, part ->
            append(part)
            if (index != nonBlankParts.lastIndex) {
                append(" ")
                withStyle(SpanStyle(fontSize = baseFontSize)) {
                    append(bullet)
                }
                append(" ")
            }
        }
    }
}
