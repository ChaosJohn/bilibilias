package com.imcys.bilibilias.ui.setting.quality

import com.imcys.bilibilias.datastore.AppSettings
import com.imcys.bilibilias.network.model.video.convertVideoQualityIdValue

private val defaultVideoQualityPreferenceOrder = listOf(
    127L,
    126L,
    125L,
    120L,
    116L,
    112L,
    100L,
    80L,
    74L,
    64L,
    32L,
    16L,
    6L,
)

fun defaultVideoQualityPreferenceOrder(): List<Long> =
    defaultVideoQualityPreferenceOrder

fun List<Long>.normalizedVideoQualityPreferenceOrder(): List<Long> {
    val normalized = distinct().filter { it in defaultVideoQualityPreferenceOrder }.toMutableList()
    defaultVideoQualityPreferenceOrder.forEach { qualityId ->
        if (qualityId !in normalized) {
            normalized.add(qualityId)
        }
    }
    return normalized
}

fun AppSettings.normalizedVideoQualityPreferenceOrder(): List<Long> =
    videoQualityPreferenceOrderList.normalizedVideoQualityPreferenceOrder()

fun Long.videoQualityDisplayName(): String =
    convertVideoQualityIdValue(this)

fun selectPreferredVideoQuality(
    availableQualityIds: Collection<Long>,
    preferenceOrder: List<Long>
): Long? {
    val normalizedAvailable = availableQualityIds.distinct()
    val normalizedPreferenceOrder = preferenceOrder.normalizedVideoQualityPreferenceOrder()

    for (preference in normalizedPreferenceOrder) {
        val matchedQuality = normalizedAvailable.firstOrNull { qualityId ->
            qualityId == preference
        }
        if (matchedQuality != null) {
            return matchedQuality
        }
    }

    return normalizedAvailable.firstOrNull()
}

fun sortVideoQualitiesByPreference(
    availableQualityIds: Collection<Long>,
    preferenceOrder: List<Long>
): List<Long> {
    val normalizedAvailable = availableQualityIds.distinct()
    val preferenceIndex = preferenceOrder.normalizedVideoQualityPreferenceOrder()
        .withIndex()
        .associate { it.value to it.index }
    val rawIndex = normalizedAvailable.withIndex().associate { it.value to it.index }

    return normalizedAvailable.sortedWith(
        compareBy<Long>(
            { qualityId ->
                preferenceIndex[qualityId] ?: Int.MAX_VALUE
            },
            { qualityId -> rawIndex[qualityId] ?: Int.MAX_VALUE }
        )
    )
}

