package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.db.ClothingItemEntity
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CreateOutfitUiState(
    val name: String = "",
    val occasion: String? = null,
    val selectedItemIds: List<Long> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

class CreateOutfitViewModel(private val repository: WardrobeRepository) : ViewModel() {
    private val state = MutableStateFlow(CreateOutfitUiState())
    val uiState: StateFlow<CreateOutfitUiState> = state

    val allItems: StateFlow<List<ClothingItemEntity>> =
        repository.observeClothingItems(category = null, query = "")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedItems: StateFlow<List<ClothingItemEntity>> =
        combine(allItems, state) { items, s ->
            val map = items.associateBy { it.id }
            s.selectedItemIds.mapNotNull { map[it] }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setName(name: String) {
        state.value = state.value.copy(name = name, errorMessage = null, saved = false)
    }

    fun setOccasion(occasion: String) {
        state.value = state.value.copy(occasion = occasion, errorMessage = null, saved = false)
    }

    fun toggleItem(itemId: Long) {
        val current = state.value.selectedItemIds
        state.value = if (current.contains(itemId)) {
            state.value.copy(selectedItemIds = current.filterNot { it == itemId }, errorMessage = null, saved = false)
        } else {
            state.value.copy(selectedItemIds = current + itemId, errorMessage = null, saved = false)
        }
    }

    fun removeItem(itemId: Long) {
        state.value = state.value.copy(
            selectedItemIds = state.value.selectedItemIds.filterNot { it == itemId },
            errorMessage = null,
            saved = false,
        )
    }

    fun save() {
        val current = state.value
        if (current.occasion.isNullOrBlank()) {
            state.value = current.copy(errorMessage = "Please choose an occasion")
            return
        }
        if (current.selectedItemIds.isEmpty()) {
            state.value = current.copy(errorMessage = "Please select at least one item")
            return
        }
        val name = current.name.trim().ifBlank { "New outfit" }
        viewModelScope.launch {
            state.value = state.value.copy(isSaving = true, errorMessage = null, saved = false)
            val imageUri = selectedItems.value.firstOrNull()?.imageUri
            runCatching {
                repository.addOutfit(
                    name = name,
                    occasion = current.occasion,
                    itemIdsInOrder = current.selectedItemIds,
                    imageUri = imageUri,
                )
            }.onSuccess {
                state.value = CreateOutfitUiState(saved = true)
            }.onFailure {
                state.value = state.value.copy(isSaving = false, errorMessage = "Save failed")
            }
        }
    }
}

