package com.marketlabs.pulse.core.analytics

import com.marketlabs.pulse.core.ads.AdManager
import com.marketlabs.pulse.data.favorites.FavoriteMetricsRepository
import com.marketlabs.pulse.data.favorites.FavoriteStocksRepository
import com.marketlabs.pulse.data.legal.LegalRepository
import com.marketlabs.pulse.data.notifications.NotificationPreference
import com.marketlabs.pulse.data.notifications.NotificationPreferencesRepository
import com.marketlabs.pulse.data.privacy.AnalyticsConsentRepository
import com.marketlabs.pulse.data.theme.ThemeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the analytics tracker in step with app state: whether collection is on, and the user
 * properties. Each is a flow collected for the life of the process, which covers both the value at
 * startup and every later change without each feature having to remember to report it.
 */
@Singleton
class AnalyticsStateSync @Inject constructor(
    private val tracker: AnalyticsTracker,
    private val consentRepository: AnalyticsConsentRepository,
    private val themeRepository: ThemeRepository,
    private val favoriteStocksRepository: FavoriteStocksRepository,
    private val favoriteMetricsRepository: FavoriteMetricsRepository,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
    private val legalRepository: LegalRepository,
    private val adManager: AdManager
) {

    fun start(scope: CoroutineScope) {
        scope.launch {
            consentRepository.enabled
                .map { AnalyticsCollection.effective(it) }
                .distinctUntilChanged()
                .collect { tracker.setCollectionEnabled(it) }
        }
        sync(scope, UserProperty.THEME_PRESET, themeRepository.selectedTheme.map { it.name.lowercase(Locale.ROOT) })
        sync(scope, UserProperty.FAV_STOCKS_BUCKET, favoriteStocksRepository.favoriteSymbols.map { UserProperty.favoritesBucket(it.size) })
        sync(scope, UserProperty.FAV_METRICS_BUCKET, favoriteMetricsRepository.favoriteMetricIds.map { UserProperty.favoritesBucket(it.size) })
        sync(scope, UserProperty.PUSH_SUMMARY_ON, notificationPreferencesRepository.enabledPreferences.map { (NotificationPreference.DAILY_SUMMARY in it).toString() })
        sync(scope, UserProperty.PUSH_STOCKS_ON, notificationPreferencesRepository.enabledPreferences.map { (NotificationPreference.STOCK_ANALYSIS in it).toString() })
        sync(scope, UserProperty.LEGAL_VERSION, legalRepository.acceptedVersion.map { it.toString() })
        sync(scope, UserProperty.AD_FREE, adManager.isAdFree.map { it.toString() })
    }

    private fun sync(
        scope: CoroutineScope,
        property: UserProperty,
        values: kotlinx.coroutines.flow.Flow<String>
    ) {
        scope.launch { values.distinctUntilChanged().collect { tracker.setUserProperty(property, it) } }
    }
}
