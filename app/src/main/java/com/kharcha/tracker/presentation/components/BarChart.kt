package com.kharcha.tracker.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class BarData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun BarChart(
    bars: List<BarData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    maxHeight: Float = 200f,
    animationDuration: Int = 800
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(bars) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    val maxValue = bars.maxOfOrNull { it.value } ?: 0f
    if (maxValue == 0f || bars.isEmpty()) return

    // Calculate grid lines (0, 50%, 100% of max)
    val gridLines = listOf(0f, maxValue / 2, maxValue)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight.dp)
        ) {
            // Draw Grid Lines & Y-Axis Labels
            Canvas(modifier = Modifier.fillMaxSize()) {
                val linePaint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                    color = android.graphics.Color.LTGRAY
                    strokeWidth = 1.dp.toPx()
                    pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
                }
                
                // Draw Y-Axis labels and lines will be tricky in strict Canvas without Text measurements easily.
                // Simplified: Draw dashed lines at top, middle, bottom
                
                val yStep = size.height / 2
                
                // Top line (Max Value)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                
                 // Middle line
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, yStep),
                    end = Offset(size.width, yStep),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
                
                // Bottom lineBase
                 drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height)
                )
            }

            // Draw Bars
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp) // Space for top label if we added one, or just margin
            ) {
                val barCount = bars.size
                val maxBarWidth = 40.dp.toPx()
                val minSpacing = 12.dp.toPx()
                
                val totalAvailableWidth = size.width
                val calculatedBarWidth = (totalAvailableWidth - (minSpacing * (barCount + 1))) / barCount
                val barWidth = calculatedBarWidth.coerceAtMost(maxBarWidth)
                
                val totalBarWidth = barWidth * barCount
                val totalSpacingRequired = totalAvailableWidth - totalBarWidth
                val spacing = totalSpacingRequired / (barCount + 1)
                
                // Gradient for bars
                val brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(barColor, barColor.copy(alpha = 0.6f))
                )

                bars.forEachIndexed { index, bar ->
                    // Adjust height to leave room for labels area if drawn here, but we draw labels below.
                    // We map value to height.
                    val normalizedValue = bar.value / maxValue
                    val barHeight = (normalizedValue * size.height * animationProgress.value)
                    
                    val x = spacing + index * (barWidth + spacing)
                    val y = size.height - barHeight

                    val cornerRadius = 6.dp.toPx()

                    drawRoundRect(
                        brush = brush,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly // simplified spacing
        ) {
            // We need to align labels with bars manually or use a Row with weighted spacers?
            // SpaceEvenly matches reasonably well if bars are centered.
            bars.forEach { bar ->
                 Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                     modifier = Modifier.width(30.dp), // Fixed width to ensure centering? 
                     textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                     maxLines = 1,
                     overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                )
            }
        }
    }
}
