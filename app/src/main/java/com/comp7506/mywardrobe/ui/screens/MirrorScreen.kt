package com.comp7506.mywardrobe.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.comp7506.mywardrobe.ui.components.GlassCard
import com.comp7506.mywardrobe.ui.viewmodel.AIViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.MyWardrobeApplication
import com.comp7506.mywardrobe.R
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.ui.viewmodel.AppViewModelFactory
import com.comp7506.mywardrobe.ui.viewmodel.WardrobeViewModel

@Composable
fun MirrorScreen(navController: NavController) {
    val app = LocalContext.current.applicationContext as MyWardrobeApplication
    val factory = remember { AppViewModelFactory(app, app.repository, app.locationProvider, app.weatherRepository) }
    val vm: WardrobeViewModel = viewModel(factory = factory)
    val aiViewModel: AIViewModel = viewModel(factory = factory)

    val itemsList by vm.items.collectAsStateWithLifecycle()
    val baseImageFile by aiViewModel.baseImageFile.collectAsStateWithLifecycle()
    val selectedItems = remember { mutableStateListOf<Long>() }
    val context = LocalContext.current
    val useImageUpload by aiViewModel.useImageUpload.collectAsState()

    LaunchedEffect(Unit) {
        aiViewModel.loadDefaultShapeImage()

        // 重置为默认形象模式（图生图），确保每次进入 MirrorScreen 都是默认状态
        aiViewModel.setUseImageUpload(true)
        android.util.Log.d("MirrorScreen", "Reset useImageUpload to true (default shape mode)")

        val savedPhotoUri = navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selectedPortraitUri")

        if (savedPhotoUri != null) {
            aiViewModel.setBaseImageFromUri(savedPhotoUri)
            navController.previousBackStackEntry?.savedStateHandle?.remove<String>("selectedPortraitUri")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (baseImageFile != null) {
            AsyncImage(
                model = baseImageFile,
                contentDescription = "Mirror Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.defaultshape),
                contentDescription = "Default Portrait",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.08f)),
        )

        IconButton(
            onClick = { navController.navigateUp() },
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
                contentDescription = "Back",
                tint = Color(0xFF1C1B1F),
            )
        }

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 12.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x66FFFFFF))
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TextButton(
                onClick = { aiViewModel.setUseImageUpload(true) },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (useImageUpload) Color(0x66FFFFFF) else Color.Transparent),
            ) {
                Text(
                    stringResource(R.string.mirror_mode_default_avatar),
                    color = Color(0xFF1C1B1F),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            TextButton(
                onClick = {
                    aiViewModel.setUseImageUpload(false)
                    navController.navigate(AppRoutes.PortraitCapture.route)
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (!useImageUpload) Color(0x66FFFFFF) else Color.Transparent),
            ) {
                Text(
                    stringResource(R.string.mirror_mode_my_photo),
                    color = Color(0xFF1C1B1F),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        TextButton(
            onClick = {
                if (selectedItems.isNotEmpty()) {
                    val selectedIds = selectedItems.toList()
                    val route = if (useImageUpload) {
                        AppRoutes.MirrorLoading.passClothingIdsAndMode(selectedIds, true)
                    } else {
                        AppRoutes.MirrorLoading.passClothingIdsAndMode(selectedIds, false)
                    }
                    navController.navigate(route)
                } else {
                    Toast.makeText(context, "Please select at least one item to try on", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .statusBarsPadding()
                .padding(12.dp)
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x66FFFFFF))
                .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
        ) {
            Text("Next", style = MaterialTheme.typography.titleMedium, color = Color(0xFF1C1B1F))
        }

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
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFFBDBDBD)),
                )
                Text(
                    "Wardrobe Items",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF1C1B1F),
                    modifier = Modifier.padding(top = 6.dp, bottom = 6.dp),
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp),
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.4f))
                                .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                .clickable { navController.navigate(AppRoutes.PortraitCapture.route) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Open Camera",
                                tint = Color(0xFF1C1B1F),
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                    items(itemsList) { item ->
                        val isSelected = selectedItems.contains(item.id)
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                                .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                .clickable {
                                    if (isSelected) selectedItems.remove(item.id) else selectedItems.add(item.id)
                                },
                        ) {
                            AsyncImage(
                                model = item.imageUri?.let { Uri.parse(it) },
                                contentDescription = item.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0x337C4DFF)),
                                )
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF7C4DFF),
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopStart),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
