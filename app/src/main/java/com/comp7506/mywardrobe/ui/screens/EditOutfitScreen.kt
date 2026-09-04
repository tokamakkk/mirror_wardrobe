package com.comp7506.mywardrobe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.data.repository.WardrobeRepository.OutfitItemTransform
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.ui.viewmodel.EditOutfitViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberEditOutfitViewModelFactory

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EditOutfitScreen(outfitId: Long, navController: NavController) {
    val factory = rememberEditOutfitViewModelFactory(outfitId)
    val vm: EditOutfitViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val selectedItems by vm.selectedItems.collectAsStateWithLifecycle()
    val allItems by vm.allItems.collectAsStateWithLifecycle()
    val collageItemStates = remember { mutableStateMapOf<Long, CollageItemState>() }
    val layerOrder = remember { mutableStateListOf<Long>() }
    var selectedCanvasItemId by remember { mutableStateOf<Long?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            navController.popBackStack()
        }
    }

    LaunchedEffect(selectedItems, state.itemTransforms, canvasSize) {
        val selectedIds = selectedItems.map { it.id }
        val removedIds = collageItemStates.keys.toSet() - selectedIds.toSet()
        removedIds.forEach { id ->
            collageItemStates.remove(id)
            layerOrder.remove(id)
            if (selectedCanvasItemId == id) selectedCanvasItemId = null
        }

        selectedIds.forEach { id ->
            if (!collageItemStates.containsKey(id)) {
                val saved = state.itemTransforms[id]
                collageItemStates[id] = if (saved != null) {
                    CollageItemState(
                        offsetX = saved.offsetX,
                        offsetY = saved.offsetY,
                        scale = saved.scale,
                        rotation = saved.rotation,
                    )
                } else {
                    // New item defaults to the canvas center.
                    CollageItemState(offsetX = 0f, offsetY = 0f, scale = 1f, rotation = 0f)
                }
            }
            if (!layerOrder.contains(id)) {
                layerOrder.add(id)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE6E1F9), Color(0xFFF3E7D9)),
                ),
            )
            .systemBarsPadding(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1C1B1F),
                    )
                }
                Text(
                    text = "Edit",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                )
                TextButton(
                    onClick = { vm.save() },
                    enabled = state.isLoaded && !state.isSaving,
                ) {
                    Text("Update", fontSize = 18.sp, color = Color(0xFF1C1B1F))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!state.isLoaded) {
                Text(
                    "Loading…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(OUTFIT_CARD_CORNER),
                colors = CardDefaults.cardColors(containerColor = Color(0x4DFFFFFF)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(OUTFIT_CARD_ASPECT_RATIO)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(OUTFIT_CARD_INNER_CORNER))
                        .background(Color(0x33FFFFFF))
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = { selectedCanvasItemId = null })
                        },
                ) {
                    val bringToFront: (Long) -> Unit = { itemId ->
                        layerOrder.remove(itemId)
                        layerOrder.add(itemId)
                    }

                    if (selectedItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Pick items from the sheet below",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0x99000000),
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .onSizeChanged { canvasSize = it }
                                .padding(8.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.20f)),
                        ) {
                            layerOrder.forEachIndexed { index, itemId ->
                                val item = selectedItems.firstOrNull { it.id == itemId } ?: return@forEachIndexed
                                val itemState = collageItemStates[itemId] ?: return@forEachIndexed
                                val isSelected = selectedCanvasItemId == itemId

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(130.dp)
                                        .graphicsLayer {
                                            translationX = itemState.offsetX
                                            translationY = itemState.offsetY
                                            scaleX = itemState.scale
                                            scaleY = itemState.scale
                                            rotationZ = itemState.rotation
                                        }
                                        .zIndex(index.toFloat() + 1f)
                                        .pointerInput(itemId) {
                                            detectDragGestures(
                                                onDragStart = {
                                                    selectedCanvasItemId = itemId
                                                    bringToFront(itemId)
                                                },
                                            ) { change, dragAmount ->
                                                change.consume()
                                                val currentState = collageItemStates[itemId] ?: itemState
                                                val updated = currentState.copy(
                                                    offsetX = currentState.offsetX + dragAmount.x,
                                                    offsetY = currentState.offsetY + dragAmount.y,
                                                )
                                                collageItemStates[itemId] = updated
                                                vm.updateItemTransform(
                                                    itemId = itemId,
                                                    transform = OutfitItemTransform(
                                                        offsetX = updated.offsetX,
                                                        offsetY = updated.offsetY,
                                                        scale = updated.scale,
                                                        rotation = updated.rotation,
                                                    ),
                                                )
                                            }
                                        }
                                        .pointerInput(itemId) {
                                            detectTapGestures {
                                                selectedCanvasItemId = itemId
                                                bringToFront(itemId)
                                            }
                                        },
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                    ) {
                                        if (item.imageUri.isNullOrBlank()) {
                                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE9EEF5)))
                                        } else {
                                            AsyncImage(
                                                model = item.imageUri,
                                                contentDescription = item.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Fit,
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(1.6.dp, Color.White, RoundedCornerShape(10.dp)),
                                        )

                                        SmallCanvasAction(
                                            modifier = Modifier.align(Alignment.TopStart),
                                            icon = Icons.Outlined.Refresh,
                                            contentDescription = "Rotate",
                                            onClick = {
                                                val currentState = collageItemStates[itemId] ?: itemState
                                                val updated = currentState.copy(rotation = currentState.rotation + 12f)
                                                collageItemStates[itemId] = updated
                                                vm.updateItemTransform(
                                                    itemId = itemId,
                                                    transform = OutfitItemTransform(
                                                        offsetX = updated.offsetX,
                                                        offsetY = updated.offsetY,
                                                        scale = updated.scale,
                                                        rotation = updated.rotation,
                                                    ),
                                                )
                                            },
                                        )
                                        SmallCanvasAction(
                                            modifier = Modifier.align(Alignment.BottomEnd),
                                            icon = Icons.Outlined.OpenInFull,
                                            contentDescription = "Scale",
                                            onDrag = { dragAmount ->
                                                val currentState = collageItemStates[itemId] ?: itemState
                                                val delta = (dragAmount.x - dragAmount.y) * 0.0045f
                                                val updated = currentState.copy(
                                                    scale = (currentState.scale + delta).coerceIn(0.55f, 2.30f),
                                                )
                                                collageItemStates[itemId] = updated
                                                vm.updateItemTransform(
                                                    itemId = itemId,
                                                    transform = OutfitItemTransform(
                                                        offsetX = updated.offsetX,
                                                        offsetY = updated.offsetY,
                                                        scale = updated.scale,
                                                        rotation = updated.rotation,
                                                    ),
                                                )
                                            },
                                        )
                                        SmallCanvasAction(
                                            modifier = Modifier.align(Alignment.TopEnd),
                                            icon = Icons.Outlined.Close,
                                            contentDescription = "Remove item",
                                            onClick = {
                                                vm.toggleItem(itemId)
                                                selectedCanvasItemId = null
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE6FFFFFF)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(width = 32.dp, height = 4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFFBDBDBD)),
                    )

                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { vm.setName(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Outfit name") },
                        enabled = state.isLoaded,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0x33FFFFFF),
                            unfocusedContainerColor = Color(0x33FFFFFF),
                        ),
                    )

                    Text("Assets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        items(allItems, key = { it.id }) { item ->
                            val selected = state.selectedItemIds.contains(item.id)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clickable(enabled = state.isLoaded) { vm.toggleItem(item.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) Color.White else Color(0x33FFFFFF),
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 0.dp),
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (item.imageUri.isNullOrBlank()) {
                                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE9EEF5)))
                                    } else {
                                        AsyncImage(
                                            model = item.imageUri,
                                            contentDescription = item.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                    if (selected) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "Selected",
                                                tint = Color(0xFF1C1B1F),
                                                modifier = Modifier.size(12.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }

        FloatingActionButton(
            onClick = { navController.navigate(AppRoutes.AddItem.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp),
            shape = CircleShape,
            containerColor = Color(0xCCFFFFFF),
            contentColor = Color(0xFF1C1B1F),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add clothing item",
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

private const val OUTFIT_CARD_ASPECT_RATIO = 0.82f
private val OUTFIT_CARD_CORNER = 24.dp
private val OUTFIT_CARD_INNER_CORNER = 20.dp

@Composable
private fun SmallCanvasAction(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: (() -> Unit)? = null,
    onDrag: ((Offset) -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.95f))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .then(
                if (onDrag != null) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount)
                        }
                    }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFF1C1B1F),
            modifier = Modifier.size(16.dp),
        )
    }
}

private data class CollageItemState(
    val offsetX: Float,
    val offsetY: Float,
    val scale: Float = 1f,
    val rotation: Float = 0f,
)
