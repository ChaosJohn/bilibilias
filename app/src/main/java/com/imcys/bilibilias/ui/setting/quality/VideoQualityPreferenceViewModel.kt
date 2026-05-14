package com.imcys.bilibilias.ui.setting.quality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imcys.bilibilias.data.repository.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoQualityPreferenceViewModel(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _qualityPreferenceOrder = MutableStateFlow(defaultVideoQualityPreferenceOrder())
    val qualityPreferenceOrder = _qualityPreferenceOrder.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.asyncVideoQualityPreferenceOrder()
            appSettingsRepository.appSettingsFlow.collect { settings ->
                _qualityPreferenceOrder.value = settings.normalizedVideoQualityPreferenceOrder()
            }
        }
    }

    fun moveQualityItem(fromIndex: Int, toIndex: Int) {
        val currentList = _qualityPreferenceOrder.value.toMutableList()
        if (currentList.isEmpty()) return
        if (fromIndex !in currentList.indices) return
        if (fromIndex == toIndex) return
        val item = currentList.removeAt(fromIndex)
        val insertIndex = toIndex.coerceIn(0, currentList.size)
        currentList.add(insertIndex, item)
        _qualityPreferenceOrder.value = currentList
        viewModelScope.launch {
            appSettingsRepository.updateVideoQualityPreferenceOrder(currentList)
        }
    }

    fun restoreDefault() {
        viewModelScope.launch {
            val defaultOrder = defaultVideoQualityPreferenceOrder()
            _qualityPreferenceOrder.value = defaultOrder
            appSettingsRepository.updateVideoQualityPreferenceOrder(defaultOrder)
        }
    }
}

