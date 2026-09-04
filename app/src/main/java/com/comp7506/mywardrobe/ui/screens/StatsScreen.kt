package com.comp7506.mywardrobe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.comp7506.mywardrobe.ui.components.AppTopBar
import com.comp7506.mywardrobe.ui.components.CategoryPieChart
import com.comp7506.mywardrobe.ui.theme.AppBackground
import com.comp7506.mywardrobe.ui.viewmodel.StatsViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory

@Composable
fun StatsScreen(navController: NavController) {
    val factory = rememberAppViewModelFactory()
    val vm: StatsViewModel = viewModel(factory = factory)
    val total by vm.totalCount.collectAsStateWithLifecycle()
    val categoryCounts by vm.categoryCounts.collectAsStateWithLifecycle()
    val wearCounts by vm.wearCounts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(title = "Wardrobe stats", onBack = { navController.popBackStack() })
        },
        containerColor = AppBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Total items")
                    Text(total.toString(), style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("By category", style = MaterialTheme.typography.titleMedium)
                    if (categoryCounts.isEmpty()) {
                        Text("No data yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        CategoryPieChart(counts = categoryCounts)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Wear frequency", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (wearCounts.isEmpty()) {
                Text("No outfit logs yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(wearCounts, key = { it.itemId }) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(item.name, style = MaterialTheme.typography.bodyLarge)
                                Text("${item.wearCount} wears", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
