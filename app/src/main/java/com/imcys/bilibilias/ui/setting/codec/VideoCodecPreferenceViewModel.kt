package com.imcys.bilibilias.ui.setting.codec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imcys.bilibilias.data.repository.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoCodecPreferenceViewModel(
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _codecPreferenceOrder = MutableStateFlow(defaultVideoCodecPreferenceOrder())
    val codecPreferenceOrder = _codecPreferenceOrder.asStateFlow()

    init {
        viewModelScope.launch {
            appSettingsRepository.asyncVideoCodecPreferenceOrder()
            appSettingsRepository.appSettingsFlow.collect { settings ->
                _codecPreferenceOrder.value = settings.normalizedVideoCodecPreferenceOrder()
            }
        }
    }

    fun moveCodecItem(fromIndex: Int, toIndex: Int) {
        val currentList = _codecPreferenceOrder.value.toMutableList()
        if (currentList.isEmpty()) return
        if (fromIndex !in currentList.indices) return
        if (fromIndex == toIndex) return
        val item = currentList.removeAt(fromIndex)
        val insertIndex = toIndex.coerceIn(0, currentList.size)
        currentList.add(insertIndex, item)
        _codecPreferenceOrder.value = currentList
        viewModelScope.launch {
            appSettingsRepository.updateVideoCodecPreferenceOrder(currentList)
        }
    }

    fun restoreDefault() {
        viewModelScope.launch {
            val defaultOrder = defaultVideoCodecPreferenceOrder()
            _codecPreferenceOrder.value = defaultOrder
            appSettingsRepository.updateVideoCodecPreferenceOrder(defaultOrder)
        }
    }
}



