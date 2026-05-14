package com.imcys.bilibilias.ui.setting.quality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imcys.bilibilias.data.repository.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioQualityPreferenceViewModel(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _qualityPreferenceOrder = MutableStateFlow(defaultAudioQualityPreferenceOrder())
    val qualityPreferenceOrder = _qualityPreferenceOrder.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.asyncAudioQualityPreferenceOrder()
            appSettingsRepository.appSettingsFlow.collect { settings ->
                _qualityPreferenceOrder.value = settings.normalizedAudioQualityPreferenceOrder()
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
            appSettingsRepository.updateAudioQualityPreferenceOrder(currentList)
        }
    }

    fun restoreDefault() {
        viewModelScope.launch {
            val defaultOrder = defaultAudioQualityPreferenceOrder()
            _qualityPreferenceOrder.value = defaultOrder
            appSettingsRepository.updateAudioQualityPreferenceOrder(defaultOrder)
        }
    }
}

