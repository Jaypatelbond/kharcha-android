package com.kharcha.core.designsystem.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
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
import com.kharcha.core.designsystem.theme.ExpenseRed
import com.kharcha.core.designsystem.theme.ExpenseRedDark
import com.kharcha.core.designsystem.theme.IncomeGreen
import com.kharcha.core.designsystem.theme.IncomeGreenDark
import com.kharcha.core.designsystem.theme.TealDark
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.common.util.CurrencyFormatter

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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OverviewCardItem(
                title = "Total Balance",
                amount = balance,
                icon = Icons.Rounded.AccountBalanceWallet,
                gradient = Brush.verticalGradient(listOf(TealPrimary, TealDark)),
                contentColor = Color.White
            )
        }
        item {
            OverviewCardItem(
                title = "Total Income",
                amount = income,
                icon = Icons.Rounded.ArrowUpward,
                gradient = Brush.verticalGradient(listOf(IncomeGreen, IncomeGreenDark)),
                contentColor = Color.White
            )
        }
        item {
            OverviewCardItem(
                title = "Total Expense",
                amount = expense,
                icon = Icons.Rounded.ArrowDownward,
                gradient = Brush.verticalGradient(listOf(ExpenseRed, ExpenseRedDark)),
                contentColor = Color.White
            )
        }
        item {
            OverviewCardItem(
                title = "Total Savings",
                amount = savings,
                icon = Icons.Rounded.Savings,
                gradient = Brush.verticalGradient(listOf(Color(0xFF64FFDA), Color(0xFF1DE9B6))), // Custom Teal
                contentColor = Color(0xFF004D40), // Dark teal text for contrast on light teal
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
    progress: Float? = null
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Transparent to show Box gradient
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradient)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = contentColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = CurrencyFormatter.format(amount),
                        style = MaterialTheme.typography.headlineSmall, // Increased size
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }

                if (progress != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = contentColor,
                        trackColor = contentColor.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}
