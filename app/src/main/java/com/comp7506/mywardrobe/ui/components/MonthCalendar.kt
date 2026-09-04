package com.comp7506.mywardrobe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthCalendar(
    month: YearMonth,
    datesWithRecords: Set<LocalDate>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val offset = (firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value).let { if (it < 0) it + 7 else it }
    val cells = buildList<LocalDate?> {
        repeat(offset) { add(null) }
        for (d in 1..daysInMonth) add(month.atDay(d))
        while (size % 7 != 0) add(null)
    }

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { label ->
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        CalendarCell(
                            date = date,
                            selected = date == selectedDate,
                            hasRecord = date != null && datesWithRecords.contains(date),
                            onClick = { if (date != null) onSelectDate(date) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCell(
    date: LocalDate?,
    selected: Boolean,
    hasRecord: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFFF5E6D3) else Color.Transparent)
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF1C1B1F),
                )
                if (hasRecord) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(99.dp))
                            .background(Color(0xFF7C4DFF))
                            .padding(3.dp),
                    )
                }
            }
        }
    }
}

