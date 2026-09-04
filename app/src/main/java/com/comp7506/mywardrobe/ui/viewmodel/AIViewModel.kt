package com.comp7506.mywardrobe.ui.viewmodel

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.comp7506.mywardrobe.MyWardrobeApplication
import com.comp7506.mywardrobe.R
import com.comp7506.mywardrobe.data.repository.AIRepository
import com.comp7506.mywardrobe.data.repository.WardrobeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream
import java.security.MessageDigest

/**
 * AI试衣功能ViewModel
 * 管理AI试衣的状态、数据加载和错误处理
 */
class AIViewModel(application: Application) : AndroidViewModel(application) {

    private val wardrobeRepository: WardrobeRepository = (application as MyWardrobeApplication).repository
    private val aiRepository: AIRepository = AIRepository(application, wardrobeRepository)

    companion object {
        private const val TAG = "AIViewModel"
        private const val CACHE_DIR = "ai_fitting"
    }

    // UI状态
    private val _loadingState = MutableStateFlow(false)
    val loadingState: StateFlow<Boolean> = _loadingState.asStateFlow()

    private val _resultImage = MutableStateFlow<String?>(null)
    val resultImage: StateFlow<String?> = _resultImage.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _baseImageFile = MutableStateFlow<File?>(null)
    val baseImageFile: StateFlow<File?> = _baseImageFile.asStateFlow()

    private val _useImageUpload = MutableStateFlow(true)
    val useImageUpload: StateFlow<Boolean> = _useImageUpload.asStateFlow()

    // 图片缓存管理
    private val imageCache = mutableMapOf<String, String>()

    init {
        // 初始化时加载默认模特图片，确保图生图模式可用
        loadDefaultShapeImage()
    }

    /**
     * 生成试衣图片
     */
    fun generateFittingImage(
        clothingIds: List<Long>,
        userId: String,
        imageFile: File? = null,
        forceTextOnly: Boolean = false
    ) {
        Log.d(TAG, "AIViewModel.generateFittingImage called. Items: ${clothingIds.size}")

        viewModelScope.launch {
            _loadingState.value = true
            _error.value = null

            try {
                val baseImage = imageFile ?: _baseImageFile.value
                val shouldUseImageUpload = !forceTextOnly && _useImageUpload.value && baseImage != null

                Log.d(TAG, "shouldUseImageUpload check:")
                Log.d(TAG, "  forceTextOnly: $forceTextOnly")
                Log.d(TAG, "  _useImageUpload.value: ${_useImageUpload.value}")
                Log.d(TAG, "  baseImage: ${baseImage != null}")
                Log.d(TAG, "  shouldUseImageUpload: $shouldUseImageUpload")

                if (shouldUseImageUpload) {
                    Log.d(TAG, "Using image-to-image mode")
                    aiRepository.generateFittingImageWithImages(
                        clothingIds = clothingIds,
                        userId = userId,
                        baseImageFile = baseImage!!
                    )
                } else {
                    Log.d(TAG, "Using text-only mode (fallback or forced)")
                    aiRepository.generateFittingImage(
                        clothingIds = clothingIds,
                        userId = userId,
                        imageFile = null
                    )
                }.onSuccess { imageUrl ->
                    Log.d(TAG, "AI image generated: $imageUrl")
                    val cacheKey = generateCacheKey(clothingIds, userId)
                    imageCache[cacheKey] = imageUrl
                    _resultImage.value = imageUrl
                }.onFailure { exception ->
                    Log.e(TAG, "Failed to generate image", exception)
                    _error.value = exception.message ?: getApplication<Application>().getString(R.string.ai_error_generate_failed)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in generateFittingImage", e)
                _error.value = e.message ?: getApplication<Application>().getString(R.string.ai_error_generate_failed)
            } finally {
                _loadingState.value = false
            }
        }
    }

    /**
     * 加载默认身形图片
     */
    fun loadDefaultShapeImage() {
        viewModelScope.launch {
            try {
                val file = aiRepository.loadDefaultShapeImage()
                if (file != null) {
                    _baseImageFile.value = file
                    Log.d(TAG, "Default shape image loaded: ${file.absolutePath}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading default shape image", e)
            }
        }
    }

    /**
     * 设置基础图片
     */
    fun setBaseImage(file: File?) {
        _baseImageFile.value = file
    }

    /**
     * 从URI设置基础图片
     */
    suspend fun setBaseImageFromUri(uri: String) {
        try {
            val file = aiRepository.prepareImageForUpload(uri)
            if (file != null) {
                _baseImageFile.value = file
                _useImageUpload.value = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting base image from URI", e)
        }
    }

    /**
     * 切换图片上传模式
     */
    fun setUseImageUpload(use: Boolean) {
        _useImageUpload.value = use
    }

    /**
     * 清除结果
     */
    fun clearResult() {
        _resultImage.value = null
        _error.value = null
    }

    /**
     * 保存图片到相册
     */
    fun saveImageToGallery(imagePath: String?): Boolean {
        if (imagePath == null) return false
        return try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            bitmap?.let { saveBitmapToGallery(it) } ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 清除缓存
     */
    fun clearCache() {
        viewModelScope.launch {
            try {
                val cacheDir = File(getApplication<Application>().cacheDir, CACHE_DIR)
                if (cacheDir.exists()) {
                    cacheDir.listFiles()?.forEach { it.delete() }
                }
                imageCache.clear()
                _resultImage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache", e)
            }
        }
    }

    /**
     * 从缓存获取图片
     */
    fun getCachedImage(clothingIds: List<Long>, userId: String): String? {
        val cacheKey = generateCacheKey(clothingIds, userId)
        return imageCache[cacheKey]
    }

    private fun generateCacheKey(clothingIds: List<Long>, userId: String): String {
        val key = "${clothingIds.sorted().joinToString("-")}-$userId"
        return MessageDigest.getInstance("MD5")
            .digest(key.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "ai_fitting_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyWardrobe")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = getApplication<Application>().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
            )
            uri?.let {
                val outputStream: OutputStream? = getApplication<Application>().contentResolver.openOutputStream(it)
                outputStream?.use { stream -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream) }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                getApplication<Application>().contentResolver.update(it, contentValues, null, null)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
}
