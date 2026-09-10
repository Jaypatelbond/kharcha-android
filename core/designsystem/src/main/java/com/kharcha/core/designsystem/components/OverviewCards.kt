package com.kharcha.core.designsystem.components

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
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import com.kharcha.core.designsystem.theme.TealPrimary

@Composable
fun OverviewCards(
    balance: Double,
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val savings = (income - expense).coerceAtLeast(0.0)
    val savingsProgress = if (income > 0) (savings / income).toFloat() else 0f

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            OverviewCardItem(
                title = "Total Balance",
                amount = balance,
                icon = Icons.Rounded.AccountBalanceWallet,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color(0xFF0D9488))
                ),
                contentColor = Color.White,
                accentColor = TealPrimary
            )
        }
        item {
            OverviewCardItem(
                title = "Monthly Income",
                amount = income,
                icon = Icons.Rounded.ArrowUpward,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF047857))
                ),
                contentColor = Color.White,
                accentColor = Color(0xFF34D399)
            )
        }
        item {
            OverviewCardItem(
                title = "Monthly Expense",
                amount = expense,
                icon = Icons.Rounded.ArrowDownward,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF4C0519), Color(0xFF881337), Color(0xFF9F1239))
                ),
                contentColor = Color.White,
                accentColor = Color(0xFFFB7185)
            )
        }
        item {
            OverviewCardItem(
                title = "Net Savings",
                amount = savings,
                icon = Icons.Rounded.Savings,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFF312E81), Color(0xFF4338CA), Color(0xFF065F46))
                ),
                contentColor = Color.White,
                accentColor = Color(0xFF38BDF8),
                progress = savingsProgress
            )
        }
    }
}

@Composable
fun OverviewCardItem(
    title: String,
    amount: Double,
    icon: ImageVector,
    gradient: Brush,
    contentColor: Color,
    accentColor: Color,
    progress: Float? = null
) {
    Card(
        modifier = Modifier
            .width(165.dp)
            .height(185.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = CurrencyFormatter.format(amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }

                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = accentColor,
                        trackColor = Color.White.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}
