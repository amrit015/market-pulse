package com.marketlabs.pulse.core.analytics

import android.net.Uri

/**
 * The host of [url] for the `publisher` param, or `null` when it has none. Host only, never the
 * URL: paths and query strings can carry tokens or tracking ids.
 */
fun publisherHost(url: String): String? =
    Uri.parse(url).host?.removePrefix("www.")?.takeIf { it.isNotEmpty() }?.take(MAX_PARAM_LENGTH)

private const val MAX_PARAM_LENGTH = 100
