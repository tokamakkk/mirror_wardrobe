package com.comp7506.mywardrobe.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.comp7506.mywardrobe.ui.components.MonthCalendar
import com.comp7506.mywardrobe.ui.theme.UiTokens
import com.comp7506.mywardrobe.ui.viewmodel.CalendarViewModel
import com.comp7506.mywardrobe.ui.viewmodel.rememberAppViewModelFactory
import com.comp7506.mywardrobe.util.createTempImageUri
import com.comp7506.mywardrobe.util.toLocalDateOrNull
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private const val CALENDAR_YEAR_MIN = 1990

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CalendarScreen() {
    val context = LocalContext.current
    val factory = rememberAppViewModelFactory()
    val vm: CalendarViewModel = viewModel(factory = factory)
    val month by vm.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by vm.currentSelectedDate.collectAsStateWithLifecycle()
    val records by vm.recordsInMonth.collectAsStateWithLifecycle()
    val selectedRecord by vm.selectedRecord.collectAsStateWithLifecycle()
    val outfits by vm.outfits.collectAsStateWithLifecycle()

    val datesWithRecords = remember(records) {
        records.mapNotNull { it.date.toLocalDateOrNull() }.toSet()
    }

    val selectedOutfit = selectedRecord?.let { record -> outfits.firstOrNull { it.outfit.id == record.outfitId } }
    var pickDialogVisible by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendingCameraUri
        if (ok && uri != null) {
            vm.recordPhotoOutfit(uri)
            pickDialogVisible = false
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createTempImageUri(context).toString()
            pendingCameraUri = uri
            takePictureLauncher.launch(Uri.parse(uri))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UiTokens.appGradientBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(bottom = UiTokens.wardrobeBottomContentPadding)
                        .offset(y = (-32).dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (selectedRecord != null) showDeleteConfirm = true
                        },
                        modifier = Modifier.size(56.dp),
                        containerColor = if (selectedRecord != null) {
                            Color(0xCCFFFFFF)
                        } else {
                            Color(0x66FFFFFF)
                        },
                        contentColor = if (selectedRecord != null) {
                            Color(0xFF1C1B1F)
                        } else {
                            Color(0x661C1B1F)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Remove day record",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    FloatingActionButton(
                        onClick = { pickDialogVisible = true },
                        modifier = Modifier.size(56.dp),
                        containerColor = Color(0xCCFFFFFF),
                        contentColor = Color(0xFF1C1B1F),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Log today's outfit",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
            },
        ) { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xBFFFFFFF)),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val calendarMaxYear = LocalDate.now().year
                        val calendarYears = remember(calendarMaxYear) {
                            (CALENDAR_YEAR_MIN..calendarMaxYear).reversed().toList()
                        }
                        CalendarYearMonthHeader(
                            month = month,
                            years = calendarYears,
                            onPreviousMonth = { vm.previousMonth() },
                            onNextMonth = { vm.nextMonth() },
                            onYearMonthSelected = { vm.setYearMonth(it) },
                        )

                        MonthCalendar(
                            month = month,
                            datesWithRecords = datesWithRecords,
                            selectedDate = selectedDate,
                            onSelectDate = { vm.setSelectedDate(it) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Outfit for this day",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xBFFFFFFF)),
                ) {
                    if (selectedOutfit == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.8f)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .drawBehind {
                                    drawRoundRect(
                                        color = Color(0xCCFFFFFF),
                                        style = Stroke(
                                            width = 3f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f), 0f),
                                        ),
                                        cornerRadius = CornerRadius(24f, 24f),
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = Color(0xAAFFFFFF),
                                modifier = Modifier.size(40.dp),
                            )
                        }
                    } else {
                        val imageUri = selectedOutfit.outfit.imageUri ?: selectedOutfit.items.firstOrNull()?.imageUri
                        if (imageUri.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.8f)
                                    .background(Color(0xFFE9EEF5)),
                            )
                        } else {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = selectedOutfit.outfit.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.8f),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(UiTokens.wardrobeBottomContentPadding + 16.dp))
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Remove day record") },
                    text = { Text("Remove the outfit log for this day? The saved outfit will not be deleted.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.deleteRecordForSelectedDate()
                                showDeleteConfirm = false
                            },
                        ) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                    },
                )
            }

            if (pickDialogVisible) {
                AlertDialog(
                    onDismissRequest = { pickDialogVisible = false },
                    title = { Text("Log outfit") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Take photo")
                            }
                            if (outfits.isEmpty()) {
                                Text(
                                    "No saved outfits yet. Take a photo first, or create one on the Outfits tab.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                Text("Choose saved outfit", style = MaterialTheme.typography.labelLarge)
                                outfits.forEach { outfit ->
                                    Button(
                                        onClick = {
                                            vm.recordOutfitForSelectedDate(outfit.outfit.id)
                                            pickDialogVisible = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(outfit.outfit.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { pickDialogVisible = false }) { Text("Cancel") }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarYearMonthHeader(
    month: YearMonth,
    years: List<Int>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onYearMonthSelected: (YearMonth) -> Unit,
) {
    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val monthLabel = month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val glassFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0x99FFFFFF),
        unfocusedContainerColor = Color(0x99FFFFFF),
        disabledContainerColor = Color(0x99FFFFFF),
        focusedBorderColor = Color.White.copy(alpha = 0.55f),
        unfocusedBorderColor = Color.White.copy(alpha = 0.38f),
        focusedTextColor = Color(0xFF1C1B1F),
        unfocusedTextColor = Color(0xFF1C1B1F),
        disabledTextColor = Color(0xFF1C1B1F),
        focusedTrailingIconColor = Color(0xFF1C1B1F),
        unfocusedTrailingIconColor = Color(0xFF1C1B1F),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = {
                monthExpanded = false
                yearExpanded = false
                onPreviousMonth()
            },
            modifier = Modifier.size(44.dp),
        ) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExposedDropdownMenuBox(
                expanded = monthExpanded,
                onExpandedChange = {
                    monthExpanded = it
                    if (it) yearExpanded = false
                },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .heightIn(max = 48.dp),
                    value = monthLabel,
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.2.sp,
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = glassFieldColors,
                )
                ExposedDropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = { monthExpanded = false },
                    modifier = Modifier
                        .heightIn(max = 280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xE6FFFFFF)),
                ) {
                    Month.entries.forEach { m ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    m.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            },
                            onClick = {
                                onYearMonthSelected(YearMonth.of(month.year, m))
                                monthExpanded = false
                            },
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = yearExpanded,
                onExpandedChange = {
                    yearExpanded = it
                    if (it) monthExpanded = false
                },
                modifier = Modifier.weight(1f),
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .heightIn(max = 48.dp),
                    value = month.year.toString(),
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.2.sp,
                    ),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    colors = glassFieldColors,
                )
                ExposedDropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded = false },
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xE6FFFFFF)),
                ) {
                    years.forEach { y ->
                        DropdownMenuItem(
                            text = {
                                Text(y.toString(), style = MaterialTheme.typography.bodyMedium)
                            },
                            onClick = {
                                onYearMonthSelected(YearMonth.of(y, month.month))
                                yearExpanded = false
                            },
                        )
                    }
                }
            }
        }

        IconButton(
            onClick = {
                monthExpanded = false
                yearExpanded = false
                onNextMonth()
            },
            modifier = Modifier.size(44.dp),
        ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
        }
    }
}
