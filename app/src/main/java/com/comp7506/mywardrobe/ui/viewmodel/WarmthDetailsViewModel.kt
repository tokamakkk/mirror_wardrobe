package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.db.OutfitWithItems
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import com.comp7506.mywardrobe.domain.recommendation.OutfitRecommendation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class WarmthDetailsViewModel(private val repository: WardrobeRepository) : ViewModel() {
    private val selectedOccasion = MutableStateFlow("All")
    private val weatherRecommendation = MutableStateFlow<String?>(null)
    private val requiredWarmth = MutableStateFlow(0)

    val currentOccasion: StateFlow<String> =
        selectedOccasion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "All")

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

    val matchedOutfits: StateFlow<List<OutfitWithItems>> =
        combine(repository.observeOutfits(), selectedOccasion) { all, occ ->
            if (occ == "All") {
                emptyList()
            } else {
                all.filter { o ->
                    val itemOccasion = normalizeOccasion(o.outfit.occasion)
                    itemOccasion.equals(occ, ignoreCase = true)
                }.take(10)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todaySuggestion: StateFlow<String?> =
        combine(selectedOccasion, matchedOutfits, requiredWarmth, weatherRecommendation) { occ, outfits, reqWarmth, weatherRec ->
            if (occ == "All") {
                weatherRec
            } else {
                outfits.minByOrNull { outfit ->
                    Math.abs(outfit.items.sumOf { it.warmthValue } - reqWarmth)
                }?.let { outfit ->
                    val totalWarmth = outfit.items.sumOf { it.warmthValue }
                    "${outfit.outfit.name} ($totalWarmth)"
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setOccasion(value: String) {
        selectedOccasion.value = value
    }

    fun setWeatherRecommendation(recommendation: OutfitRecommendation?) {
        weatherRecommendation.value = recommendation?.suggestions?.firstOrNull()
        requiredWarmth.value = recommendation?.requiredWarmth ?: 0
    }
}