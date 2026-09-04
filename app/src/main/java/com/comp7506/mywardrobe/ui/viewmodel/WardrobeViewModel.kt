package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.db.ClothingItemEntity
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WardrobeViewMode {
    Grid,
    List,
}

class WardrobeViewModel(private val repository: WardrobeRepository) : ViewModel() {
    private val category = MutableStateFlow<String?>(null)
    private val query = MutableStateFlow("")
    private val viewMode = MutableStateFlow(WardrobeViewMode.Grid)

    val currentCategory: StateFlow<String?> = category.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val currentQuery: StateFlow<String> = query.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val currentViewMode: StateFlow<WardrobeViewMode> = viewMode.stateIn(viewModelScope, SharingStarted.Eagerly, WardrobeViewMode.Grid)

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<ClothingItemEntity>> =
        combine(category, query) { c, q -> c to q }
            .flatMapLatest { (c, q) -> repository.observeClothingItems(category = c, query = q) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCategory(newCategory: String?) {
        category.value = newCategory
    }

    fun setQuery(newQuery: String) {
        query.value = newQuery
    }

    fun toggleViewMode() {
        viewMode.value = if (viewMode.value == WardrobeViewMode.Grid) WardrobeViewMode.List else WardrobeViewMode.Grid
    }

    fun deleteItem(id: Long) {
        viewModelScope.launch {
            repository.deleteClothingItem(id)
        }
    }
}

