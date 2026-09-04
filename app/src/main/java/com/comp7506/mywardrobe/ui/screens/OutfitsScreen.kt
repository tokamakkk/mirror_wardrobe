package com.comp7506.mywardrobe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.ui.theme.UiTokens
import com.comp7506.mywardrobe.ui.components.OutfitTileLayout
import com.comp7506.mywardrobe.ui.viewmodel.OutfitsViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory
import androidx.compose.material3.FabPosition

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun OutfitsScreen(navController: NavController) {
    val factory = rememberAppViewModelFactory()
    val vm: OutfitsViewModel = viewModel(factory = factory)
    val outfits by vm.outfits.collectAsStateWithLifecycle()
    val query by vm.currentQuery.collectAsStateWithLifecycle()
    val occasion by vm.currentOccasion.collectAsStateWithLifecycle()
    var outfitIdToDelete by remember { mutableStateOf<Long?>(null) }

    val occasions = listOf(
        null to "All",
        "Business" to "Business",
        "Office" to "Office",
        "Dating" to "Dating",
        "Casual" to "Casual",
        "Ceremony" to "Ceremony",
        "Sport" to "Sport",
        "Homewear" to "Homewear",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UiTokens.appGradientBrush)
    ) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(AppRoutes.CreateOutfit.route) },
                    containerColor = Color(0x66FFFFFF),
                    contentColor = Color(0xFF1C1B1F),
                    modifier = Modifier.padding(bottom = UiTokens.wardrobeBottomContentPadding),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "New outfit", modifier = Modifier.size(28.dp))
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
            containerColor = Color.Transparent,
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.setQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search my outfits…") },
                    shape = RoundedCornerShape(24.dp),
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    occasions.forEach { (key, label) ->
                        val selected = occasion == key
                        Text(
                            text = label,
                            fontSize = 18.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Light,
                            color = if (selected) Color.Black else Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { vm.setOccasion(key) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (outfits.isEmpty()) {
                    Text("No outfits yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(UiTokens.wardrobeGridSpacing),
                        horizontalArrangement = Arrangement.spacedBy(UiTokens.wardrobeGridSpacing),
                        contentPadding = PaddingValues(bottom = UiTokens.wardrobeBottomContentPadding + 16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(outfits, key = { it.outfit.id }) { outfit ->
                            val orderedItems = outfit.items
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.82f)
                                    .clickable { navController.navigate(AppRoutes.EditOutfit.route(outfit.outfit.id)) },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF)),
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (orderedItems.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0x80FFFFFF)),
                                        )
                                    } else {
                                        OutfitTileLayout(
                                            items = orderedItems,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(12.dp)
                                                .clip(RoundedCornerShape(20.dp)),
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(10.dp)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x66FFFFFF))
                                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                            .clickable { navController.navigate(AppRoutes.EditOutfit.route(outfit.outfit.id)) },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Edit outfit",
                                            tint = Color(0xFF1C1B1F),
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (outfitIdToDelete != null) {
            AlertDialog(
                onDismissRequest = { outfitIdToDelete = null },
                title = { Text("Delete outfit") },
                text = { Text("Delete this outfit? This cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.deleteOutfit(outfitIdToDelete!!)
                            outfitIdToDelete = null
                        },
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { outfitIdToDelete = null }) { Text("Cancel") }
                },
            )
        }
    }
}
