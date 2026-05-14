package com.imcys.bilibilias.ui.setting.codec

import com.imcys.bilibilias.datastore.AppSettings

private val defaultVideoCodecPreferenceOrder = listOf(
    AppSettings.VideoCodecPreference.AV1,
    AppSettings.VideoCodecPreference.H265,
    AppSettings.VideoCodecPreference.H264,
)

private val h265RawCodecSet = setOf("hev1", "hvc1", "dvh1")

fun defaultVideoCodecPreferenceOrder(): List<AppSettings.VideoCodecPreference> =
    defaultVideoCodecPreferenceOrder

fun List<AppSettings.VideoCodecPreference>.normalizedVideoCodecPreferenceOrder(): List<AppSettings.VideoCodecPreference> {
    val normalized = distinct().filter { it in defaultVideoCodecPreferenceOrder }.toMutableList()
    defaultVideoCodecPreferenceOrder.forEach { codec ->
        if (codec !in normalized) {
            normalized.add(codec)
        }
    }
    return normalized
}

fun AppSettings.normalizedVideoCodecPreferenceOrder(): List<AppSettings.VideoCodecPreference> =
    videoCodecPreferenceOrderList.normalizedVideoCodecPreferenceOrder()

fun AppSettings.VideoCodecPreference.displayName(): String = when (this) {
    AppSettings.VideoCodecPreference.AV1 -> "AV1 / av01"
    AppSettings.VideoCodecPreference.H264 -> "AVC / h.264"
    AppSettings.VideoCodecPreference.H265 -> "HEVC / h.265"
    else -> name
}

fun AppSettings.VideoCodecPreference.description(): String = when (this) {
    AppSettings.VideoCodecPreference.AV1 -> "清晰度允许时优先尝试 AV1 编码"
    AppSettings.VideoCodecPreference.H264 -> "兼容性更高的 AVC 编码"
    AppSettings.VideoCodecPreference.H265 -> "压缩效率更高的 HEVC 编码"
    else -> ""
}

fun String.toVideoCodecPreference(): AppSettings.VideoCodecPreference? = when (substringBefore('.')) {
    "av01" -> AppSettings.VideoCodecPreference.AV1
    "avc1" -> AppSettings.VideoCodecPreference.H264
    in h265RawCodecSet -> AppSettings.VideoCodecPreference.H265
    else -> null
}

fun AppSettings.VideoCodecPreference.matchesRawCodec(codec: String): Boolean = when (this) {
    AppSettings.VideoCodecPreference.AV1 -> codec.substringBefore('.') == "av01"
    AppSettings.VideoCodecPreference.H264 -> codec.substringBefore('.') == "avc1"
    AppSettings.VideoCodecPreference.H265 -> codec.substringBefore('.') in h265RawCodecSet
    else -> false
}

fun selectPreferredVideoCodec(
    availableCodecs: Collection<String>,
    preferenceOrder: List<AppSettings.VideoCodecPreference>
): String? {
    val normalizedAvailable = availableCodecs.map { it.substringBefore('.') }.distinct()
    val normalizedPreferenceOrder = preferenceOrder.normalizedVideoCodecPreferenceOrder()

    for (preference in normalizedPreferenceOrder) {
        val matchedCodec = normalizedAvailable.firstOrNull { codec ->
            preference.matchesRawCodec(codec)
        }
        if (matchedCodec != null) {
            return matchedCodec
        }
    }

    return normalizedAvailable.firstOrNull()
}

fun sortVideoCodecsByPreference(
    availableCodecs: Collection<String>,
    preferenceOrder: List<AppSettings.VideoCodecPreference>
): List<String> {
    val normalizedAvailable = availableCodecs.map { it.substringBefore('.') }.distinct()
    val preferenceIndex = preferenceOrder.normalizedVideoCodecPreferenceOrder()
        .withIndex()
        .associate { it.value to it.index }
    val rawIndex = normalizedAvailable.withIndex().associate { it.value to it.index }

    return normalizedAvailable.sortedWith(
        compareBy<String>(
            { codec ->
                preferenceIndex[codec.toVideoCodecPreference()] ?: Int.MAX_VALUE
            },
            { codec -> rawIndex[codec] ?: Int.MAX_VALUE }
        )
    )
}

