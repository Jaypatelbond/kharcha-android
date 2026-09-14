package com.kharcha.tracker.presentation.screens.split.detail

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kharcha.core.model.MemberBalance
import com.kharcha.core.model.SplitExpense
import com.kharcha.core.designsystem.components.MemberAvatar
import com.kharcha.core.designsystem.theme.ExpenseRed
import com.kharcha.core.designsystem.theme.IncomeGreen
import com.kharcha.core.designsystem.theme.TealPrimary
import com.kharcha.core.common.util.CurrencyFormatter
import com.kharcha.core.common.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onNavigateBack: () -> Unit,
    onAddExpenseClick: (Long) -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel(),
    mainViewModel: com.kharcha.tracker.MainViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isAdFree by mainViewModel.isAdFree.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showDeleteGroupDialog by remember { mutableStateOf(false) }
    var showLeaveGroupDialog by remember { mutableStateOf(false) }
    var memberToRemove by remember { mutableStateOf<Pair<Long, String>?>(null) }

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })
    
    // Sync Pager and Tabs
    LaunchedEffect(selectedTab) {
        pagerState.animateScrollToPage(selectedTab)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name ->
                viewModel.addMember(name)
                showAddMemberDialog = false
            }
        )
    }

    if (showDeleteGroupDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteGroupDialog = false },
            title = { Text("Delete Group?") },
            text = { Text("Are you sure you want to delete \"${state.groupWithMembers?.group?.name ?: "this group"}\"? All expenses, shares, and members in this group will be permanently deleted.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteGroupDialog = false
                        viewModel.deleteGroup {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLeaveGroupDialog) {
        val members = state.groupWithMembers?.members ?: emptyList()
        var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: 0L) }
        AlertDialog(
            onDismissRequest = { showLeaveGroupDialog = false },
            title = { Text("Leave Group") },
            text = {
                Column {
                    Text("Select your member profile to leave and remove yourself from this group:")
                    Spacer(modifier = Modifier.height(12.dp))
                    members.forEach { member ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMemberId = member.id }
                                .padding(vertical = 4.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = (member.id == selectedMemberId),
                                onClick = { selectedMemberId = member.id }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(member.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLeaveGroupDialog = false
                        if (selectedMemberId != 0L) {
                            viewModel.removeMember(selectedMemberId) {
                                onNavigateBack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Leave Group")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    memberToRemove?.let { (id, name) ->
        AlertDialog(
            onDismissRequest = { memberToRemove = null },
            title = { Text("Remove Member?") },
            text = { Text("Are you sure you want to remove \"$name\" from this group? Any expenses associated with them will also be removed.") },
            confirmButton = {
                Button(
                    onClick = {
                        val toRemoveId = id
                        memberToRemove = null
                        viewModel.removeMember(toRemoveId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { memberToRemove = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.groupWithMembers?.group?.name ?: "Group Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    var showMenu by remember { mutableStateOf(false) }
                    
                    val importLauncher = rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            viewModel.importGroup(uri, context)
                        }
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "More Options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share Text Summary") },
                            leadingIcon = { Icon(Icons.Rounded.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                // Logic from before (Share Text)
                                val summary = buildString {
                                    appendLine("📊 Kharcha: ${state.groupWithMembers?.group?.name} Statement")
                                    appendLine("--------------------------------")
                                    if (state.balances.isEmpty()) {
                                        appendLine("All settled up! ✅")
                                    } else {
                                        state.balances.forEach { balance ->
                                            val amount = CurrencyFormatter.format(kotlin.math.abs(balance.balance))
                                            if (balance.balance > 0) {
                                                appendLine("${balance.memberName} gets back $amount")
                                            } else if (balance.balance < 0) {
                                                appendLine("${balance.memberName} owes $amount")
                                            }
                                        }
                                    }
                                    appendLine("--------------------------------")
                                    appendLine("Generated by Kharcha 🇮🇳")
                                }
                                
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, summary)
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Group Summary")
                                context.startActivity(shareIntent)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Export Group File") },
                            leadingIcon = { Icon(Icons.Rounded.Upload, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                viewModel.exportGroup(context)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Group File") },
                            leadingIcon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                importLauncher.launch("application/json")
                            }
                        )
                        androidx.compose.material3.HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Leave Group (Remove Myself)", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showLeaveGroupDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Group", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteGroupDialog = true
                            }
                        )
                    }

                    IconButton(onClick = { showAddMemberDialog = true }) {
                        Icon(Icons.Rounded.PersonAdd, contentDescription = "Add Member")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) { // Only show FAB on expenses tab
                FloatingActionButton(
                    onClick = { state.groupWithMembers?.group?.id?.let { onAddExpenseClick(it) } },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Expense")
                }
            }
        },
        bottomBar = {
            com.kharcha.core.designsystem.components.AdMobBanner(isAdFree = isAdFree)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Modern Segmented Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
                listOf("Expenses", "Balances").forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { 
                                selectedTab = index 
                                haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = TealPrimary)
                }
            } else {
                androidx.compose.foundation.pager.HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> ExpensesList(state.expenses)
                        1 -> BalancesList(
                            balances = state.balances,
                            onRemoveMember = { id, name ->
                                memberToRemove = id to name
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensesList(expenses: List<SplitExpense>) {
    if (expenses.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.ReceiptLong,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No expenses yet",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Add an expense to start splitting!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), // Increased spacing
            modifier = Modifier.fillMaxSize()
        ) {
            items(expenses) { expense ->
                ExpenseItem(expense)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun ExpenseItem(expense: SplitExpense) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33475569)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Initial of payer or description icon? Let's use generic for now or maybe payer name if available
                // Actually SplitExpense has paidByMemberId but I need name... 
                // Getting name here is hard without lookup. 
                // Maybe just show Date prominent? 
                Column {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.formatDate(expense.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = CurrencyFormatter.format(expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun BalancesList(
    balances: List<MemberBalance>,
    onRemoveMember: (Long, String) -> Unit = { _, _ -> }
) {
    if (balances.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = IncomeGreen.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "All settled up!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "No one owes anything in this group.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(balances) { balance ->
                BalanceItem(
                    balance = balance,
                    onRemove = { onRemoveMember(balance.memberId, balance.memberName) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun BalanceItem(
    balance: MemberBalance,
    onRemove: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33475569)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                MemberAvatar(
                    name = balance.memberName,
                    size = 48.dp,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = balance.memberName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            val isOwed = balance.balance > 0
            val isDebt = balance.balance < 0
            val color = when {
                isOwed -> IncomeGreen
                isDebt -> ExpenseRed
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isOwed) "gets back" else if (isDebt) "owes" else "settled",
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                    Text(
                        text = CurrencyFormatter.format(kotlin.math.abs(balance.balance)),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Remove Member",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Member") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
