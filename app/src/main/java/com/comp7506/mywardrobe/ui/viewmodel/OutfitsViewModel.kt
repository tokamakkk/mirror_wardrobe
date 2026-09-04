package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.db.OutfitWithItems
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OutfitsViewMode {
    Grid,
    List,
}

class OutfitsViewModel(private val repository: WardrobeRepository) : ViewModel() {
    private fun normalizeOccasion(raw: String?): String? {
        val v = raw?.trim().orEmpty()
        if (v.isBlank()) return null
        return when (v.lowercase()) {
            "business" -> "Business"
            "office" -> "Office"
            "dating" -> "Dating"
            "casual" -> "Casual"
            "ceremony" -> "Ceremony"
            "sport" -> "Sport"
            "homewear" -> "homewear"
            else -> v
        }
    }

    private val query = MutableStateFlow("")
    private val occasion = MutableStateFlow<String?>(null)
    private val viewMode = MutableStateFlow(OutfitsViewMode.List)

    val currentQuery: StateFlow<String> =
        query.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val currentOccasion: StateFlow<String?> =
        occasion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentViewMode: StateFlow<OutfitsViewMode> =
        viewMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OutfitsViewMode.List)

    val outfits: StateFlow<List<OutfitWithItems>> =
        combine(repository.observeOutfits(), query, occasion) { all, q, occ ->
            val trimmed = q.trim()
            val selectedOccasion = normalizeOccasion(occ)
            all.filter { o ->
                val itemOccasion = normalizeOccasion(o.outfit.occasion)
                val occasionMatches =
                    selectedOccasion == null || itemOccasion.equals(selectedOccasion, ignoreCase = true)
                val queryMatches =
                    trimmed.isBlank() || (itemOccasion?.contains(trimmed, ignoreCase = true) == true)
                occasionMatches && queryMatches
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setOccasion(value: String?) {
        occasion.value = value
    }

    fun toggleViewMode() {
        viewMode.value = if (viewMode.value == OutfitsViewMode.Grid) OutfitsViewMode.List else OutfitsViewMode.Grid
    }

    fun deleteOutfit(outfitId: Long) {
        viewModelScope.launch {
            repository.deleteOutfit(outfitId)
        }
    }
}

