package com.comp7506.mywardrobe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.ui.components.ClothingItemGridCard
import com.comp7506.mywardrobe.ui.theme.UiTokens
import com.comp7506.mywardrobe.ui.viewmodel.WardrobeViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory

@Composable
fun WardrobeScreen(navController: NavController) {
    val factory = rememberAppViewModelFactory()
    val vm: WardrobeViewModel = viewModel(factory = factory)
    val query by vm.currentQuery.collectAsStateWithLifecycle()
    val category by vm.currentCategory.collectAsStateWithLifecycle()
    val itemsList by vm.items.collectAsStateWithLifecycle()
    var itemIdToDelete by remember { mutableStateOf<Long?>(null) }

    val categories = listOf(
        null to "All",
        "Tops" to "Tops",
        "Pants" to "Pants",
        "Outerwear" to "Outerwear",
        "Shoes" to "Shoes",
        "Accessories" to "Accessories",
    )

    // 使用 Box 包裹以应用全屏背景，包括状态栏
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UiTokens.appGradientBrush)
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(AppRoutes.AddItem.route) },
                    // 调整 FAB 位置，使其在视觉上位于底栏上方
                    modifier = Modifier.padding(bottom = UiTokens.wardrobeBottomContentPadding),
                    containerColor = Color(0x66FFFFFF),
                    contentColor = Color(0xFF1C1B1F),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Item")
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            containerColor = Color.Transparent // 让 Scaffold 透明，露出底部的 Box 背景
        ) { _ -> // 忽略 Scaffold 的 paddingValues 以实现全屏布局
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding() // 确保内容不被状态栏遮挡
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp)) // 减小顶部留白

                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.setQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search items…") },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = Color(0x80000000),
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0x66FFFFFF),
                        unfocusedContainerColor = Color(0x66FFFFFF),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedPlaceholderColor = Color(0x80000000),
                        unfocusedPlaceholderColor = Color(0x80000000),
                    ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    categories.forEach { (key, label) ->
                        val selected = category == key
                        Text(
                            text = label,
                            fontSize = 18.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Light,
                            color = if (selected) Color.Black else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { vm.setCategory(key) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.wardrobeGridSpacing),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.wardrobeGridSpacing),
                    // 使用底栏高度作为 contentPadding，确保列表最后的内容不被遮挡
                    contentPadding = PaddingValues(bottom = UiTokens.wardrobeBottomContentPadding),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(itemsList, key = { it.id }) { item ->
                        ClothingItemGridCard(
                            item = item,
                            modifier = Modifier.aspectRatio(0.92f),
                            onDelete = { itemIdToDelete = item.id },
                        )
                    }
                }
            }

            if (itemIdToDelete != null) {
                AlertDialog(
                    onDismissRequest = { itemIdToDelete = null },
                    title = { Text("Delete item") },
                    text = { Text("Delete this clothing item? This cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.deleteItem(itemIdToDelete!!)
                                itemIdToDelete = null
                            },
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { itemIdToDelete = null }) { Text("Cancel") }
                    },
                )
            }
        }
    }
}
