package com.comp7506.mywardrobe.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import com.comp7506.mywardrobe.util.copyUriToInternalStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddItemUiState(
    val name: String = "",
    val category: String = "Tops",
    val warmthValue: Int = 0,
    val imageUri: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false,
)

class AddItemViewModel(
    application: Application,
    private val repository: WardrobeRepository
) : AndroidViewModel(application) {
    private val state = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = state

    fun setName(name: String) {
        state.value = state.value.copy(name = name, errorMessage = null, saved = false)
    }

    fun setCategory(category: String) {
        state.value = state.value.copy(category = category, errorMessage = null, saved = false)
    }

    fun setWarmthValue(warmthValue: Int) {
        state.value = state.value.copy(warmthValue = warmthValue, errorMessage = null, saved = false)
    }

    fun setImageUri(imageUri: String?) {
        state.value = state.value.copy(imageUri = imageUri, errorMessage = null, saved = false)
    }

    fun save() {
        val current = state.value
        if (current.name.isBlank()) {
            state.value = current.copy(errorMessage = "Please enter a name")
            return
        }
        viewModelScope.launch {
            state.value = state.value.copy(isSaving = true, errorMessage = null, saved = false)
            runCatching {
                // 如果有图片，将其复制到内部存储以实现永久保存
                val finalImageUri = current.imageUri?.let { uriString ->
                    if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                        copyUriToInternalStorage(getApplication(), Uri.parse(uriString))
                    } else {
                        uriString
                    }
                }

                repository.addClothingItem(
                    name = current.name.trim(),
                    category = current.category,
                    warmthValue = current.warmthValue,
                    imageUri = finalImageUri,
                )
            }.onSuccess {
                state.value = AddItemUiState(saved = true, category = current.category)
            }.onFailure {
                state.value = state.value.copy(isSaving = false, errorMessage = "Save failed")
            }
        }
    }
}
