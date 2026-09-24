package com.marketlabs.pulse.data.stockAnalysis

import android.content.Context
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StockAnalysisUiStateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StockAnalysisUiStateRepository {

    override val isCustomListBannerDismissed: Flow<Boolean> = context.stockAnalysisUiDataStore.data.map { prefs ->
        prefs[StockAnalysisUiPreferences.CUSTOM_LIST_BANNER_DISMISSED] ?: false
    }

    override suspend fun dismissCustomListBanner() {
        context.stockAnalysisUiDataStore.edit { prefs ->
            prefs[StockAnalysisUiPreferences.CUSTOM_LIST_BANNER_DISMISSED] = true
        }
    }
}
