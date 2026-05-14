package com.imcys.bilibilias.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.imcys.bilibilias.datastore.AppSettings
import com.imcys.bilibilias.datastore.Settings
import com.imcys.bilibilias.datastore.copy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

class AppSettingsRepository(
    private val dataStore: DataStore<AppSettings>,
) {
    private val TAG: String = "AppSettingsRepository"

    private fun createDefaultVideoCodecPreferenceOrder() = listOf(
        AppSettings.VideoCodecPreference.AV1,
        AppSettings.VideoCodecPreference.H265,
        AppSettings.VideoCodecPreference.H264,
    )

    private fun normalizeVideoCodecPreferenceOrder(order: List<AppSettings.VideoCodecPreference>): List<AppSettings.VideoCodecPreference> {
        val defaults = createDefaultVideoCodecPreferenceOrder()
        val normalized = order.distinct().filter { it in defaults }.toMutableList()
        defaults.forEach { codec ->
            if (codec !in normalized) {
                normalized.add(codec)
            }
        }
        return normalized
    }

    private fun createDefaultVideoQualityPreferenceOrder() = listOf(
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

    private fun createDefaultAudioQualityPreferenceOrder() = listOf(
        30251L,
        30250L,
        30280L,
        30232L,
        30216L,
    )

    private fun normalizeQualityPreferenceOrder(order: List<Long>, defaults: List<Long>): List<Long> {
        val normalized = order.distinct().filter { it in defaults }.toMutableList()
        defaults.forEach { qualityId ->
            if (qualityId !in normalized) {
                normalized.add(qualityId)
            }
        }
        return normalized
    }

    val appSettingsFlow: Flow<AppSettings> = dataStore.data


    // 同意了隐私政策
    suspend fun hasAgreedPrivacyPolicy(): Boolean {
        val currentSettings = dataStore.data.first()
        return currentSettings.agreePrivacyPolicy == AppSettings.AgreePrivacyPolicyState.Agreed
    }


    // 添加更新隐私政策同意状态的方法
    suspend fun updatePrivacyPolicyAgreement(agreed: AppSettings.AgreePrivacyPolicyState) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setAgreePrivacyPolicy(agreed)
                .build()
        }
    }


    // 添加更新隐私政策同意状态的方法
    suspend fun updateKnowAboutApp(knowAboutApp: AppSettings.KnowAboutApp) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setKnowAboutApp(knowAboutApp)
                .build()
        }
    }

    suspend fun updateRoamEnabledState(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                enabledRoam = enabled
            }
        }
    }

    suspend fun updateEnabledDynamicColor(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                enabledDynamicColor = enabled
            }
        }
    }

    suspend fun updateClipboardAutoHandling(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                enabledClipboardAutoHandling = enabled
            }
        }
    }

    suspend fun updateAutoBackAfterCreateDownload(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                enabledAutoBackAfterCreateDownload = enabled
            }
        }
    }

    suspend fun updateAutoDownloadAfterParseSuccess(enabled: Boolean) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                enabledAutoDownloadAfterParseSuccess = enabled
            }
        }
    }

    suspend fun asyncVideoCodecPreferenceOrder(): List<AppSettings.VideoCodecPreference> {
        val currentList = dataStore.data.first().videoCodecPreferenceOrderList
        val normalizedList = normalizeVideoCodecPreferenceOrder(currentList)
        if (currentList != normalizedList) {
            dataStore.updateData { currentSettings ->
                currentSettings.toBuilder()
                    .clearVideoCodecPreferenceOrder()
                    .addAllVideoCodecPreferenceOrder(normalizedList)
                    .build()
            }
        }
        return normalizedList
    }

    suspend fun updateVideoCodecPreferenceOrder(newList: List<AppSettings.VideoCodecPreference>) {
        val normalizedList = normalizeVideoCodecPreferenceOrder(newList)
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .clearVideoCodecPreferenceOrder()
                .addAllVideoCodecPreferenceOrder(normalizedList)
                .build()
        }
    }

    suspend fun asyncVideoQualityPreferenceOrder(): List<Long> {
        val currentList = dataStore.data.first().videoQualityPreferenceOrderList
        val normalizedList = normalizeQualityPreferenceOrder(
            order = currentList,
            defaults = createDefaultVideoQualityPreferenceOrder(),
        )
        if (currentList != normalizedList) {
            dataStore.updateData { currentSettings ->
                currentSettings.toBuilder()
                    .clearVideoQualityPreferenceOrder()
                    .addAllVideoQualityPreferenceOrder(normalizedList)
                    .build()
            }
        }
        return normalizedList
    }

    suspend fun updateVideoQualityPreferenceOrder(newList: List<Long>) {
        val normalizedList = normalizeQualityPreferenceOrder(
            order = newList,
            defaults = createDefaultVideoQualityPreferenceOrder(),
        )
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .clearVideoQualityPreferenceOrder()
                .addAllVideoQualityPreferenceOrder(normalizedList)
                .build()
        }
    }

    suspend fun asyncAudioQualityPreferenceOrder(): List<Long> {
        val currentList = dataStore.data.first().audioQualityPreferenceOrderList
        val normalizedList = normalizeQualityPreferenceOrder(
            order = currentList,
            defaults = createDefaultAudioQualityPreferenceOrder(),
        )
        if (currentList != normalizedList) {
            dataStore.updateData { currentSettings ->
                currentSettings.toBuilder()
                    .clearAudioQualityPreferenceOrder()
                    .addAllAudioQualityPreferenceOrder(normalizedList)
                    .build()
            }
        }
        return normalizedList
    }

    suspend fun updateAudioQualityPreferenceOrder(newList: List<Long>) {
        val normalizedList = normalizeQualityPreferenceOrder(
            order = newList,
            defaults = createDefaultAudioQualityPreferenceOrder(),
        )
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .clearAudioQualityPreferenceOrder()
                .addAllAudioQualityPreferenceOrder(normalizedList)
                .build()
        }
    }

    suspend fun updateLastSkipUpdateVersionCode(versionCode: Int) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setLastSkipUpdateVersionCode(versionCode)
                .build()
        }
    }

    suspend fun asyncHomeLayoutTypesetList(): List<AppSettings.HomeLayoutItem> {
        val defaultList = createDefaultHomeLayoutItems()
        val existingList = dataStore.data.first().homeLayoutTypesetList.toMutableList()

        return if (existingList.isEmpty()) {
            dataStore.updateData { currentSettings ->
                currentSettings.toBuilder()
                    .clearHomeLayoutTypeset()
                    .addAllHomeLayoutTypeset(defaultList)
                    .build()
            }
            defaultList
        } else {
            val existingTypes = existingList.map { it.type }.toSet()
            val missingItems = defaultList.filterNot { it.type in existingTypes }
            existingList.addAll(missingItems)
            existingList
        }
    }

    private fun createDefaultHomeLayoutItems(): List<AppSettings.HomeLayoutItem> {
        val defaultTypes = listOf(
            AppSettings.HomeLayoutType.Banner,
            AppSettings.HomeLayoutType.Announcement,
            AppSettings.HomeLayoutType.UpdateInfo,
            AppSettings.HomeLayoutType.Tools,
            AppSettings.HomeLayoutType.DownloadList
        )

        return defaultTypes.map { type ->
            AppSettings.HomeLayoutItem.newBuilder()
                .setType(type)
                .setIsHidden(false)
                .build()
        }
    }

    suspend fun updateHomeLayoutTypesetList(newList: List<AppSettings.HomeLayoutItem>) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .clearHomeLayoutTypeset()
                .addAllHomeLayoutTypeset(newList)
                .build()
        }
    }


    suspend fun updateLastBulletinContent(content: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setLastBulletinContent(content)
                .build()
        }
    }

    suspend fun saveDownloadSAFUriString(uriString: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setDownloadUri(uriString)
                .build()
        }
    }

    suspend fun updateEpisodeListMode(it: AppSettings.EpisodeListMode) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                episodeListMode = it
            }
        }
    }

    suspend fun updateVideoNamingRule(rule: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                videoNamingRule = rule
            }
        }
    }

    suspend fun updateBangumiNamingRule(rule: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                bangumiNamingRule = rule
            }
        }
    }

    suspend fun updateLineHost(lineHost: String) {
        dataStore.updateData { currentSettings ->
            currentSettings.copy {
                this.biliLineHost = lineHost
            }
        }
    }

    // 存储使用工具记录
    suspend fun updateUseToolRecord(toolName: String) {
        dataStore.updateData { currentSettings ->
            val historyList = currentSettings.useToolHistoryList.toMutableList()
            if (historyList.size > 10) {
                historyList.removeLastOrNull()
            }
            historyList.add(0, toolName)
            // 去重
            val distinctList = historyList.distinct()
            currentSettings.toBuilder()
                .clearUseToolHistory()
                .addAllUseToolHistory(distinctList)
                .build()
        }
    }

}


fun AppSettings.HomeLayoutType.getDescription(): String = when (this) {
    AppSettings.HomeLayoutType.Banner -> "轮播图"
    AppSettings.HomeLayoutType.Announcement -> "公告信息"
    AppSettings.HomeLayoutType.UpdateInfo -> "更新信息"
    AppSettings.HomeLayoutType.Tools -> "工具列表"
    AppSettings.HomeLayoutType.DownloadList -> "下载列表"
    else -> this.name
}