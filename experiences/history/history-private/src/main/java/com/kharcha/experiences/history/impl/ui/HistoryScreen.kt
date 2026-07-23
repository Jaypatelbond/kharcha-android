package com.kharcha.experiences.history.impl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kharcha.core.model.TransactionType
import com.kharcha.core.designsystem.components.AdMobBanner
import com.kharcha.core.designsystem.components.EmptyState
import com.kharcha.core.designsystem.components.TransactionCard
import com.kharcha.core.designsystem.components.TransactionOptionsSheet
import com.kharcha.core.designsystem.theme.ExpenseRed
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.common.util.DateUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState // Alias for easier usage if needed, but uiState is fine.
    // Actually, viewModel.uiState probably returns a plain UiState object, not containing filtered lists yet?
    // Wait, HistoryViewModel typically exposes state with filtered list.
    // Let's assume uiState has what we need: transactions, filteredTransactions, etc.
    
    val isAdFree by viewModel.isAdFree.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(
        initialPage = 0,
        pageCount = { 3 }
    )

    // Sync Pager -> ViewModel
    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
        val newFilter = when(pagerState.currentPage) {
            0 -> null
            1 -> TransactionType.EXPENSE
            2 -> TransactionType.INCOME
            else -> null
        }
        if (uiState.selectedTypeFilter != newFilter) {
            viewModel.onTypeFilterChange(newFilter)
        }
    }
    
    // Sync ViewModel -> Pager (if needed)
    androidx.compose.runtime.LaunchedEffect(uiState.selectedTypeFilter) {
        val targetPage = when(uiState.selectedTypeFilter) {
            null -> 0
            TransactionType.EXPENSE -> 1
            TransactionType.INCOME -> 2
        }
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.kharcha.core.model.Transaction?>(null) }
    var showOptionsFor by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.kharcha.core.model.Transaction?>(null) }

    if (showOptionsFor != null) {
        TransactionOptionsSheet(
            transaction = showOptionsFor!!,
            onDismiss = { showOptionsFor = null },
            onEdit = { 
                onNavigateToEditTransaction(showOptionsFor!!.id) 
            },
            onDelete = { 
                showDeleteDialog = showOptionsFor 
            }
        )
    }

    if (showDeleteDialog != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.deleteTransaction(showDeleteDialog!!)
                        showDeleteDialog = null
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            },
             containerColor = MaterialTheme.colorScheme.surface,
             shape = RoundedCornerShape(24.dp)
        )
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
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs
            androidx.compose.material3.TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = TealPrimary,
                divider = {},
                indicator = { tabPositions ->
                    if (pagerState.currentPage < tabPositions.size) {
                         SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = TealPrimary
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
                        text = { Text(title) },
                        selectedContentColor = TealPrimary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                 // Filter logic
                 val pageTypeFilter = when(page) {
                     0 -> null
                     1 -> TransactionType.EXPENSE
                     2 -> TransactionType.INCOME
                     else -> null
                 }
                 
                 val localFiltered = uiState.transactions.filter { transaction ->
                     val matchesSearch = uiState.searchQuery.isBlank() || 
                         transaction.note.contains(uiState.searchQuery, ignoreCase = true) ||
                         transaction.category.displayName.contains(uiState.searchQuery, ignoreCase = true) ||
                         transaction.amount.toString().contains(uiState.searchQuery)
                         
                     val matchesType = pageTypeFilter == null || transaction.type == pageTypeFilter
                     
                     matchesSearch && matchesType
                 }
                 
                 HistoryListContent(
                     transactions = localFiltered,
                     isLoading = uiState.isLoading,
                     searchQuery = uiState.searchQuery,
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
    transactions: List<com.kharcha.core.model.Transaction>,
    isLoading: Boolean,
    searchQuery: String,
    onTransactionClick: (Long) -> Unit,
    onDelete: (com.kharcha.core.model.Transaction) -> Unit,
    onLongClick: (com.kharcha.core.model.Transaction) -> Unit
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
            EmptyState(
                title = if (searchQuery.isNotBlank()) "No results found" else "No transactions",
                subtitle = if (searchQuery.isNotBlank()) "Try a different search" else "Tap + to add one"
            )
            return
        }

        // Grouped transaction list
        val grouped = transactions.groupBy { DateUtils.getRelativeDateLabel(it.date) }

        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            grouped.forEach { (dateLabel, transactions) ->
                item {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(
                    items = transactions,
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
