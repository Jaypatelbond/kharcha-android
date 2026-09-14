package com.kharcha.core.designsystem.components

import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kharcha.core.common.util.CurrencyFormatter

@Composable
fun OverviewCards(
    totalKharcha: Double,
    monthlyExpense: Double,
    dailyExpense: Double,
    totalTransactionsCount: Int,
    collectionsCount: Int,
    monthLabel: String,
    selectedCollectionName: String? = null,
    selectedCollectionTotal: Double? = null,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card 1: Total Kharcha (All-Time Overall Spending)
        item {
            OverviewCardItem(
                title = "Total Kharcha",
                amountStr = CurrencyFormatter.format(totalKharcha),
                subtitle = "$totalTransactionsCount total entries",
                icon = Icons.Rounded.Payments,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF4C0519), Color(0xFF881337), Color(0xFF9F1239))
                ),
                contentColor = Color.White,
                accentColor = Color(0xFFFF4757)
            )
        }

        // Card 2: This Month's Kharcha
        item {
            OverviewCardItem(
                title = "This Month",
                amountStr = CurrencyFormatter.format(monthlyExpense),
                subtitle = monthLabel.ifBlank { "Monthly spend" },
                icon = Icons.Rounded.CalendarMonth,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
                ),
                contentColor = Color.White,
                accentColor = Color(0xFFA5B4FC)
            )
        }

        // Card 3: Active Books / Collections
        item {
            val title = if (selectedCollectionName != null) selectedCollectionName else "Expense Books"
            val amt = if (selectedCollectionTotal != null) {
                CurrencyFormatter.format(selectedCollectionTotal)
            } else {
                "$collectionsCount Books"
            }
            val sub = if (selectedCollectionName != null) "Filtered book" else "Tap below to view"
            OverviewCardItem(
                title = title,
                amountStr = amt,
                subtitle = sub,
                icon = Icons.Rounded.FolderSpecial,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF042F2E), Color(0xFF0F766E), Color(0xFF0D9488))
                ),
                contentColor = Color.White,
                accentColor = Color(0xFF00F5D4)
            )
        }

        // Card 4: Daily / Selected Day Spend
        item {
            OverviewCardItem(
                title = "Selected Day",
                amountStr = CurrencyFormatter.format(dailyExpense),
                subtitle = if (dailyExpense > 0) "Recorded on date" else "No spend on date",
                icon = Icons.Rounded.Today,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF451A03), Color(0xFF78350F), Color(0xFFB45309))
                ),
                contentColor = Color.White,
                accentColor = Color(0xFFFBBF24)
            )
        }
    }
}

@Composable
fun OverviewCardItem(
    title: String,
    amountStr: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    contentColor: Color,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .width(165.dp)
            .height(175.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient)
                .border(1.dp, Color(0x33CBD5E1), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.85f),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = amountStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
