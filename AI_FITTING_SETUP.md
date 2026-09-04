# AI试衣功能配置指南

## 问题说明

智谱AI（glm-4.7）的 `api/paas/v4/images/generations` API只支持**文本输入**，不支持：
- 上传 defaultshape.png 作为基础身形
- 上传衣服图片进行叠加
- 图片到图片的处理

因此生成的图片与预期不同的原因是API无法看到参考图片。

## 解决方案

### 方案1：使用支持图片输入的AI服务

推荐以下支持图片叠加的AI服务：

#### 1. Replicate AI
- **支持**: 图片输入、ControlNet、图片叠加
- **API**: https://replicate.com/
- **文档**: https://replicate.com/docs

#### 2. Stability AI
- **支持**: 图片到图片处理、ControlNet
- **API**: https://api.stability.ai/
- **文档**: https://platform.stability.ai/docs

#### 3. Hugging Face
- **支持**: 多种图像模型、ControlNet
- **API**: https://api-inference.huggingface.co/
- **文档**: https://huggingface.co/docs/api-inference

### 方案2：本地搭建AI服务

如果您有本地GPU资源，可以搭建：
- 自定义的Stable Diffusion服务
- ControlNet或Inpaint模型
- 支持图片输入和身形参考

## 配置步骤

### 步骤1：获取API密钥

1. 注册对应的AI服务平台
2. 生成API密钥
3. 验证密钥权限

### 步骤2：修改代码配置

在 `AIService.kt` 中的 `APIConfig` 对象中修改：

```kotlin
object APIConfig {
    // 切换到支持图片输入的服务
    var currentProvider: String = "replicate"  // 或 "stability"
    
    val apiKeys = mutableMapOf<String, String>(
        "replicate" to "your-replicate-api-key",
        "stability" to "your-stability-api-key",
    )
    
    val baseUrls = mutableMapOf(
        "replicate" to "https://api.replicate.com/",
        "stability" to "https://api.stability.ai/",
    )
}
```

### 步骤3：修改API接口

根据选择的AI服务，需要修改 `AIService` 接口和 `AIRepository` 实现。

#### Replicate AI 示例

```kotlin
interface AIService {
    @POST("predictions")
    suspend fun generateFittingImage(
        @Body request: ReplicateRequest
    ): retrofit2.Response<ReplicateResponse>
}

data class ReplicateRequest(
    val version: String = "your-model-version",
    val input: ReplicateInput
)

data class ReplicateInput(
    val image: String?,      // defaultshape.png
    val prompt: String,
    val clothing_image: String?  // 衣服图片
)
```

#### Stability AI 示例

```kotlin
interface AIService {
    @POST("v2beta/stable-image/generate")
    suspend fun generateFittingImage(
        @Body request: StabilityRequest
    ): retrofit2.Response<StabilityResponse>
}

data class StabilityRequest(
    val text_prompts: List<String>,
    val init_image: String?,  // defaultshape.png
    val image_strength: Float = 0.35,
    val cfg_scale: Float = 7.0
)
```

## 推荐方案

### 最简单方案：Replicate AI

Replicate提供很多预训练的虚拟试衣模型：
- ControlNet for Virtual Try-On
- SDXL 模型
- 快速部署和集成

### 开源替代方案

如果您愿意自建服务，可以考虑：
1. **OOTDiffusion**: 开源的虚拟试衣项目
   - GitHub: https://github.com/ironjr/oodiffusion
   - 基于 Stable Diffusion，专注于虚拟试衣

2. **OOTDiffusion-compat**: 兼容版本
   - 支持更多模型
   - 更好的ControlNet支持

## 提示

- ✅ 智谱AI适合：快速生成文本到图片，但无法实现真正的试衣效果
- ✅ Replicate/Stability：适合：需要图片叠加、身形参考的场景
- ✅ 本地部署：适合：需要完全控制、高度定制的场景

## 下一步

1. 选择一个支持图片输入的AI服务
2. 获取API密钥
3. 修改代码以适配新服务的API格式
4. 测试API调用和图片生成效果
