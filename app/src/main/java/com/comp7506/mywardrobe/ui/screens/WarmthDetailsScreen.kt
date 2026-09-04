package com.comp7506.mywardrobe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.R
import com.comp7506.mywardrobe.ui.components.AppTopBar
import com.comp7506.mywardrobe.ui.theme.AppBackground
import com.comp7506.mywardrobe.ui.viewmodel.HomeViewModel
import com.comp7506.mywardrobe.ui.viewmodel.WarmthDetailsViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarmthDetailsScreen(navController: NavController) {
    val factory = rememberAppViewModelFactory()
    val vm: WarmthDetailsViewModel = viewModel(factory = factory)
    val homeVm: HomeViewModel = viewModel(factory = factory)

    val selectedOccasion by vm.currentOccasion.collectAsStateWithLifecycle()
    val outfits by vm.matchedOutfits.collectAsStateWithLifecycle()
    val todaySuggestion by vm.todaySuggestion.collectAsStateWithLifecycle()
    val recommendation by homeVm.recommendation.collectAsStateWithLifecycle()

    LaunchedEffect(recommendation) {
        vm.setWeatherRecommendation(recommendation)
    }

    val occasions = listOf("All", "Business", "Office", "Dating", "Casual", "Ceremony", "Sport", "Homewear")

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(title = "Warmth & occasion", onBack = { navController.popBackStack() })
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today outfit suggestion", style = MaterialTheme.typography.titleMedium)
                    if (todaySuggestion != null) {
                        Text("• $todaySuggestion", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("Waiting for weather…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How warmth target works", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Base: target = 26°C - outdoor temp (prefer feels-like).",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "Adjust: wind / humidity / heavy clouds may add +1~2.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Warm value reference (examples)", style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.warmth_value_light_tshirt), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.warmth_value_shirt), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.warmth_value_sweatshirt), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.warmth_value_thick_sweater), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.warmth_value_trench), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.warmth_value_heavy_coat), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.warmth_value_fur_coat), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextField(
                    value = selectedOccasion,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("Occasion") },
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    occasions.forEach { occ ->
                        DropdownMenuItem(
                            text = { Text(occ) },
                            onClick = {
                                vm.setOccasion(occ)
                                expanded = false
                            },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedOccasion != "All") {
                if (outfits.isEmpty()) {
                    Text("No saved outfits match this occasion.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Saved outfits", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // We can't put a LazyColumn inside a verticalScroll Column easily without fixed height.
                    // Instead, we just list them out since we're in a scrollable Column.
                    outfits.forEach { outfit ->
                        val imageUri = outfit.outfit.imageUri ?: outfit.items.firstOrNull()?.imageUri
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { navController.navigate(AppRoutes.EditOutfit.route(outfit.outfit.id)) },
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (imageUri.isNullOrBlank()) {
                                    Box(modifier = Modifier.size(72.dp).background(Color(0xFFE9EEF5)))
                                } else {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = outfit.outfit.name,
                                        modifier = Modifier.size(72.dp),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(outfit.outfit.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                    Text(
                                        outfit.outfit.occasion?.ifBlank { "—" } ?: "—",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                    )
                                    val totalWarmth = outfit.items.sumOf { it.warmthValue }
                                    Text(
                                        "Warmth: $totalWarmth",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
