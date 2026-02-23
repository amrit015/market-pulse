package com.marketlabs.pulse.core.riskRadar

import com.marketlabs.pulse.storage.model.riskRadar.RiskRadar
import kotlinx.coroutines.flow.Flow

interface RiskRadarRepository {

    fun getRiskStream(): Flow<RiskRadar?>

    suspend fun refreshRisk(force: Boolean): Result<Unit>
}