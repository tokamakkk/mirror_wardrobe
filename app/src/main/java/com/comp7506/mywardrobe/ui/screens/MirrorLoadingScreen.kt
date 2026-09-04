package com.comp7506.mywardrobe.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.comp7506.mywardrobe.MyWardrobeApplication
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.ui.viewmodel.AIViewModel
import com.comp7506.mywardrobe.ui.viewmodel.AppViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun MirrorLoadingScreen(navController: NavController) {
    val context = LocalContext.current
    val alpha = remember { Animatable(0f) }
    val app = LocalContext.current.applicationContext as MyWardrobeApplication
    val factory = remember { AppViewModelFactory(app, app.repository, app.locationProvider, app.weatherRepository) }
    val aiViewModel: AIViewModel = viewModel(factory = factory)

    // 使用 collectAsState 监听 StateFlow
    val loadingState by aiViewModel.loadingState.collectAsState()
    val resultImage by aiViewModel.resultImage.collectAsState()
    val error by aiViewModel.error.collectAsState()
    val baseImageFile by aiViewModel.baseImageFile.collectAsState()

    // 获取当前用户ID
    val userId = remember {
        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    }

    // 获取导航参数
    val clothingIdsString = navController.currentBackStackEntry
        ?.arguments
        ?.getString("clothingIds")

    // 强制使用图生图模式，忽略导航参数（避免参数传递问题）
    val useImageUpload = true

    // 单个LaunchedEffect处理所有逻辑
    LaunchedEffect(Unit) {
        android.util.Log.d("MirrorLoadingScreen", "=== Screen created ===")
        android.util.Log.d("MirrorLoadingScreen", "UserId: $userId")
        android.util.Log.d("MirrorLoadingScreen", "Raw clothing IDs string: $clothingIdsString")
        android.util.Log.d("MirrorLoadingScreen", "Forcing image-to-image mode (useImageUpload=true)")

        // 开始淡入动画
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000)
        )

        // 确保默认图片已加载（用于图生图模式）
        aiViewModel.loadDefaultShapeImage()
    }

    // 监听默认图片加载状态，加载完成后调用生成方法
    LaunchedEffect(baseImageFile) {
        android.util.Log.d("MirrorLoadingScreen", "baseImageFile changed: ${baseImageFile != null}")

        // 确保图片已加载且不在加载中
        if (baseImageFile != null && !loadingState) {
            // 获取和解析参数
            val selectedClothingIds = clothingIdsString
                ?.split(",")
                ?.mapNotNull { it.trim().toLongOrNull() }
                ?: emptyList()

            android.util.Log.d("MirrorLoadingScreen", "Parsed clothing IDs: $selectedClothingIds")

            // 调用AI生成试衣图片，强制使用图生图模式
            if (selectedClothingIds.isNotEmpty()) {
                android.util.Log.d("MirrorLoadingScreen", "Calling generateFittingImage with forceTextOnly=false...")
                android.util.Log.d("MirrorLoadingScreen", "baseImageFile available: true")
                aiViewModel.generateFittingImage(
                    clothingIds = selectedClothingIds,
                    userId = userId,
                    forceTextOnly = false  // 强制使用图生图模式
                )
            } else {
                android.util.Log.e("MirrorLoadingScreen", "No clothing IDs selected, going back")
                navController.popBackStack()
            }
        }
    }

    // 监听AI结果和错误
    LaunchedEffect(resultImage, loadingState) {
        android.util.Log.d("MirrorLoadingScreen", "=== State changed ===")
        android.util.Log.d("MirrorLoadingScreen", "Loading state: $loadingState")
        android.util.Log.d("MirrorLoadingScreen", "Result image: $resultImage")

        // 当加载完成且有结果图片时，导航到结果页
        if (!loadingState) {
            resultImage?.let { imageUrl ->
                android.util.Log.d("MirrorLoadingScreen", "Navigating to AiResult with image: $imageUrl")

                val clothingIdsParam = clothingIdsString ?: ""
                
                // 使用在 AppRoutes 中新定义的 route 辅助函数，确保路径格式正确
                val route = AppRoutes.AiResult.route(imageUrl, clothingIdsParam)
                android.util.Log.d("MirrorLoadingScreen", "Final route: $route")

                navController.navigate(route) {
                    popUpTo(AppRoutes.MirrorLoading.route) { inclusive = true }
                }
            }
        }
    }

    // 监听错误
    LaunchedEffect(error) {
        error?.let { err ->
            android.util.Log.e("MirrorLoadingScreen", "Error: $err")
            android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F1A),
                        Color(0xFF000000)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Text(
                text = "Mirror, mirror, on the wall,\nWho in this land is fairest of all?",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = Color(0xFFBB86FC), // Light Purple
                trackColor = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}
