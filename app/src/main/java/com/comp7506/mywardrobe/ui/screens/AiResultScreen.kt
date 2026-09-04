package com.comp7506.mywardrobe.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.comp7506.mywardrobe.R
import com.comp7506.mywardrobe.data.db.ClothingItemEntity
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.ui.components.GlassCard
import com.comp7506.mywardrobe.ui.viewmodel.AIViewModel
import com.comp7506.mywardrobe.ui.viewmodel.WardrobeViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory
import kotlinx.coroutines.launch

/**
 * AI 试衣结果展示页面（视觉与 [MirrorScreen] 统一：全屏底图、轻遮罩、磨砂底栏）。
 */
@Composable
fun AiResultScreen(
    navController: NavController,
    imageUrl: String?,
    selectedClothingIds: List<Long>,
) {
    val factory = rememberAppViewModelFactory()
    val aiViewModel: AIViewModel = viewModel()
    val wardrobeViewModel: WardrobeViewModel = viewModel(factory = factory)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val loadingState by aiViewModel.loadingState.collectAsStateWithLifecycle()
    val error by aiViewModel.error.collectAsStateWithLifecycle()

    val selectedItems by wardrobeViewModel.items.collectAsStateWithLifecycle()
    val selectedClothingItems = selectedItems.filter { it.id in selectedClothingIds }

    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!loadingState && imageUrl != null) {
            aiViewModel.clearResult()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !loadingState && error == null && imageUrl != null -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.ai_result_generated_image_desc),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    error = painterResource(id = android.R.drawable.ic_menu_report_image),
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1A1A22)),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.08f)),
        )

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .size(44.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.45f)),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.ai_result_back),
                tint = Color(0xFF1C1B1F),
            )
        }

        when {
            loadingState -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xE6FFFFFF))
                            .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 28.dp, vertical = 24.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color(0xFF7C4DFF),
                            strokeWidth = 3.dp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.ai_result_loading),
                            color = Color(0xFF1C1B1F),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .width(200.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = Color(0xFF7C4DFF),
                            trackColor = Color(0x331C1B1F),
                        )
                    }
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        cornerRadius = 24.dp,
                        contentPadding = 20.dp,
                        shadowAlpha = 0.03f,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.ai_result_failed),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFB3261E),
                            )
                            Text(
                                text = error ?: stringResource(R.string.ai_result_unknown_error),
                                color = Color(0xFF5C5672),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                            )
                            TextButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0x66FFFFFF))
                                    .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
                            ) {
                                Text(
                                    stringResource(R.string.ai_result_back),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF1C1B1F),
                                )
                            }
                        }
                    }
                }
            }
            imageUrl != null -> {
                GlassCard(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 12.dp, end = 12.dp, bottom = 68.dp),
                    cornerRadius = 24.dp,
                    contentPadding = 10.dp,
                    shadowAlpha = 0.03f,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .size(width = 32.dp, height = 4.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(Color(0xFFBDBDBD)),
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            MirrorStyleTextButton(
                                text = stringResource(R.string.ai_result_save),
                                onClick = {
                                    isSaving = true
                                    scope.launch {
                                        aiViewModel.saveImageToGallery(imageUrl)
                                        isSaving = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isSaving,
                                leading = {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = Color(0xFF1C1B1F),
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Download,
                                            contentDescription = null,
                                            tint = Color(0xFF1C1B1F),
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                },
                            )
                            MirrorStyleTextButton(
                                text = stringResource(R.string.ai_result_share),
                                onClick = { shareImage(context, imageUrl) },
                                modifier = Modifier.weight(1f),
                                leading = {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color(0xFF1C1B1F),
                                        modifier = Modifier.size(18.dp),
                                    )
                                },
                            )
                            MirrorStyleTextButton(
                                text = stringResource(R.string.ai_result_regenerate),
                                onClick = {
                                    navController.popBackStack(AppRoutes.Mirror.route, false)
                                },
                                modifier = Modifier.weight(1f),
                                leading = null,
                            )
                        }
                        if (selectedClothingItems.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.ai_result_selected_items),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF1C1B1F),
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 2.dp),
                            ) {
                                items(selectedClothingItems, key = { it.id }) { item ->
                                    ClothingItemThumb(item)
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.ai_result_empty),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x66FFFFFF))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        color = Color(0xFF1C1B1F),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun MirrorStyleTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)?,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x66FFFFFF))
            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF1C1B1F),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ClothingItemThumb(item: ClothingItemEntity) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.35f))
            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
    ) {
        AsyncImage(
            model = item.imageUri,
            contentDescription = item.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            error = painterResource(id = android.R.drawable.ic_menu_report_image),
        )
    }
}

private fun shareImage(context: android.content.Context, imageUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(imageUrl))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.ai_result_share_chooser_title)))
    } catch (_: Exception) {
    }
}
