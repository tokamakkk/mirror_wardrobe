package com.comp7506.mywardrobe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.db.ClothingItemEntity
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import com.comp7506.mywardrobe.data.repository.WardrobeRepository.OutfitItemTransform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EditOutfitUiState(
    val name: String = "",
    val selectedItemIds: List<Long> = emptyList(),
    val itemTransforms: Map<Long, OutfitItemTransform> = emptyMap(),
    val coverImageUri: String? = null,
    val isLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

class EditOutfitViewModel(
    private val repository: WardrobeRepository,
    private val outfitId: Long,
) : ViewModel() {
    private val state = MutableStateFlow(EditOutfitUiState())

    val uiState: StateFlow<EditOutfitUiState> = state

    val allItems: StateFlow<List<ClothingItemEntity>> =
        repository.observeClothingItems(category = null, query = "")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedItems: StateFlow<List<ClothingItemEntity>> =
        combine(allItems, state) { items, s ->
            val map = items.associateBy { it.id }
            s.selectedItemIds.mapNotNull { map[it] }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.observeOutfits()
                .map { list -> list.firstOrNull { it.outfit.id == outfitId } }
                .distinctUntilChanged()
                .collect { o ->
                    if (o == null || state.value.isLoaded) return@collect
                    val refs = repository.getOrderedItemRefsForOutfit(outfitId)
                    val ids = refs.map { it.itemId }
                    val transforms = refs.associate { ref ->
                        ref.itemId to OutfitItemTransform(
                            offsetX = ref.offsetX,
                            offsetY = ref.offsetY,
                            scale = ref.scale,
                            rotation = ref.rotation,
                        )
                    }
                    state.value = EditOutfitUiState(
                        name = o.outfit.name,
                        selectedItemIds = ids,
                        itemTransforms = transforms,
                        coverImageUri = o.outfit.imageUri,
                        isLoaded = true,
                    )
                }
        }
    }

    fun setName(name: String) {
        state.value = state.value.copy(name = name, errorMessage = null, saved = false)
    }

    fun toggleItem(itemId: Long) {
        val current = state.value.selectedItemIds
        state.value = if (current.contains(itemId)) {
            state.value.copy(
                selectedItemIds = current.filterNot { it == itemId },
                itemTransforms = state.value.itemTransforms - itemId,
                errorMessage = null,
                saved = false,
            )
        } else {
            state.value.copy(
                selectedItemIds = current + itemId,
                itemTransforms = state.value.itemTransforms + (itemId to OutfitItemTransform()),
                errorMessage = null,
                saved = false,
            )
        }
    }

    fun removeItem(itemId: Long) {
        state.value = state.value.copy(
            selectedItemIds = state.value.selectedItemIds.filterNot { it == itemId },
            itemTransforms = state.value.itemTransforms - itemId,
            errorMessage = null,
            saved = false,
        )
    }

    fun updateItemTransform(itemId: Long, transform: OutfitItemTransform) {
        state.value = state.value.copy(
            itemTransforms = state.value.itemTransforms + (itemId to transform),
            saved = false,
        )
    }

    fun save() {
        val current = state.value
        if (!current.isLoaded) return
        val imageUri = selectedItems.value.firstOrNull()?.imageUri ?: current.coverImageUri
        if (current.selectedItemIds.isEmpty() && imageUri.isNullOrBlank()) {
            state.value = current.copy(errorMessage = "Select at least one item or keep the outfit cover image")
            return
        }
        val name = current.name.trim().ifBlank { "Outfit" }
        viewModelScope.launch {
            state.value = state.value.copy(isSaving = true, errorMessage = null, saved = false)
            runCatching {
                repository.updateOutfit(
                    outfitId = outfitId,
                    name = name,
                    itemIdsInOrder = current.selectedItemIds,
                    imageUri = imageUri,
                    itemTransforms = current.itemTransforms,
                )
            }.onSuccess {
                state.value = state.value.copy(isSaving = false, saved = true)
            }.onFailure {
                state.value = state.value.copy(isSaving = false, errorMessage = "Save failed")
            }
        }
    }
}
