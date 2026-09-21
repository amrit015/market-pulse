package com.marketlabs.pulse.ui.screens.stocks

import com.marketlabs.pulse.storage.model.stocks.StockPreview

/**
 * Backed by the backend's `asset_type` field (STOCK | ETF | INDEX | CRYPTO | OTHER, present on both
 * `market_stocks/{symbol}` and its detail doc, defaulting server-side to
 * "STOCK" if ever missing). This is the one place the Stocks/Indices-ETF tab split is decided --
 * CRYPTO/OTHER (and a null/missing value) fall into "Stocks" along with STOCK, since the Analysis
 * screen only has these two tabs, not a third bucket for them.
 */
fun StockPreview.isIndexOrEtf(): Boolean = assetType == "ETF" || assetType == "INDEX"
