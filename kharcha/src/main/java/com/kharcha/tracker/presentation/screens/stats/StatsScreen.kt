package com.kharcha.tracker.presentation.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kharcha.core.designsystem.components.AdMobBanner
import com.kharcha.core.designsystem.components.BarChart
import com.kharcha.core.designsystem.components.BarData
import com.kharcha.core.designsystem.components.DonutChart
import com.kharcha.core.designsystem.components.DonutSegment
import com.kharcha.core.designsystem.components.EmptyState
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.designsystem.util.ui
import com.kharcha.core.common.util.CurrencyFormatter
import com.kharcha.core.common.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(
    mainViewModel: com.kharcha.tracker.MainViewModel = hiltViewModel(),
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState: StatsUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState
    val isAdFree by mainViewModel.isAdFree.collectAsStateWithLifecycle()

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = state.selectedRange.ordinal,
        pageCount = { TimeRange.entries.size }
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Sync pager with selected range (clicked from chip)
    androidx.compose.runtime.LaunchedEffect(state.selectedRange) {
        if (pagerState.currentPage != state.selectedRange.ordinal) {
            pagerState.animateScrollToPage(state.selectedRange.ordinal)
        }
    }

    // Sync selected range with pager (swiped)
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        val newRange = TimeRange.entries[pagerState.currentPage]
        if (state.selectedRange != newRange) {
            viewModel.onTimeRangeChange(newRange)
        }
    }

    Scaffold(
        bottomBar = {
            AdMobBanner(isAdFree = isAdFree)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )

            // Time range selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                TimeRange.entries.forEach { range ->
                    FilterChip(
                        selected = state.selectedRange == range,
                        onClick = {
                            viewModel.onTimeRangeChange(range)
                            scope.launch { pagerState.animateScrollToPage(range.ordinal) }
                        },
                        label = { Text(range.label) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val range = TimeRange.entries[page]
                val rangeData = state.stats[range] ?: StatsData() // Get specific data for this page
                
                StatsPageContent(
                    data = rangeData,
                    timeRange = range // Pass if needed for emptiness check or labels
                )
            }
        }
    }
}

@Composable
fun StatsPageContent(
    data: StatsData,
    timeRange: TimeRange
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (data.categoryTotals.isEmpty() && !data.isLoading) {
            item { EmptyState(title = "No data", subtitle = "No transactions in this ${timeRange.label.lowercase()}") }
            return@LazyColumn
        }

        if (data.isLoading && data.categoryTotals.isEmpty()) {
             // Show loading indicator if empty and loading
             item { 
                 Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                     androidx.compose.material3.CircularProgressIndicator(color = TealPrimary)
                 }
             }
             return@LazyColumn
        }

        // Donut chart
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Expense Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    DonutChart(
                        segments = data.categoryTotals.map { ct ->
                            DonutSegment(
                                label = ct.category.displayName,
                                value = ct.total.toFloat(),
                                color = ct.category.ui().color
                            )
                        },
                        centerText = CurrencyFormatter.formatShort(data.totalExpense),
                        centerSubText = "Total Spent"
                    )
                }
            }
        }

        // Daily spending bar chart
        if (data.dailyTotals.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Spending Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        BarChart(
                            bars = data.dailyTotals.entries.toList().takeLast(7).map { entry ->
                                BarData(
                                    label = DateUtils.formatDay(entry.key),
                                    value = entry.value.toFloat(),
                                    color = TealPrimary
                                )
                            }
                        )
                    }
                }
            }
        }

        // Category breakdown list
        item {
            Text(
                text = "By Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(data.categoryTotals) { ct ->
            val catUi = ct.category.ui()
            val percentage = if (data.totalExpense > 0) (ct.total / data.totalExpense).toFloat() else 0f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(catUi.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        catUi.icon,
                        contentDescription = null,
                        tint = catUi.color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = ct.category.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = CurrencyFormatter.format(ct.total),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                             color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Custom rounded progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(catUi.color.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percentage)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(catUi.color)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                     Text(
                        text = "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
