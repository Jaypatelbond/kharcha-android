package com.kharcha.experiences.history.impl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kharcha.core.common.util.CurrencyFormatter
import com.kharcha.core.common.util.DateUtils
import com.kharcha.core.designsystem.components.AdMobBanner
import com.kharcha.core.designsystem.components.EmptyState
import com.kharcha.core.designsystem.components.TransactionCard
import com.kharcha.core.designsystem.components.TransactionOptionsSheet
import com.kharcha.core.designsystem.theme.ExpenseRed
import com.kharcha.core.designsystem.theme.IncomeGreen
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.model.Transaction
import com.kharcha.core.model.TransactionType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isAdFree by viewModel.isAdFree.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDateRangePicker by remember { mutableStateOf(false) }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    // Sync Pager -> ViewModel
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        val newFilter = when (pagerState.currentPage) {
            0 -> null
            1 -> TransactionType.EXPENSE
            2 -> TransactionType.INCOME
            else -> null
        }
        if (uiState.selectedTypeFilter != newFilter) {
            viewModel.onTypeFilterChange(newFilter)
        }
    }

    var showDeleteDialog by remember { mutableStateOf<Transaction?>(null) }
    var showOptionsFor by remember { mutableStateOf<Transaction?>(null) }

    if (showOptionsFor != null) {
        TransactionOptionsSheet(
            transaction = showOptionsFor!!,
            onDismiss = { showOptionsFor = null },
            onEdit = {
                val id = showOptionsFor!!.id
                showOptionsFor = null
                onNavigateToEditTransaction(id)
            },
            onDelete = {
                val tx = showOptionsFor
                showOptionsFor = null
                showDeleteDialog = tx
            }
        )
    }

    if (showDeleteDialog != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(showDeleteDialog!!)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Material 3 Date Range Picker Dialog
    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = uiState.customStartDate,
            initialSelectedEndDateMillis = uiState.customEndDate
        )
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis ?: start
                        if (start != null && end != null) {
                            val actualEnd = DateUtils.getEndOfDay(end)
                            viewModel.setCustomDateRange(start, actualEnd)
                        }
                        showDateRangePicker = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateRangePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AdMobBanner(isAdFree = isAdFree)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(top = 12.dp)
        ) {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Collection Filter Chips with Counts
            val allCollections = remember(uiState.availableCollections, uiState.collectionCounts) {
                val defaults = listOf(
                    "All Collections",
                    "Home Expenses",
                    "Personal & MISC",
                    "Bike",
                    "Sissy Expenses",
                    "Home Renovation",
                    "Car",
                    "Legal & Lawyer",
                    "Lend & Borrow",
                    "Investments"
                )
                val dynamic = uiState.availableCollections.filter { it !in defaults && it.isNotBlank() }
                defaults + dynamic
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allCollections) { collectionName ->
                    val isSelected = if (collectionName == "All Collections") {
                        uiState.selectedCollection == null
                    } else {
                        uiState.selectedCollection.equals(collectionName, ignoreCase = true)
                    }

                    val count = if (collectionName == "All Collections") {
                        uiState.transactions.size
                    } else {
                        uiState.collectionCounts[collectionName] ?: 0
                    }

                    val label = if (collectionName == "All Collections") {
                        "All ($count)"
                    } else {
                        "$collectionName ($count)"
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.onSelectCollection(if (collectionName == "All Collections") null else collectionName)
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Date Mode Chips: [All Time | Monthly | Date Range]
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                HistoryDateMode.entries.forEach { mode ->
                    val isSelected = uiState.selectedDateMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setDateMode(mode)
                            if (mode == HistoryDateMode.DATE_RANGE && (uiState.customStartDate == null || uiState.customEndDate == null)) {
                                showDateRangePicker = true
                            }
                        },
                        label = {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Date Selection Detail Row / Card
            when (uiState.selectedDateMode) {
                HistoryDateMode.ALL_TIME -> {
                    val filterDesc = if (uiState.selectedCollection != null) {
                        "Showing all ${uiState.dateFilteredTransactions.size} entries for ${uiState.selectedCollection}"
                    } else {
                        "Showing all ${uiState.dateFilteredTransactions.size} transactions across books"
                    }
                    Text(
                        text = filterDesc,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                }

                HistoryDateMode.MONTHLY -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.onPreviousMonth() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = uiState.selectedMonthLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(onClick = { viewModel.onNextMonth() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                HistoryDateMode.DATE_RANGE -> {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clickable { showDateRangePicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val rangeText = if (uiState.customStartDate != null && uiState.customEndDate != null) {
                                "${DateUtils.formatShortDate(uiState.customStartDate!!)} - ${DateUtils.formatShortDate(uiState.customEndDate!!)}"
                            } else {
                                "Tap to select date range"
                            }
                            Text(
                                text = rangeText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Rounded.DateRange,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Period Summary Banner
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Expense",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            CurrencyFormatter.format(uiState.totalExpense),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            CurrencyFormatter.format(uiState.totalIncome),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Entries",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${uiState.dateFilteredTransactions.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Tabs: [All | Expenses | Income]
            androidx.compose.material3.TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {},
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            ) {
                val tabs = listOf("All", "Expenses", "Income")
                tabs.forEachIndexed { index, title ->
                    androidx.compose.material3.Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(title, fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pageTypeFilter = when (page) {
                    0 -> null
                    1 -> TransactionType.EXPENSE
                    2 -> TransactionType.INCOME
                    else -> null
                }

                val localFiltered = if (pageTypeFilter == null) {
                    uiState.dateFilteredTransactions
                } else {
                    uiState.dateFilteredTransactions.filter { it.type == pageTypeFilter }
                }

                HistoryListContent(
                    transactions = localFiltered,
                    isLoading = uiState.isLoading,
                    searchQuery = uiState.searchQuery,
                    selectedCollection = uiState.selectedCollection,
                    collectionCount = uiState.collectionCounts[uiState.selectedCollection] ?: 0,
                    onViewAllTime = { viewModel.setDateMode(HistoryDateMode.ALL_TIME) },
                    onTransactionClick = onNavigateToEditTransaction,
                    onDelete = { viewModel.deleteTransaction(it) },
                    onLongClick = { showOptionsFor = it }
                )
            }
        }
    }
}

@Composable
fun HistoryListContent(
    transactions: List<Transaction>,
    isLoading: Boolean,
    searchQuery: String,
    selectedCollection: String?,
    collectionCount: Int,
    onViewAllTime: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onDelete: (Transaction) -> Unit,
    onLongClick: (Transaction) -> Unit
) {
    if (isLoading) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(6) {
                com.kharcha.core.designsystem.components.ShimmerItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        }
        return
    }

    if (transactions.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState(
                title = if (searchQuery.isNotBlank()) "No results found" else "No entries in this period",
                subtitle = if (selectedCollection != null && collectionCount > 0) {
                    "$selectedCollection has $collectionCount entries in other months."
                } else {
                    "Try selecting 'All Time' or another date range"
                }
            )
            if (selectedCollection != null && collectionCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onViewAllTime,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View All $collectionCount Entries (All Time)", fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    // Grouped transaction list
    val grouped = transactions.groupBy { DateUtils.getRelativeDateLabel(it.date) }

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        grouped.forEach { (dateLabel, txns) ->
            item {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(
                items = txns,
                key = { it.id }
            ) { transaction ->
                val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
                TransactionCard(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) },
                    onLongClick = {
                        haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        onLongClick(transaction)
                    }
                )
            }
        }
    }
}
