package com.marketlabs.pulse.network.store.posture

import android.util.Log
import com.marketlabs.pulse.network.api.MarketPostureApi
import com.marketlabs.pulse.storage.model.posture.DomainMarketPosture
import com.marketlabs.pulse.storage.model.posture.mappers.toDomain
import javax.inject.Inject

/**
 * 💡 THOUGHT PROCESS:
 * Since the Node.js script writes directly to Firestore, we bypass Retrofit here
 * and fetch the document directly from the "insights" collection. We wrap the
 * Firestore call in a Kotlin Result object to safely handle network timeouts.
 */
class RemoteMarketPostureDataSourceImpl @Inject constructor(
    private val api: MarketPostureApi
) : RemoteMarketPostureDataSource {

    override suspend fun getLatestPosture(): Result<DomainMarketPosture> {
        return try {
            val response = api.getMarketPosture()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Log.e("MarketPosture", "Failed to fetch remote posture", e)
            Result.failure(e)
        }
    }
}