@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.comp7506.mywardrobe.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.Umbrella
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.navigation.AppRoutes
import com.comp7506.mywardrobe.ui.components.CategoryPieChart
import com.comp7506.mywardrobe.ui.components.GlassCard
import com.comp7506.mywardrobe.ui.components.OutfitTileLayout
import com.comp7506.mywardrobe.ui.theme.UiTokens
import com.comp7506.mywardrobe.ui.viewmodel.AuthViewModel
import com.comp7506.mywardrobe.ui.viewmodel.HomeViewModel
import com.comp7506.mywardrobe.ui.viewmodel.WeatherUiState
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory
import com.comp7506.mywardrobe.ui.viewmodel.rememberAuthViewModelFactory

@Composable
fun HomeScreen(navController: NavController) {
    val factory = rememberAppViewModelFactory()
    val vm: HomeViewModel = viewModel(factory = factory)
    val authVm: AuthViewModel = viewModel(factory = rememberAuthViewModelFactory())
    val categoryCounts by vm.categoryCounts.collectAsStateWithLifecycle()
    val recentOutfits by vm.recentOutfits.collectAsStateWithLifecycle()
    val weatherState by vm.weatherState.collectAsStateWithLifecycle()
    val recommendation by vm.recommendation.collectAsStateWithLifecycle()
    val cityName by vm.cityName.collectAsStateWithLifecycle()
    val isEditingCity by vm.isEditingCity.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UiTokens.appGradientBrush)
            .padding(
                start = UiTokens.screenHorizontalPadding,
                end = UiTokens.screenHorizontalPadding,
                top = UiTokens.screenVerticalPadding,
                bottom = UiTokens.screenVerticalPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { authVm.logout() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log out", fontWeight = FontWeight.Medium)
            }
        }

        WeatherGlassCard(
            cityName = cityName,
            weatherState = weatherState,
            outfitAdvice = recommendation?.suggestions?.firstOrNull(),
            isEditingCity = isEditingCity,
            onStartCityEdit = { vm.setEditingCity(true) },
            onCommitCityEdit = { vm.commitCityEdit(it) },
            onRetry = { vm.retryCurrentCityWeather() },
            onClick = { navController.navigate(AppRoutes.WarmthDetails.route) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f, fill = true),
        )

        // Chart sits directly on page gradient (no card background).
        if (categoryCounts.isEmpty()) {
            Text(
                text = "No data yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            CategoryPieChart(
                counts = categoryCounts,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(AppRoutes.Wardrobe.route) }
                    .weight(0.4f, fill = true),
            )
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.30f, fill = true),
            cornerRadius = 28.dp,
            contentPadding = 16.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Recent Outfits",
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                if (recentOutfits.isEmpty()) {
                    Text(
                        "No outfit logs yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val cardGap = 10.dp
                        val cardWidth = (maxWidth - cardGap) / 2
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(cardGap),
                            contentPadding = PaddingValues(end = 4.dp),
                        ) {
                            itemsIndexed(recentOutfits, key = { _, item -> item.outfit.id }) { _, outfit ->
                                val previewItems = outfit.items
                                GlassCard(
                                    modifier = Modifier
                                        .width(cardWidth)
                                        .height(maxHeight)
                                        .clickable { navController.navigate(AppRoutes.EditOutfit.route(outfit.outfit.id)) },
                                    cornerRadius = 16.dp,
                                    contentPadding = 10.dp,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(UiTokens.homeRecentCard, RoundedCornerShape(14.dp)),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (previewItems.isEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFE9EEF5)),
                                                )
                                            } else {
                                                OutfitTileLayout(
                                                    items = previewItems,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(10.dp)),
                                                    // Home recent cards are smaller, tighten spacing.
                                                    gap = 5.dp,
                                                    itemCornerRadius = 9.dp,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherGlassCard(
    cityName: String,
    weatherState: WeatherUiState,
    outfitAdvice: String?,
    isEditingCity: Boolean,
    onStartCityEdit: () -> Unit,
    onCommitCityEdit: (String) -> Unit,
    onRetry: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    data class WeatherCardMetrics(
        val icon: ImageVector,
        val conditionText: String,
        val tempText: String,
        val humidity: String,
        val wind: String,
        val cloudCover: String,
        val isLive: Boolean,
    )

    val metrics = when (weatherState) {
        is WeatherUiState.Success -> {
            val weather = weatherState.data.weather
            val current = weather.temperatureC?.toInt()
            val feels = weather.apparentTemperatureC?.toInt()
            WeatherCardMetrics(
                icon = weatherIcon(weather.weatherCode, weather.precipitationMm, weather.cloudCoverPct),
                conditionText = weatherLabel(weather.weatherCode, weather.precipitationMm, weather.cloudCoverPct),
                tempText = if (current == null && feels == null) "—/—°C" else "${current ?: "—"}°C/${feels ?: "—"}°C",
                humidity = "${weather.relativeHumidityPct?.toInt() ?: "—"}%",
                wind = "${weather.windSpeedMps?.toInt() ?: "—"} m/s",
                cloudCover = "${weather.cloudCoverPct?.toInt() ?: "—"}%",
                isLive = true,
            )
        }
        WeatherUiState.Loading -> WeatherCardMetrics(Icons.Outlined.WbCloudy, "Loading", "--/--°C", "--%", "-- m/s", "--%", false)
        WeatherUiState.NeedsPermission -> WeatherCardMetrics(Icons.Outlined.WbCloudy, "Loading weather", "--/--°C", "--%", "-- m/s", "--%", false)
        WeatherUiState.PermissionDenied -> WeatherCardMetrics(Icons.Outlined.WbCloudy, "Unavailable", "--/--°C", "--%", "-- m/s", "--%", false)
        is WeatherUiState.Error -> WeatherCardMetrics(Icons.Outlined.Thunderstorm, "Unavailable", "--/--°C", "--%", "-- m/s", "--%", false)
    }

    val cityTextStyle = TextStyle(
        fontSize = 36.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black,
    )
    var draftText by remember { mutableStateOf(cityName) }
    LaunchedEffect(cityName, isEditingCity) {
        if (!isEditingCity) draftText = cityName
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(isEditingCity) {
        if (isEditingCity) {
            draftText = cityName
            focusRequester.requestFocus()
        }
    }

    var hadFocus by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isEditingCity,
                onClick = onClick,
            ),
        cornerRadius = 24.dp,
        contentPadding = 18.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(isEditingCity) {
                    if (isEditingCity) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        imageVector = metrics.icon,
                        contentDescription = "Weather icon",
                        tint = Color(0xFF1C1B1F),
                        modifier = Modifier.size(80.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (isEditingCity) {
                            BasicTextField(
                                value = draftText,
                                onValueChange = { draftText = it },
                                textStyle = cityTextStyle,
                                singleLine = true,
                                cursorBrush = SolidColor(Color(0xFF1C1B1F)),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done,
                                    keyboardType = KeyboardType.Text,
                                    capitalization = KeyboardCapitalization.Words,
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { focusManager.clearFocus() },
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { state ->
                                        if (hadFocus && !state.isFocused) {
                                            onCommitCityEdit(draftText)
                                        }
                                        hadFocus = state.isFocused
                                    },
                                decorationBox = { inner ->
                                    Box(Modifier.fillMaxWidth()) { inner() }
                                },
                            )
                        } else {
                            Text(
                                text = cityName,
                                style = cityTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.combinedClickable(
                                    onClick = {},
                                    onDoubleClick = { onStartCityEdit() },
                                ),
                            )
                        }
                        Text(
                            text = metrics.tempText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1C1B1F),
                        )
                        Text(
                            text = metrics.conditionText,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1C1B1F),
                        )
                    }
                }

                Text(
                    text = "Outfit Advice: ${outfitAdvice ?: "Preparing recommendation..."}",
                    style = MaterialTheme.typography.titleMedium,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF1C1B1F),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                when (weatherState) {
                    is WeatherUiState.Error -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                weatherState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = onRetry, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Outlined.WbCloudy, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    WeatherUiState.Loading -> {
                        IconButton(onClick = onRetry, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.WbCloudy, contentDescription = "Retry", modifier = Modifier.size(16.dp))
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun WeatherMicroStat(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.56f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = Color(0xFF5C5672),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF5C5672),
        )
    }
}

private fun weatherLabel(code: Int?, precipitationMm: Double?, cloudCoverPct: Double?): String {
    val rain = (precipitationMm ?: 0.0) > 0.1
    val cloudy = (cloudCoverPct ?: 0.0) >= 60.0
    val c = code ?: -1
    return when {
        c in listOf(95, 96, 99) -> "Thunderstorm"
        rain || c in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> "Rainy"
        cloudy || c in listOf(2, 3) -> "Partly Cloudy"
        else -> "Sunny"
    }
}

private fun weatherIcon(code: Int?, precipitationMm: Double?, cloudCoverPct: Double?): ImageVector {
    val rain = (precipitationMm ?: 0.0) > 0.1
    val cloudy = (cloudCoverPct ?: 0.0) >= 60.0
    val c = code ?: -1
    return when {
        c in listOf(95, 96, 99) -> Icons.Outlined.Thunderstorm
        rain || c in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) -> Icons.Outlined.Umbrella
        cloudy || c in listOf(2, 3) -> Icons.Outlined.WbCloudy
        else -> Icons.Outlined.WbSunny
    }
}

