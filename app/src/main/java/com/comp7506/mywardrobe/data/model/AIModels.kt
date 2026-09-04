package com.comp7506.mywardrobe.data.model

/**
 * AI试衣请求模型
 */
data class FittingRequest(
    val clothingIds: List<Long>,
    val userId: String,
    val provider: String = "default",
    val apiKey: String? = null
)

/**
 * AI试衣响应模型
 */
data class AIResponse(
    val success: Boolean,
    val imageUrl: String?,
    val message: String,
    val provider: String? = null,
    val processingTime: Long? = null
)

/**
 * 模型列表响应
 */
data class ModelListResponse(
    val success: Boolean,
    val models: List<AIModel>,
    val message: String? = null
)

/**
 * AI模型信息
 */
data class AIModel(
    val id: String,
    val name: String,
    val description: String,
    val provider: String,
    val isAvailable: Boolean,
    val maxRetries: Int = 3
)

/**
 * 模型切换响应
 */
data class ModelSwitchResponse(
    val success: Boolean,
    val currentModel: String,
    val message: String? = null
)
