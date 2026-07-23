package com.kharcha.experiences.dashboard.impl.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.kharcha.experiences.dashboard.impl.DashboardViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kharcha.core.designsystem.components.AdMobBanner
import com.kharcha.core.designsystem.components.EmptyState
import com.kharcha.core.designsystem.components.TransactionCard

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToEditTransaction: (Long) -> Unit,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val state = uiState // Alias for easier access if code uses 'state'
    val isAdFree by viewModel.isAdFree.collectAsState()
    val context = LocalContext.current
    // We need a way to show the reminder settings dialog.
    var showReminderDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    var showDeleteDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.kharcha.core.model.Transaction?>(null) }
    var showOptionsFor by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.kharcha.core.model.Transaction?>(null) }

    // Permission Launcher
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, now we can schedule or test
                android.widget.Toast.makeText(context, "Notifications enabled!", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                android.widget.Toast.makeText(context, "Permission denied. Reminders won't work.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    )

    Scaffold(
        bottomBar = {
            AdMobBanner(isAdFree = isAdFree)
        }
    ) { padding ->
        // Ask for notification permission on launch (Android 13+)
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar Area
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good day,",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    // Notification Icon
                    androidx.compose.material3.IconButton(
                        onClick = { showReminderDialog = true },
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Reminders",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Calendar Week View
            item {
                com.kharcha.core.designsystem.components.CalendarWeekView(
                    selectedDate = state.selectedDate,
                    onDateSelected = { viewModel.onDateSelected(it) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Overview Cards (Horizontal Scroll)
            item {
                if (state.isLoading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                         // Balance Card Shimmer
                         com.kharcha.core.designsystem.components.ShimmerItem(
                             modifier = Modifier
                                 .weight(1f)
                                 .height(160.dp)
                         )
                         // Income/Expense Shimmer (smaller column)
                         Column(
                             modifier = Modifier
                                 .weight(1f)
                                 .height(160.dp),
                             verticalArrangement = Arrangement.spacedBy(12.dp)
                         ) {
                             com.kharcha.core.designsystem.components.ShimmerItem(modifier = Modifier.weight(1f).fillMaxWidth())
                             com.kharcha.core.designsystem.components.ShimmerItem(modifier = Modifier.weight(1f).fillMaxWidth())
                         }
                    }
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { -it / 2 }
                    ) {
                        com.kharcha.core.designsystem.components.OverviewCards(
                            balance = state.balance,
                            income = state.totalIncome,
                            expense = state.totalExpense
                        )
                    }
                }
            }

            // Recent Transactions Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (state.isLoading) {
                 items(5) {
                     com.kharcha.core.designsystem.components.ShimmerItem(
                         modifier = Modifier
                             .fillMaxWidth()
                             .height(80.dp)
                             .padding(horizontal = 20.dp)
                     )
                 }
            } else if (state.recentTransactions.isEmpty()) {
                item {
                    val dateFormatter = androidx.compose.runtime.remember { java.time.format.DateTimeFormatter.ofPattern("MMM dd") }
                    EmptyState(
                        title = "No transactions found",
                        subtitle = "No transactions recorded for ${state.selectedDate.format(dateFormatter)}",
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            // Transaction list
            itemsIndexed(
                items = state.recentTransactions,
                key = { _, t -> t.id }
            ) { index, transaction ->
                val enterAnim = androidx.compose.runtime.remember { fadeIn() + slideInVertically { it / 3 } }
                AnimatedVisibility(
                    visible = true,
                    enter = enterAnim
                ) {
                    TransactionCard(
                        transaction = transaction,
                        onClick = { onNavigateToEditTransaction(transaction.id) },
                        onLongClick = { showOptionsFor = transaction },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }

    if (showOptionsFor != null) {
        com.kharcha.core.designsystem.components.TransactionOptionsSheet(
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
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = com.kharcha.core.designsystem.theme.ExpenseRed)
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
             shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
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
                        // Check Permission before enabling
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                             if (androidx.core.content.ContextCompat.checkSelfPermission(
                                     context,
                                     android.Manifest.permission.POST_NOTIFICATIONS
                                 ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                             ) {
                                 permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                 return@TextButton // Wait for result
                             }
                        }

                        viewModel.scheduleReminder(
                            context,
                            timePickerState.hour,
                            timePickerState.minute,
                            true
                        )
                        showReminderDialog = false
                        android.widget.Toast.makeText(context, "Reminder set!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Enable")
                }
            },
            dismissButton = {
                Row {
                     // Test Button (Debug Only)
                    val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
                    if (isDebug) {
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
                                viewModel.sendTestNotification(context)
                            }
                        ) {
                            Text("Test")
                        }
                    }
                    
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
}
