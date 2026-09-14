package com.kharcha.experiences.dashboard.impl.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalGasStation
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kharcha.core.common.util.CurrencyFormatter
import com.kharcha.core.designsystem.components.AdMobBanner
import com.kharcha.core.designsystem.components.CalendarWeekView
import com.kharcha.core.designsystem.components.EmptyState
import com.kharcha.core.designsystem.components.OverviewCards
import com.kharcha.core.designsystem.components.ManageCollectionsDialog
import com.kharcha.core.designsystem.components.CollectionItemData
import com.kharcha.core.designsystem.components.ShimmerItem
import com.kharcha.core.designsystem.components.TransactionCard
import com.kharcha.core.designsystem.components.TransactionOptionsSheet
import com.kharcha.core.designsystem.theme.ExpenseRed
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.model.Transaction
import com.kharcha.experiences.dashboard.impl.DashboardViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isAdFree by viewModel.isAdFree.collectAsState()

    val context = LocalContext.current
    var showReminderDialog by remember { mutableStateOf(false) }
    var showManageCollectionsDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Transaction?>(null) }
    var showOptionsFor by remember { mutableStateOf<Transaction?>(null) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                android.widget.Toast.makeText(context, "Notifications enabled!", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(context, "Permission denied. Reminders won't work.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    )

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
                androidx.compose.material3.TextButton(
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
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showReminderDialog) {
        val timePickerState = androidx.compose.material3.rememberTimePickerState(
            initialHour = 20,
            initialMinute = 0
        )

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showReminderDialog = false },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                            ) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                return@TextButton
                            }
                        }

                        viewModel.scheduleReminder(context, timePickerState.hour, timePickerState.minute, true)
                        showReminderDialog = false
                        android.widget.Toast.makeText(context, "Reminder set!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                Row {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            viewModel.scheduleReminder(context, 0, 0, false)
                            showReminderDialog = false
                            android.widget.Toast.makeText(context, "Reminder disabled", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Disable")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    androidx.compose.material3.TimePicker(state = timePickerState)
                }
            }
        )
    }


    if (showManageCollectionsDialog) {
        ManageCollectionsDialog(
            collections = state.collections.map {
                CollectionItemData(
                    name = it.name,
                    count = it.count,
                    isDefault = it.isDefault
                )
            },
            onDismiss = { showManageCollectionsDialog = false },
            onCreateCollection = { name ->
                viewModel.createCollection(name) { res ->
                    if (res.isFailure) {
                        android.widget.Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to create", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Collection '$name' created", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onRenameCollection = { oldName, newName ->
                viewModel.renameCollection(oldName, newName) { res ->
                    if (res.isFailure) {
                        android.widget.Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to rename", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Renamed to '$newName'", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDeleteCollection = { name ->
                viewModel.deleteCollection(name) { res ->
                    if (res.isFailure) {
                        android.widget.Toast.makeText(context, res.exceptionOrNull()?.message ?: "Failed to delete", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(context, "Deleted '$name'. Entries transferred to Home Expenses.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            AdMobBanner(isAdFree = isAdFree)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar / Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome back 👋",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Home Kharcha",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showReminderDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = "Reminders",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Overview Kharcha Cards (Pure Expense metrics)
            item {
                if (state.isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        ShimmerItem(modifier = Modifier.weight(1f).height(175.dp))
                        ShimmerItem(modifier = Modifier.weight(1f).height(175.dp))
                    }
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { -it / 2 }
                    ) {
                        OverviewCards(
                            totalKharcha = state.totalKharchaAllTime,
                            monthlyExpense = state.monthlyKharcha,
                            dailyExpense = state.dailyKharcha,
                            totalTransactionsCount = state.totalTransactionsCount,
                            collectionsCount = state.collections.size,
                            monthLabel = state.currentMonthLabel,
                            selectedCollectionName = state.selectedCollection,
                            selectedCollectionTotal = state.selectedCollectionTotal
                        )
                    }
                }
            }

            // 3. Quick Action Hub
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "Add Kharcha",
                        icon = Icons.Rounded.Add,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAddTransaction
                    )
                    QuickActionButton(
                        title = "History",
                        icon = Icons.Rounded.DateRange,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAllTransactions
                    )
                    QuickActionButton(
                        title = "Reminders",
                        icon = Icons.Rounded.Notifications,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        onClick = { showReminderDialog = true }
                    )
                }
            }

            // 4. Collections Carousel (Interactive Books)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Collections",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Tap a book to view all its entries",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.selectedCollection != null) {
                                TextButton(onClick = { viewModel.onSelectCollection(null) }) {
                                    Text(
                                        text = "Reset",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            TextButton(onClick = { showManageCollectionsDialog = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Manage Books",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Manage",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // "All Collections" card
                        item {
                            val isSelected = state.selectedCollection == null
                            CollectionCardItem(
                                name = "All Books",
                                totalExpense = state.collections.sumOf { it.totalExpense },
                                count = state.collections.sumOf { it.count },
                                icon = Icons.Rounded.MoreHoriz,
                                accentColor = Color(0xFF6366F1),
                                isSelected = isSelected,
                                onClick = { viewModel.onSelectCollection(null) }
                            )
                        }

                        items(state.collections) { colStat ->
                            val isSelected = state.selectedCollection.equals(colStat.name, ignoreCase = true)
                            val iconAndColor = getCollectionIconAndColor(colStat.name)
                            CollectionCardItem(
                                name = colStat.name,
                                totalExpense = colStat.totalExpense,
                                count = colStat.count,
                                icon = iconAndColor.first,
                                accentColor = iconAndColor.second,
                                isSelected = isSelected,
                                onClick = { viewModel.onSelectCollection(colStat.name) }
                            )
                        }

                        // "+ New Book" card
                        item {
                            NewBookCardItem(onClick = { showManageCollectionsDialog = true })
                        }
                    }
                }
            }

            // 4b. Active Filter Indicator Banner
            if (state.isFilteringByCollection && state.selectedCollection != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                            .clickable { viewModel.onSelectCollection(null) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtered by ${state.selectedCollection} (${state.displayedTransactions.size} entries)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Clear ✕",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // 5. Weekly Calendar Strip (only shown if not actively filtering collection)
            if (!state.isFilteringByCollection) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            text = "Calendar View",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        CalendarWeekView(
                            selectedDate = state.selectedDate,
                            onDateSelected = { viewModel.onDateSelected(it) }
                        )
                    }
                }
            }

            // 6. Transactions Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val headerTitle = when {
                        state.selectedCollection != null -> "${state.selectedCollection} (${state.displayedTransactions.size})"
                        state.dailyKharcha > 0 -> "Activity on Date (${state.displayedTransactions.size})"
                        else -> "Recent Transactions (${state.displayedTransactions.size})"
                    }
                    Text(
                        text = headerTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    TextButton(onClick = {
                        com.kharcha.core.common.util.SharedFilter.preselectedCollection = state.selectedCollection
                        onNavigateToAllTransactions()
                    }) {
                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // 7. Transactions List
            if (state.isLoading) {
                items(4) {
                    ShimmerItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .padding(horizontal = 20.dp)
                    )
                }
            } else if (state.displayedTransactions.isEmpty()) {
                item {
                    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy") }
                    EmptyState(
                        title = "No transactions found",
                        subtitle = if (state.selectedCollection != null) {
                            "No entries recorded for ${state.selectedCollection}"
                        } else {
                            "No entries recorded for ${state.selectedDate.format(dateFormatter)}"
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                itemsIndexed(
                    items = state.displayedTransactions,
                    key = { _, t -> t.id }
                ) { _, transaction ->
                    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        TransactionCard(
                            transaction = transaction,
                            onClick = { onNavigateToEditTransaction(transaction.id) },
                            onLongClick = {
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                showOptionsFor = transaction
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CollectionCardItem(
    name: String,
    totalExpense: Double,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = name,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = CurrencyFormatter.format(totalExpense),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private fun getCollectionIconAndColor(name: String): Pair<ImageVector, Color> {
    val n = name.lowercase()
    return when {
        "home" in n && "reno" in n -> Icons.Rounded.Home to Color(0xFFF59E0B)
        "home" in n -> Icons.Rounded.Home to Color(0xFF38BDF8)
        "sissy" in n -> Icons.Rounded.CardGiftcard to Color(0xFFEC4899)
        "bike" in n -> Icons.Rounded.LocalGasStation to Color(0xFF10B981)
        "car" in n -> Icons.Rounded.DirectionsBus to Color(0xFF06B6D4)
        "invest" in n -> Icons.AutoMirrored.Rounded.TrendingUp to Color(0xFF8B5CF6)
        "lend" in n -> Icons.Rounded.AccountBalance to Color(0xFF6366F1)
        "lawyer" in n || "legal" in n -> Icons.Rounded.Work to Color(0xFFE11D48)
        else -> Icons.Rounded.ShoppingCart to Color(0xFFA855F7)
    }
}

@Composable
private fun NewBookCardItem(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(130.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "New Book",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "New Book",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
