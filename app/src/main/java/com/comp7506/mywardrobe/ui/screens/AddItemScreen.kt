package com.comp7506.mywardrobe.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.ui.components.GlassCard
import com.comp7506.mywardrobe.ui.theme.UiTokens
import com.comp7506.mywardrobe.ui.viewmodel.AddItemViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory
import com.comp7506.mywardrobe.util.createTempImageUri
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AddItemScreen(navController: NavController) {
    val context = LocalContext.current
    val factory = rememberAppViewModelFactory()
    val vm: AddItemViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()

    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var warmthExpanded by rememberSaveable { mutableStateOf(false) }

    val takePictureLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            val uri = pendingCameraUri
            if (ok && uri != null) {
                vm.setImageUri(uri)
            }
            pendingCameraUri = null
        }

    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            vm.setImageUri(uri?.toString())
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val uri = createTempImageUri(context).toString()
                pendingCameraUri = uri
                takePictureLauncher.launch(Uri.parse(uri))
            }
        }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.popBackStack()
        }
    }

    val categories = listOf("Tops", "Pants", "Outerwear", "Shoes", "Accessories")
    val warmthOptions = listOf(
        0 to "Unknown (0)",
        1 to "Thin T-shirt / Underwear T-shirt / Shorts (1)",
        2 to "Shirt / Long-sleeved undergarment / Pants (2)",
        3 to "Hoodie / Light sweater (3)",
        4 to "Thick sweater / Cotton vest / Wool pants (4)",
        6 to "Trench coat / Light down jacket (6)",
        8 to "Thick down jacket / Woolen coat (8)",
        12 to "Fur coat (12)",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UiTokens.appGradientBrush)
    ) {
        Scaffold(
            topBar = {
                // 2. 这里的 Row 不再设置背景，直接透出底部的渐变
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding() // 只针对顶部状态栏，不包含导航栏
                        .height(56.dp)       // 使用标准 TopAppBar 高度
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f)), // 调低透明度更轻盈
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1C1B1F),
                        )
                    }
                    Text(
                        text = "Add Item",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F),
                    )
                    // 用一个相同大小的 Spacer 来平衡布局，让标题真正居中
                    Spacer(modifier = Modifier.width(40.dp))
                }
            },
            containerColor = Color.Transparent, // 3. 必须设置 Scaffold 透明
            contentWindowInsets = WindowInsets(0.dp),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    // 这里不再设置 background(UiTokens.appGradientBrush)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // 4. 调整这个 Spacer 的高度可以控制 TopBar 与第一个卡片的间距
                Spacer(modifier = Modifier.height(4.dp))


                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    contentPadding = 16.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFA99BCF),
                                    contentColor = Color.White,
                                ),
                            ) {
                                Text("Camera")
                            }
                            Button(
                                onClick = { pickImageLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(99.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFA99BCF),
                                    contentColor = Color.White,
                                ),
                            ) {
                                Text("Gallery")
                            }
                        }

                        if (!state.imageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = state.imageUri,
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    contentPadding = 16.dp,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { vm.setName(it) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Name") },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color(0xFFB7AECF),
                                unfocusedBorderColor = Color(0x80B7AECF),
                            ),
                        )

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                        ) {
                            OutlinedTextField(
                                value = state.category,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color(0xFFB7AECF),
                                    unfocusedBorderColor = Color(0x80B7AECF),
                                ),
                            )
                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false },
                            ) {
                                categories.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c) },
                                        onClick = {
                                            vm.setCategory(c)
                                            categoryExpanded = false
                                        },
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = warmthExpanded,
                            onExpandedChange = { warmthExpanded = !warmthExpanded },
                        ) {
                            val selectedLabel =
                                warmthOptions.firstOrNull { it.first == state.warmthValue }?.second
                                    ?: "Unknown (0)"
                            OutlinedTextField(
                                value = selectedLabel,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                label = { Text("Warmth") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = warmthExpanded) },
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color(0xFFB7AECF),
                                    unfocusedBorderColor = Color(0x80B7AECF),
                                ),
                            )
                            DropdownMenu(
                                expanded = warmthExpanded,
                                onDismissRequest = { warmthExpanded = false },
                            ) {
                                warmthOptions.forEach { (v, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            vm.setWarmthValue(v)
                                            warmthExpanded = false
                                        },
                                    )
                                }
                            }
                        }

                        if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Button(
                            onClick = { vm.save() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSaving,
                            shape = RoundedCornerShape(99.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFA99BCF),
                                contentColor = Color.White,
                            ),
                        ) {
                            Text("Save")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}