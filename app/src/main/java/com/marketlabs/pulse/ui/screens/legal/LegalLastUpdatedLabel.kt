package com.marketlabs.pulse.ui.screens.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.marketlabs.pulse.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.DateTimeParseException

/**
 * "Last updated: <date>" for a legal document, with the document's ISO `lastUpdated` rendered in
 * the device locale. An unparseable date is shown verbatim rather than hiding the line, so a
 * malformed asset is visible instead of silently dropping the date.
 */
@Composable
fun legalLastUpdatedLabel(isoDate: String): String {
    val formatted = try {
        LocalDate.parse(isoDate).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    } catch (e: DateTimeParseException) {
        isoDate
    }
    return stringResource(id = R.string.legal_last_updated, formatted)
}
