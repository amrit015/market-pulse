package com.marketlabs.pulse.storage.model.dashboard.mappers

import com.marketlabs.pulse.network.model.pastReleases.NetworkPastRelease
import com.marketlabs.pulse.network.model.pastReleases.NetworkPastReleasesResponse
import com.marketlabs.pulse.storage.database.entity.PastReleasesEntity
import com.marketlabs.pulse.storage.model.pastReleases.PastRelease
import com.marketlabs.pulse.storage.model.pastReleases.PastReleases

// Network -> Domain
fun NetworkPastReleasesResponse.toDomain(): PastReleases {
    return PastReleases(
        lastSyncedTimestamp = System.currentTimeMillis(),
        releases = this.releases?.mapValues { (id, release) -> release.toDomain(id) }
    )
}

private fun NetworkPastRelease.toDomain(id: String): PastRelease {
    return PastRelease(
        id = id,
        label = this.label,
        feedTitle = this.feedTitle,
        date = this.date,
        estimate = this.estimate,
        previous = this.previous,
        actual = this.actual,
        actualConfirmed = this.actualConfirmed,
        postReleaseImpact = this.postReleaseImpact,
        resolvedAt = this.resolvedAt
    )
}

// Domain -> Entity
fun PastReleases.toEntity(): PastReleasesEntity {
    return PastReleasesEntity(
        id = "latest",
        lastSyncedTimestamp = System.currentTimeMillis(),
        releases = this.releases
    )
}

// Entity -> Domain
fun PastReleasesEntity.toDomain(): PastReleases {
    return PastReleases(
        lastSyncedTimestamp = this.lastSyncedTimestamp,
        releases = this.releases
    )
}
