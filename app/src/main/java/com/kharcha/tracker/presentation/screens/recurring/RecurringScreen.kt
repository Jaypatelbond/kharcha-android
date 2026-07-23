package com.kharcha.tracker.presentation.screens.recurring

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kharcha.tracker.data.local.entity.RecurringTransactionEntity
import com.kharcha.tracker.presentation.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    onBack: () -> Unit,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val transactions by viewModel.recurringTransactions.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring & Subscriptions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onAddClick,
                containerColor = TealPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Recurring")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Repeat, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No recurring transactions yet.", color = Color.Gray)
                        Text("Add rent, subscriptions, or bills.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(transactions) { item ->
                        RecurringItem(
                            item = item,
                            onDelete = { viewModel.deleteRecurringTransaction(item) }
                        )
                    }
                }
            }
        }

        if (uiState.showDialog) {
            AddRecurringDialog(
                state = uiState,
                onDismiss = viewModel::onDialogDismiss,
                onAmountChange = viewModel::onAmountChange,
                onNoteChange = viewModel::onNoteChange,
                onTypeChange = viewModel::onTypeChange,
                onFrequencyChange = viewModel::onFrequencyChange,
                onReminderToggle = viewModel::onReminderToggle,
                onSave = viewModel::saveRecurringTransaction
            )
        }
    }
}

@Composable
fun RecurringItem(
    item: RecurringTransactionEntity,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.note, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.frequency, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    if (item.reminderEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🔔 On", style = MaterialTheme.typography.bodySmall, color = TealPrimary)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "₹${item.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.type == "INCOME") Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                IconButton(onClick = onDelete) {
                    Icon(androidx.compose.material.icons.Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AddRecurringDialog(
    state: RecurringUiState,
    onDismiss: () -> Unit,
    onAmountChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onTypeChange: (Boolean) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.error != null) {
                    Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = state.note,
                    onValueChange = onNoteChange,
                    label = { Text("Name (e.g. Rent, Netflix)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.amount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Type: ", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = state.type == "EXPENSE",
                        onClick = { onTypeChange(true) },
                        label = { Text("Expense") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = state.type == "INCOME",
                        onClick = { onTypeChange(false) },
                        label = { Text("Income") }
                    )
                }
                
                // Frequency Dropdown (Simplified as Row for MVP)
                Text("Frequency:", style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState())) {
                    listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY").forEach { freq ->
                        FilterChip(
                            selected = state.frequency == freq,
                            onClick = { onFrequencyChange(freq) },
                            label = { Text(freq) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                // Reminder Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remind me when due", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = state.reminderEnabled,
                        onCheckedChange = onReminderToggle,
                        colors = SwitchDefaults.colors(checkedTrackColor = TealPrimary)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
