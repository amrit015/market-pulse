package com.marketlabs.pulse.utils

import com.marketlabs.pulse.BuildConfig

object Constants {
    // --- NETWORK ---
    const val FINNHUB_BASE_URL = "https://finnhub.io/api/v1/"
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val MARKET_PULSE_BASE_URL = "https://api-4pnvedu3ma-uc.a.run.app/"
    const val FINNHUB_TOKEN_ENDPOINT = "wss://ws.finnhub.io?token=${BuildConfig.FINNHUB_KEY}"

    const val DATABASE_NAME = "market_pulse_db"
}