package com.imcys.bilibilias.ui.setting.quality

import com.imcys.bilibilias.datastore.AppSettings
import com.imcys.bilibilias.network.model.video.convertAudioQualityIdValue

private val defaultAudioQualityPreferenceOrder = listOf(
    30251L,
    30250L,
    30280L,
    30232L,
    30216L,
)

fun defaultAudioQualityPreferenceOrder(): List<Long> =
    defaultAudioQualityPreferenceOrder

fun List<Long>.normalizedAudioQualityPreferenceOrder(): List<Long> {
    val normalized = distinct().filter { it in defaultAudioQualityPreferenceOrder }.toMutableList()
    defaultAudioQualityPreferenceOrder.forEach { qualityId ->
        if (qualityId !in normalized) {
            normalized.add(qualityId)
        }
    }
    return normalized
}

fun AppSettings.normalizedAudioQualityPreferenceOrder(): List<Long> =
    audioQualityPreferenceOrderList.normalizedAudioQualityPreferenceOrder()

fun Long.audioQualityDisplayName(): String =
    convertAudioQualityIdValue(this)

fun selectPreferredAudioQuality(
    availableQualityIds: Collection<Long>,
    preferenceOrder: List<Long>
): Long? {
    val normalizedAvailable = availableQualityIds.distinct()
    val normalizedPreferenceOrder = preferenceOrder.normalizedAudioQualityPreferenceOrder()

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

fun sortAudioQualitiesByPreference(
    availableQualityIds: Collection<Long>,
    preferenceOrder: List<Long>
): List<Long> {
    val normalizedAvailable = availableQualityIds.distinct()
    val preferenceIndex = preferenceOrder.normalizedAudioQualityPreferenceOrder()
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

