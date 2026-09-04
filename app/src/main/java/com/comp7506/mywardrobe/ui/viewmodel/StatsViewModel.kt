package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(repository: WardrobeRepository) : ViewModel() {
    val totalCount: StateFlow<Int> =
        repository.observeTotalClothingCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val categoryCounts: StateFlow<Map<String, Int>> =
        repository.observeCategoryCounts()
            .map { list -> list.associate { it.category to it.count } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val wearCounts =
        repository.observeItemWearCounts(limit = 20)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

