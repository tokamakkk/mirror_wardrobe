package com.comp7506.mywardrobe.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.comp7506.mywardrobe.ui.theme.UiTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CategoryPieChart(
    counts: Map<String, Int>,
    modifier: Modifier = Modifier,
) {
    val preferredOrder = listOf("Tops", "Pants", "Outerwear", "Shoes", "Accessories")
    val normalizedCounts = counts.entries
        .map { it.key.trim().replaceFirstChar { c -> c.uppercase() } to it.value.coerceAtLeast(0) }
        .filter { it.second > 0 }
    val orderedCounts = buildList {
        preferredOrder.forEach { name ->
            normalizedCounts.firstOrNull { it.first.equals(name, ignoreCase = true) }?.let { add(it) }
        }
        normalizedCounts
            .filterNot { entry -> preferredOrder.any { it.equals(entry.first, ignoreCase = true) } }
            .forEach { add(it) }
    }
    val slices = orderedCounts.mapIndexed { index, (label, value) ->
        PieSlice(
            label = label,
            value = value,
            color = UiTokens.homeChartColors[index % UiTokens.homeChartColors.size],
        )
    }
    val total = slices.sumOf { it.value }.coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.65f),
            contentAlignment = Alignment.Center,
        ) {
            val density = LocalDensity.current
            val chartSize = maxWidth * 0.52f
            val chartSizePx = with(density) { chartSize.toPx() }
            val radius = chartSizePx / 2f
            val lineStartRadius = radius + with(density) { 4.dp.toPx() }
            val labelRadius = radius + with(density) { 26.dp.toPx() }
            val lineTail = with(density) { 14.dp.toPx() }

            var angle = -90f
            val anchors = slices.map { slice ->
                val sweep = 360f * (slice.value.toFloat() / total.toFloat())
                val mid = angle + sweep / 2f
                val rad = (mid / 180f) * PI.toFloat()
                angle += sweep
                LabelAnchor(
                    label = slice.label,
                    percent = (slice.value.toFloat() / total.toFloat() * 100).roundToInt(),
                    rad = rad,
                    x = cos(rad) * labelRadius,
                    y = sin(rad) * labelRadius,
                    isRight = cos(rad) >= 0f,
                )
            }

            fun spread(side: List<LabelAnchor>): List<LabelAnchor> {
                if (side.isEmpty()) return side
                val minGap = with(density) { 20.dp.toPx() }
                val sorted = side.sortedBy { it.y }
                val out = mutableListOf<LabelAnchor>()
                var lastY = Float.NEGATIVE_INFINITY
                sorted.forEach { a ->
                    val adjustedY = if (a.y - lastY < minGap) lastY + minGap else a.y
                    out += a.copy(y = adjustedY)
                    lastY = adjustedY
                }
                return out
            }

            val adjustedAnchors = spread(anchors.filter { it.isRight }) + spread(anchors.filterNot { it.isRight })

            Box(
                modifier = Modifier.size(chartSize),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    val ringStroke = size.minDimension * 0.34f
                    var startAngle = -90f
                    slices.forEach { slice ->
                        val sweep = 360f * (slice.value.toFloat() / total.toFloat())
                        drawArc(
                            color = slice.color,
                            startAngle = startAngle,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(width = ringStroke, cap = StrokeCap.Butt),
                        )
                        startAngle += sweep
                    }
                }

                Text(
                    text = "$total",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "items",
                    fontSize = 12.sp,
                    color = Color(0xFF6A6375),
                    modifier = Modifier.offset(y = 16.dp),
                )
            }

            Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1.65f)) {
                adjustedAnchors.forEach { a ->
                    val start = Offset(
                        x = size.width / 2f + cos(a.rad) * lineStartRadius,
                        y = size.height / 2f + sin(a.rad) * lineStartRadius,
                    )
                    val end = Offset(
                        x = size.width / 2f + a.x + if (a.isRight) lineTail else -lineTail,
                        y = size.height / 2f + a.y,
                    )
                    drawLine(
                        color = Color(0xCC5F6368),
                        start = start,
                        end = end,
                        strokeWidth = 1.5f,
                    )
                }
            }

            adjustedAnchors.forEach { a ->
                Text(
                    text = "${a.label} ${a.percent}%",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = if (a.isRight) TextAlign.Left else TextAlign.Right,
                    color = Color(0xFF1C1B1F),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(
                            x = with(density) { (a.x + if (a.isRight) lineTail + 4f else -lineTail - 4f).toDp() },
                            y = with(density) { a.y.toDp() },
                        ),
                )
            }
        }
    }
}

private data class PieSlice(
    val label: String,
    val value: Int,
    val color: Color,
)

private data class LabelAnchor(
    val label: String,
    val percent: Int,
    val rad: Float,
    val x: Float,
    val y: Float,
    val isRight: Boolean,
)

