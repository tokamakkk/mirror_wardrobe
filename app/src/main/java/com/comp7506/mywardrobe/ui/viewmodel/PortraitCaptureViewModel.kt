package com.comp7506.mywardrobe.ui.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PortraitCaptureViewModel : ViewModel() {
    private val _selectedImageUri = MutableStateFlow<String?>(null)
    val selectedImageUri: StateFlow<String?> = _selectedImageUri.asStateFlow()

    private val _galleryThumbnails = MutableStateFlow<List<String>>(emptyList())
    val galleryThumbnails: StateFlow<List<String>> = _galleryThumbnails.asStateFlow()

    fun selectImage(uri: String) {
        _selectedImageUri.value = uri
    }

    fun loadGalleryImages(context: Context) {
        viewModelScope.launch {
            val projections = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN)
            
            // For API 26+, use Bundle to specify LIMIT and SORT
            val queryArgs = Bundle().apply {
                putInt(ContentResolver.QUERY_ARG_LIMIT, 20)
                putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.Images.Media.DATE_TAKEN)
                )
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                )
            }

            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projections,
                queryArgs,
                null
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val uris = mutableListOf<String>()
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val uri = "content://media/external/images/media/$id"
                    uris.add(uri)
                }
                _galleryThumbnails.value = uris
            }
        }
    }
}