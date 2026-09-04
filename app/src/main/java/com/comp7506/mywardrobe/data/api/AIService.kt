package com.comp7506.mywardrobe.data.api

import com.comp7506.mywardrobe.BuildConfig
import retrofit2.Response
import retrofit2.http.*

/**
 * AI试衣API接口 - 适配火山引擎(Volcengine) Ark 平台
 */
interface AIService {

    /**
     * 豆包Doubao-Seedream图像生成（图片模式）
     * 图生图接口
     */
    @Headers("Content-Type: application/json")
    @POST("images/generations")
    suspend fun generateFittingImageWithImages(
        @Body request: DoubaoImageGenRequest
    ): Response<DoubaoImageGenResponse>
}

/**
 * Doubao图像生成请求模型
 */
data class DoubaoImageGenRequest(
    val model: String,
    val prompt: String,
    val image: List<String>? = null,
    val size: String = "1024x1024"
)


/**
 * Doubao图像生成响应模型
 */
data class DoubaoImageGenResponse(
    val code: Int?,
    val message: String?,
    val data: List<DoubaoImageData>?,
    val output: DoubaoImageOutput?,
    val error: DoubaoApiError?
)

data class DoubaoImageData(
    val url: String?
)

data class DoubaoImageOutput(
    val url: String?,
    val b64_json: String?
)

data class DoubaoApiError(
    val message: String?,
    val code: Int?,
    val type: String?
)

/**
 * API配置类
 *
 * 密钥 / 模型终端 ID / 基础地址均不写入源码：构建期通过 BuildConfig 注入，
 * 值来自本地环境变量（DOUBAO_API_KEY / DOUBAO_MODEL_ENDPOINT / DOUBAO_BASE_URL）或
 * 用户级 ~/.gradle/gradle.properties，详见 README.zh-CN.md「配置说明」。
 * Key 与终端 ID 未配置时为空字符串（调用方据此跳过鉴权头并快速失败）；
 * 基础地址未配置时由构建脚本回落到火山引擎北京区 Ark 网关默认值。
 */
object APIConfig {

    /** 豆包图像生成 API 基础地址（构建期注入；Retrofit 要求以 "/" 结尾，此处统一归一化） */
    val doubaoBaseUrl: String = BuildConfig.DOUBAO_BASE_URL
        .trim()
        .let { if (it.isEmpty() || it.endsWith("/")) it else "$it/" }

    /** 豆包图像生成模型终端 ID（构建期注入，未配置时为空字符串） */
    val doubaoModelEndpoint: String = BuildConfig.DOUBAO_MODEL_ENDPOINT

    /** 当前生效的 API Key；未配置时返回 null（调用方据此跳过鉴权头并快速失败） */
    fun getCurrentApiKey(): String? = BuildConfig.DOUBAO_API_KEY.takeIf { it.isNotBlank() }

    fun getCurrentBaseUrl(): String = doubaoBaseUrl

    /** 是否已完成本地配置（Key 与终端 ID 均非空） */
    fun isConfigured(): Boolean =
        BuildConfig.DOUBAO_API_KEY.isNotBlank() && BuildConfig.DOUBAO_MODEL_ENDPOINT.isNotBlank()
}
