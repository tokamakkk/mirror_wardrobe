package com.comp7506.mywardrobe.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.net.Uri
import androidx.core.content.ContextCompat
import com.comp7506.mywardrobe.data.api.*
import com.comp7506.mywardrobe.data.db.ClothingItemEntity
import com.comp7506.mywardrobe.util.compressBitmap
import com.comp7506.mywardrobe.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import android.webkit.MimeTypeMap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * AI试衣数据仓库
 * 负责管理API调用、错误处理和重试机制
 */
class AIRepository(
    private val context: Context,
    private val wardrobeRepository: WardrobeRepository
) {
    companion object {
        private const val TAG = "AIRepository"
        private const val TIMEOUT_SECONDS = 60L
        // 根据报错信息，图片总像素需至少 3,686,400。1440x2560 = 3,686,400
        private const val DEFAULT_SIZE = "1440x2560"
    }

    private fun createRetrofit(): Retrofit {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        clientBuilder.addInterceptor(loggingInterceptor)
        clientBuilder.addInterceptor { chain ->
            val originalRequest = chain.request()
            val apiKey = APIConfig.getCurrentApiKey()
            val newRequest = if (!apiKey.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
            } else {
                originalRequest
            }
            chain.proceed(newRequest)
        }

        return Retrofit.Builder()
            .baseUrl(APIConfig.getCurrentBaseUrl())
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val apiService: AIService by lazy {
        createRetrofit().create(AIService::class.java)
    }

    private fun generateDetailedPrompt(clothingItems: List<ClothingItemEntity>): String {
        val suffix = context.getString(R.string.ai_prompt_suffix)
        if (clothingItems.isEmpty()) {
            return context.getString(R.string.ai_prompt_default, suffix)
        }
        val itemCategories = clothingItems.map { it.category }
        val description = if (itemCategories.size == 1) {
            itemCategories[0]
        } else {
            val last = itemCategories.last()
            val initial = itemCategories.dropLast(1).joinToString(", ")
            "$initial${context.getString(R.string.ai_prompt_item_joiner)}$last"
        }
        return context.getString(R.string.ai_prompt_template, description, suffix)
    }

    /**
     * 缓存图片到本地
     */
    private suspend fun cacheImage(imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val connection = java.net.URL(imageUrl).openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                val inputStream = connection.getInputStream()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val compressedBitmap = compressBitmap(bitmap, maxQuality = 85)
                    val file = File(context.cacheDir, "ai_fitting_${System.currentTimeMillis()}.jpg")
                    file.outputStream().use { outputStream ->
                        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                    }
                    file.absolutePath
                } else imageUrl
            } catch (e: Exception) {
                imageUrl
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String? {
        return try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            "data:image/png;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 纯文本模式生成图片 (仅用于降级)
     */
    suspend fun generateFittingImage(
        clothingIds: List<Long>,
        userId: String,
        imageFile: File? = null
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            if (!APIConfig.isConfigured()) {
                Log.e(TAG, "豆包 API 未配置：请先设置 DOUBAO_API_KEY / DOUBAO_MODEL_ENDPOINT 后重新构建")
                return@withContext Result.failure(
                    Exception("Doubao API is not configured. Set DOUBAO_API_KEY / DOUBAO_MODEL_ENDPOINT and rebuild.")
                )
            }
            try {
                val clothingItems = wardrobeRepository.getClothingItemsByIds(clothingIds)
                if (clothingItems.isEmpty()) return@withContext Result.failure(Exception("No clothing items found"))

                val response = apiService.generateFittingImageWithImages(
                    request = DoubaoImageGenRequest(
                        model = APIConfig.doubaoModelEndpoint,
                        prompt = generateDetailedPrompt(clothingItems),
                        size = DEFAULT_SIZE
                    )
                )
                handleResponse(response)
            } catch (e: Exception) {
                Log.e(TAG, "Error in generateFittingImage", e)
                Result.failure(e)
            }
        }
    }

    /**
     * 使用图片上传生成试衣图片 (图生图)
     */
    suspend fun generateFittingImageWithImages(
        clothingIds: List<Long>,
        userId: String,
        baseImageFile: File? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!APIConfig.isConfigured()) {
            Log.e(TAG, "豆包 API 未配置：请先设置 DOUBAO_API_KEY / DOUBAO_MODEL_ENDPOINT 后重新构建")
            return@withContext Result.failure(
                Exception("Doubao API is not configured. Set DOUBAO_API_KEY / DOUBAO_MODEL_ENDPOINT and rebuild.")
            )
        }
        try {
            // --------------------------
            // 1. 获取衣服数据
            // --------------------------
            Log.d(TAG, "Step 1: Fetching clothing items for IDs: $clothingIds")
            val clothingItems = wardrobeRepository.getClothingItemsByIds(clothingIds)
            Log.d(TAG, "Fetched ${clothingItems.size} clothing items")
            if (clothingItems.isEmpty()) {
                return@withContext Result.failure(Exception("No clothing items found"))
            }

            // --------------------------
            // 2. 准备模特底图（统一逻辑）
            // --------------------------
            // 优先用传入的 baseImageFile，没有则用默认图
            val modelBitmap: Bitmap = baseImageFile?.let { file ->
                Log.d(TAG, "Using provided base image: ${file.absolutePath}")
                BitmapFactory.decodeFile(file.absolutePath)
            } ?: loadDefaultShapeBitmap() ?: return@withContext Result.failure(Exception("Failed to load model image"))

            Log.d(TAG, "Model bitmap loaded successfully: ${modelBitmap.width}x${modelBitmap.height}")

            // 把模特 Bitmap 转成 Base64（只转一次！）
            val modelBase64 = bitmapToBase64(modelBitmap) ?: return@withContext Result.failure(Exception("Failed to encode model image"))

            // 手动释放 Bitmap，避免内存泄漏（可选但推荐）
            modelBitmap.recycle()

            // --------------------------
            // 3. 准备衣服图片
            // --------------------------
            Log.d(TAG, "Step 3: Preparing clothing images")
            val clothingImageFiles = mutableListOf<File>()
            for (item in clothingItems) {
                Log.d(TAG, "Processing item: ${item.id} - ${item.name}, imageUri: ${item.imageUri}")
                item.imageUri?.let { uri ->
                    val file = prepareImageForUpload(uri)
                    if (file != null) {
                        clothingImageFiles.add(file)
                        Log.d(TAG, "Successfully prepared clothing image: ${file.absolutePath}")
                    } else {
                        Log.w(TAG, "Failed to prepare clothing image for item ${item.id}")
                    }
                } ?: Log.w(TAG, "Item ${item.id} has no imageUri")
            }
            Log.d(TAG, "Total clothing image files prepared: ${clothingImageFiles.size}")
            if (clothingImageFiles.isEmpty()) {
                return@withContext Result.failure(Exception("No clothing images provided"))
            }

            // 把衣服图片转成 Base64
            val clothingBase64 = encodeImageToBase64(clothingImageFiles[0])
                ?: return@withContext Result.failure(Exception("Failed to encode clothing image"))

            // --------------------------
            // 4. 构建请求参数
            // --------------------------
            // 确保 image 列表顺序：[衣服, 模特]，和 prompt 对应
            val imageList = listOf(clothingBase64, modelBase64)

            Log.d(TAG, "Sending Image-to-Image request. Size: $DEFAULT_SIZE, Image count: ${imageList.size}")
            Log.d(TAG, "Clothing Base64 Prefix: ${clothingBase64.take(100)}")
            Log.d(TAG, "Model Base64 Prefix: ${modelBase64.take(100)}")

            // --------------------------
            // 5. 发起 API 请求
            // --------------------------
            val response = apiService.generateFittingImageWithImages(
                request = DoubaoImageGenRequest(
                    model = APIConfig.doubaoModelEndpoint,
                    prompt = generateDetailedPrompt(clothingItems),
                    size = DEFAULT_SIZE,
                    image = imageList
                )
            )

            handleResponse(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateFittingImageWithImages", e)
            Result.failure(e)
        }
    }

    private suspend fun handleResponse(response: retrofit2.Response<DoubaoImageGenResponse>): Result<String> {
        if (response.isSuccessful) {
            val body = response.body()
            Log.d(TAG, "Doubao Response: $body")

            val imageUrl = body?.data?.firstOrNull()?.url
                ?: body?.output?.url
                ?: body?.output?.b64_json?.let { saveBase64Image(it) }

            if (!imageUrl.isNullOrEmpty()) {
                return Result.success(cacheImage(imageUrl))
            }

            val errorMessage = body?.error?.message
                ?: body?.message
                ?: when (body?.code) {
                    400 -> context.getString(R.string.ai_error_bad_request)
                    401 -> context.getString(R.string.ai_error_unauthorized)
                    429 -> context.getString(R.string.ai_error_rate_limited)
                    else -> context.getString(R.string.ai_error_api_generic)
                }
            return Result.failure(Exception(errorMessage))
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e(TAG, "HTTP error ${response.code()}: $errorBody")
            return Result.failure(Exception("HTTP error ${response.code()}"))
        }
    }

    private suspend fun encodeImageToBase64(file: File): String? = withContext(Dispatchers.IO) {
        try {
            // 1. 读取文件字节数组
            val bytes = file.readBytes()

            // 2. 生成无换行的Base64字符串
            val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)

            // 3. 自动识别图片格式（根据文件扩展名）
            val extension = file.extension.lowercase()
            val mimeType = when (extension) {
                "jpg", "jpeg" -> "jpeg"
                "png" -> "png"
                "webp" -> "webp"
                else -> {
                    // 如果无法识别，尝试通过MimeTypeMap获取
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                        ?.substringAfter("image/") ?: "png" // 默认回退到png
                }
            }

            // 4. 拼接完整格式前缀并返回
            "data:image/$mimeType;base64,$base64String"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun saveBase64Image(base64Data: String): String? = withContext(Dispatchers.IO) {
        try {
            val imageData = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            if (bitmap != null) {
                val compressedBitmap = compressBitmap(bitmap, maxQuality = 85)
                val file = File(context.cacheDir, "ai_fitting_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                file.absolutePath
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error saving base64 image", e)
            null
        }
    }

    suspend fun prepareImageForUpload(uri: String): File? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "prepareImageForUpload: Processing URI: $uri")

            // 尝试处理不同类型的 URI
            val inputStream = try {
                context.contentResolver.openInputStream(Uri.parse(uri))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open input stream for URI: $uri", e)
                // 如果 contentResolver 失败，尝试作为文件路径处理
                try {
                    java.io.FileInputStream(uri)
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to open file: $uri", e2)
                    return@withContext null
                }
            }

            if (inputStream == null) {
                Log.e(TAG, "Input stream is null for URI: $uri")
                return@withContext null
            }

            Log.d(TAG, "Input stream opened successfully, decoding bitmap...")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap != null) {
                Log.d(TAG, "Bitmap decoded successfully: ${bitmap.width}x${bitmap.height}")
                val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    val success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    Log.d(TAG, "Bitmap compress result: $success, file saved: ${file.absolutePath}")
                }
                file
            } else {
                Log.e(TAG, "BitmapFactory.decodeStream returned null for URI: $uri")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in prepareImageForUpload for URI: $uri", e)
            null
        }
    }

    /**
     * 加载默认身形图片并返回其文件
     */
    suspend fun loadDefaultShapeImage(): File? = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadDefaultShapeBitmap()
            if (bitmap != null) {
                val file = File(context.cacheDir, "default_shape.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                file
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error loading default shape image", e)
            null
        }
    }

    /**
     * 加载默认身形图片 (defaultshape.png) 并保存为 PNG 文件
     */
    private fun loadDefaultShapeBitmap(): Bitmap? {
        // R.drawable.defaultshape 是你的资源ID
        return BitmapFactory.decodeResource(context.resources, R.drawable.defaultshape  )
    }
}
