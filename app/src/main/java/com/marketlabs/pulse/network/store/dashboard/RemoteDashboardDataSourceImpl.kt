package com.marketlabs.pulse.network.store.dashboard

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.marketlabs.pulse.network.model.dashboard.NetworkAssetOverview
import com.marketlabs.pulse.network.model.dashboard.NetworkMarketState
import com.marketlabs.pulse.network.model.dashboard.NetworkTechnicalSummary
import com.marketlabs.pulse.storage.database.entity.AssetOverviewEntity
import com.marketlabs.pulse.storage.database.entity.MarketStateEntity
import com.marketlabs.pulse.storage.model.dashboard.mappers.mergeTechnicalSummary
import com.marketlabs.pulse.storage.model.dashboard.mappers.toEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RemoteDashboardDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteDashboardDataSource {

    override fun observeDashboardData(): Flow<Pair<MarketStateEntity, List<AssetOverviewEntity>>> = callbackFlow {
        val listener = firestore.collection("market_overview")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Dashboard", "Firestore listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    var marketStateEntity: MarketStateEntity? = null
                    var tempTechnicalSummary: NetworkTechnicalSummary? = null
                    val assetsEntities = mutableListOf<AssetOverviewEntity>()

                    for (doc in snapshot.documents) {
                        when (doc.id) {
                            "market_state" -> {
                                val state = doc.toObject(NetworkMarketState::class.java)
                                state?.let { marketStateEntity = it.toEntity() }
                            }
                            "technical_summary" -> {
                                tempTechnicalSummary = doc.toObject(NetworkTechnicalSummary::class.java)
                            }
                            "config" -> {
                                // 💡 FIX: Explicitly ignore the config document!
                            }
                            else -> {
                                val asset = doc.toObject(NetworkAssetOverview::class.java)
                                asset?.let { assetsEntities.add(it.toEntity()) }
                            }
                        }
                    }

                    if (marketStateEntity != null) {
                        // Merge the technical_summary doc into the MarketState header object
                        val finalState = marketStateEntity!!.mergeTechnicalSummary(tempTechnicalSummary)

                        // Emit the fully combined data into the Flow
                        trySend(Pair(finalState, assetsEntities))
                    }
                }
            }

        awaitClose { listener.remove() }
    }
}