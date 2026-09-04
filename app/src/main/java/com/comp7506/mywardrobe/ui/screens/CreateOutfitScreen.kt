package com.comp7506.mywardrobe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.ui.theme.UiTokens
import com.comp7506.mywardrobe.ui.viewmodel.CreateOutfitViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory
import androidx.compose.foundation.layout.width

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CreateOutfitScreen(navController: NavController) {
    val factory = rememberAppViewModelFactory()
    val vm: CreateOutfitViewModel = viewModel(factory = factory)
    val state by vm.uiState.collectAsStateWithLifecycle()
    val selectedItems by vm.selectedItems.collectAsStateWithLifecycle()
    val allItems by vm.allItems.collectAsStateWithLifecycle()

    val occasions = listOf(
        "Business",
        "Office",
        "Dating",
        "Casual",
        "Ceremony",
        "Sport",
        "Homewear",
    )

    if (state.saved) {
        navController.popBackStack()
    }

    val fieldShape = RoundedCornerShape(24.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0x66FFFFFF),
        unfocusedContainerColor = Color(0x66FFFFFF),
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedLabelColor = Color(0x80000000),
        unfocusedLabelColor = Color(0x80000000),
        focusedTextColor = Color(0xFF1C1B1F),
        unfocusedTextColor = Color(0xFF1C1B1F),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UiTokens.appGradientBrush)
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f)),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1C1B1F),
                        )
                    }
                    Text(
                        text = "Create Outfit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1B1F),
                    )
                    Spacer(modifier = Modifier.width(40.dp))
                }
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF)),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Outfit canvas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1C1B1F),
                        )
                        if (selectedItems.isEmpty()) {
                            Text(
                                text = "Pick items from below to add to this outfit",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5C5672),
                            )
                        } else {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(items = selectedItems, key = { it.id }) { item ->
                                    Box {
                                        Card(
                                            modifier = Modifier.size(96.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xE6FFFFFF)),
                                        ) {
                                            if (item.imageUri.isNullOrBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(8.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0x80FFFFFF)),
                                                )
                                            } else {
                                                AsyncImage(
                                                    model = item.imageUri,
                                                    contentDescription = item.name,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(8.dp)
                                                        .clip(RoundedCornerShape(12.dp)),
                                                    contentScale = ContentScale.Crop,
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { vm.removeItem(item.id) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(2.dp)
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x66FFFFFF))
                                                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                        ) {
                                            Icon(
                                                Icons.Filled.Close,
                                                contentDescription = "Remove",
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.name,
                    onValueChange = { vm.setName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Outfit name") },
                    shape = fieldShape,
                    colors = fieldColors,
                )

                Spacer(modifier = Modifier.height(12.dp))

                var occasionMenuExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = occasionMenuExpanded,
                    onExpandedChange = { occasionMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = state.occasion ?: "",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Occasion") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = occasionMenuExpanded) },
                        shape = fieldShape,
                        colors = fieldColors,
                    )
                    ExposedDropdownMenu(
                        expanded = occasionMenuExpanded,
                        onDismissRequest = { occasionMenuExpanded = false },
                    ) {
                        occasions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    vm.setOccasion(option)
                                    occasionMenuExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Choose items",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F),
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.wardrobeGridSpacing),
                    horizontalArrangement = Arrangement.spacedBy(UiTokens.wardrobeGridSpacing),
                    contentPadding = PaddingValues(bottom = UiTokens.wardrobeBottomContentPadding),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    items(allItems, key = { it.id }) { item ->
                        val selected = state.selectedItemIds.contains(item.id)
                        Card(
                            onClick = { vm.toggleItem(item.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.82f),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xCCFFFFFF)),
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(20.dp)),
                                    ) {
                                        if (item.imageUri.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0x80FFFFFF)),
                                            )
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
                                                    .fillMaxSize()
                                                    .background(Color(0x331C1B1F)),
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(10.dp)
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xCCFFFFFF))
                                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Done,
                                                    contentDescription = "Selected",
                                                    tint = Color(0xFF1C1B1F),
                                                    modifier = Modifier.size(18.dp),
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = item.name,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF1C1B1F),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { vm.save() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xCCFFFFFF),
                        contentColor = Color(0xFF1C1B1F),
                    ),
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
